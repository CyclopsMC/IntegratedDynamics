package org.cyclops.integrateddynamics.core.inventory.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.container.ScrollingInventoryContainer;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.PartStateException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IAspectVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.item.AspectVariableFacade;
import org.cyclops.integrateddynamics.core.part.PartStateAspectVariablesHandler;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Container for parts.
 *
 * @author rubensworks
 */
public abstract class ContainerMultipartAspects<P extends IPartType<P, S>, S extends IPartState<P>, A extends IAspect<?, ?>>
        extends ScrollingInventoryContainer<A> implements IDirtyMarkListener {

    public static String BUTTON_SETTINGS = "button_settings";
    public static String BUTTON_OFFSETS = "button_offsets";

    private static final int PAGE_SIZE = 3;

    private final PartTarget target;
    private final IPartContainer partContainer;
    private final P partType;
    private final Level world;
    private final Map<IAspect, String> aspectPropertyButtons = Maps.newHashMap();
    private final Map<IAspect, Integer> aspectPropertyValueIds = Maps.newIdentityHashMap();

    protected final Container inputSlots;

    public ContainerMultipartAspects(@Nullable MenuType<?> type, int id, Inventory playerInventory, Container inventory,
                                     PartTarget target, Optional<IPartContainer> partContainer, P partType,
                                     List<A> items) {
        super(type, id, playerInventory, inventory, items, (item, pattern) -> {
            // We could cache this if this would prove to be a bottleneck.
            // But we have a small amount of aspects, so this shouldn't be a problem.
            return pattern.matcher(IModHelpers.get().getL10NHelpers().localize(item.getTranslationKey()).toLowerCase(Locale.ENGLISH)).matches();
        });
        this.target = target;
        this.partContainer = partContainer.orElseGet(() -> PartHelpers.getPartContainerChecked(target.getCenter()));
        this.partType = partType;
        this.world = player.level();

        this.inputSlots = constructInputSlotsInventory();

        putButtonAction(ContainerMultipartAspects.BUTTON_SETTINGS, (s, containerExtended) -> {
            if (!world.isClientSide()) {
                PartHelpers.openContainerPartSettings((ServerPlayer) player, target.getCenter(), partType);
            }
        });
        putButtonAction(ContainerMultipartAspects.BUTTON_OFFSETS, (s, containerExtended) -> {
            if (!world.isClientSide()) {
                PartHelpers.openContainerPartOffsets((ServerPlayer) player, target.getCenter(), partType);
            }
        });

        for (final IAspect aspect : getUnfilteredItems()) {
            if (aspect.hasProperties()) {
                String buttonId = "button_aspect_" + aspect.getUniqueName();
                aspectPropertyButtons.put(aspect, buttonId);
                aspectPropertyValueIds.put(aspect, getNextValueId());
                putButtonAction(buttonId, (s, containerExtended) -> {
                    if (!world.isClientSide()) {
                        PartHelpers.openContainerAspectSettings((ServerPlayer) player, target.getCenter(), aspect);
                    }
                });
            }
        }
    }

    public P getPartType() {
        return partType;
    }

    public PartTarget getTarget() {
        return target;
    }

    public IPartContainer getPartContainer() {
        return partContainer;
    }

    public S getPartState() {
        return (S) partContainer.getPartState(getTarget().getCenter().getSide());
    }

    public Map<IAspect, String> getAspectPropertyButtons() {
        return Collections.unmodifiableMap(this.aspectPropertyButtons);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (!player.level().isClientSide()) {
            for (Map.Entry<IAspect, Integer> entry : this.aspectPropertyValueIds.entrySet()) {
                ValueNotifierHelpers.setValue(this, entry.getValue(), getModifiedAspectPropertyValues(entry.getKey()));
            }
        }
    }

    /**
     * Determine the values of all properties of the given aspect that deviate from their default value.
     *
     * The returned list has one entry for each of the aspect's property types,
     * in the order of {@link IAspect#getPropertyTypes()}.
     * Properties that still have their default value are represented by an empty component.
     * Properties that have a variable in their slot are always included,
     * as their value can change at any time.
     * If the variable of a property is erroring, its value is shown in red,
     * as the shown value is then the statically configured fallback value.
     *
     * This can be called on both sides.
     * Client-side, the values that are driven by variables are unknown,
     * so the statically configured values are returned for those.
     *
     * @param aspect An aspect that has properties.
     * @return The modified property values, in the order of the aspect's property types.
     */
    @SuppressWarnings("unchecked")
    public List<Component> getModifiedAspectPropertyValues(IAspect aspect) {
        IPartState<P> partState = getPartState();
        IAspectProperties defaultProperties = aspect.getDefaultProperties();
        IAspectProperties properties = partState.getAspectProperties(aspect);
        if (properties == null) {
            properties = defaultProperties;
        }
        // Variables take precedence over the statically configured values
        IAspectProperties variableDrivenProperties = partState.getAspectPropertiesVariableDriven(aspect, properties);
        if (variableDrivenProperties != null) {
            properties = variableDrivenProperties;
        }
        // The variables themselves are part of the part state, so they are known on both sides,
        // even though only the server knows the values they produce.
        NonNullList<ItemStack> variables = partState.getInventoryNamed(PartStateAspectVariablesHandler.getInventoryName(aspect));

        List<Component> values = Lists.newArrayList();
        int propertyIndex = 0;
        for (IAspectPropertyTypeInstance property : (Collection<IAspectPropertyTypeInstance>) aspect.getPropertyTypes()) {
            IValue value = properties.getValue(property);
            IValue defaultValue = defaultProperties.getValue(property);
            boolean hasVariable = variables != null && propertyIndex < variables.size()
                    && !variables.get(propertyIndex).isEmpty();
            boolean variableDriven = hasVariable || partState.getAspectVariableValue(aspect, propertyIndex) != null;
            boolean variableErrored = partState.getAspectVariableError(aspect, propertyIndex) != null;
            if (value == null || (!variableDriven && !variableErrored
                    && ValueHelpers.areValuesEqual(value, defaultValue))) {
                values.add(Component.empty());
            } else {
                IValueType valueType = value.getType();
                MutableComponent compactValue = valueType.toCompactString(value);
                values.add(compactValue == null
                        ? Component.empty()
                        : compactValue.withStyle(variableErrored
                                ? ChatFormatting.RED : valueType.getDisplayColorFormat()));
            }
            propertyIndex++;
        }
        return values;
    }

    /**
     * @param aspect An aspect.
     * @return The value id under which the property values of the given aspect are synced to the client,
     *         or null if the given aspect has no properties.
     */
    @Nullable
    public Integer getAspectPropertyValueId(IAspect aspect) {
        return this.aspectPropertyValueIds.get(aspect);
    }

    /**
     * Get the modified property values of the given aspect, as synced from the server.
     * @param aspect An aspect.
     * @return The modified property values, in the order of {@link IAspect#getPropertyTypes()},
     *         or null if they are unknown.
     */
    @Nullable
    public List<Component> getModifiedAspectPropertyValuesSynced(IAspect aspect) {
        Integer valueId = this.aspectPropertyValueIds.get(aspect);
        if (valueId == null) {
            return null;
        }
        return ValueNotifierHelpers.getValueTextComponentList(this, valueId);
    }

    /**
     * Get the property values of the given aspect that must be shown in the gui.
     *
     * The statically configured values are determined locally,
     * which is possible because the part state they are stored in is available on both sides.
     * Values that the server has synced are layered on top of those,
     * as only the server knows the values that are driven by variables.
     *
     * @param aspect An aspect that has properties.
     * @return The property values to show, in the order of {@link IAspect#getPropertyTypes()}.
     */
    public List<Component> getShownAspectPropertyValues(IAspect aspect) {
        List<Component> syncedValues = getModifiedAspectPropertyValuesSynced(aspect);
        List<Component> values;
        try {
            values = getModifiedAspectPropertyValues(aspect);
        } catch (PartStateException e) {
            // The part may have been removed in the meantime,
            // in which case we can only rely on what the server has sent before.
            return syncedValues;
        }
        if (syncedValues != null) {
            for (int i = 0; i < Math.min(values.size(), syncedValues.size()); i++) {
                Component syncedValue = syncedValues.get(i);
                if (syncedValue != null && !syncedValue.getString().isEmpty()) {
                    values.set(i, syncedValue);
                }
            }
        }
        return values;
    }

    public abstract int getAspectBoxHeight();

    protected Container constructInputSlotsInventory() {
        SimpleInventory inventory = new SimpleInventory(getUnfilteredItemCount(), 1);
        inventory.addDirtyMarkListener(this);
        return inventory;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (inputSlots instanceof SimpleInventory) {
            ((SimpleInventory) inputSlots).removeDirtyMarkListener(this);
        }
    }

    protected void disableSlot(int slotIndex) {
        Slot slot = getSlot(slotIndex);
        // Yes I know this is ugly.
        // If you are reading this and know a better way, please tell me.
        setSlotPosX(slot, Integer.MIN_VALUE);
        setSlotPosY(slot, Integer.MIN_VALUE);
    }

    protected abstract void enableSlot(int slotIndex, int row);

    @Override
    public int getPageSize() {
        return PAGE_SIZE;
    }

    @Override
    public void onScroll(int firstRow) {
        for (int i = 0; i < getUnfilteredItemCount(); i++) {
            disableSlot(i);
        }
        super.onScroll(firstRow);
    }

    @Override
    protected void enableElementAt(int row, int elementIndex, A element) {
        super.enableElementAt(row, elementIndex, element);
        enableSlot(elementIndex, row);
    }

    @Override
    public boolean stillValid(Player player) {
        return PartHelpers.canInteractWith(getTarget(), player, this.partContainer);
    }

    public ItemStack writeAspectInfo(boolean generateId, ItemStack itemStack, Level level, final IAspect aspect) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        return registry.writeVariableFacadeItem(generateId, itemStack, Aspects.REGISTRY, new IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IAspectVariableFacade>() {
            @Override
            public IAspectVariableFacade create(boolean generateId) {
                return new AspectVariableFacade(generateId, getPartState().getId(), aspect);
            }

            @Override
            public IAspectVariableFacade create(int id) {
                return new AspectVariableFacade(id, getPartState().getId(), aspect);
            }
        }, level, null, null);
    }

}
