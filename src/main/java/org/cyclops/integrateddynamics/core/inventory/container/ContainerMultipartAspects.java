package org.cyclops.integrateddynamics.core.inventory.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import org.cyclops.cyclopscore.helper.L10NHelpers;
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
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Container for parts.
 *
 * @author rubensworks
 */
public abstract class ContainerMultipartAspects<P extends IPartType<P, S>, S extends IPartState<P>, A extends IAspect>
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
            return pattern.matcher(L10NHelpers.localize(item.getTranslationKey()).toLowerCase(Locale.ENGLISH)).matches();
        });
        this.target = target;
        this.partContainer = partContainer.orElseGet(() -> PartHelpers.getPartContainerChecked(target.getCenter()));
        this.partType = partType;
        this.world = player.getCommandSenderWorld();

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

        try {
            if (!player.level().isClientSide()) {
                for (Map.Entry<IAspect, Integer> entry : this.aspectPropertyValueIds.entrySet()) {
                    ValueNotifierHelpers.setValue(this, entry.getValue(), getModifiedAspectPropertyValues(entry.getKey()));
                }
            }
        } catch (PartStateException e) {
            player.closeContainer();
        }
    }

    /**
     * Determine the values of all properties of the given aspect that deviate from their default value.
     *
     * The returned list has one entry for each of the aspect's property types,
     * in the order of {@link IAspect#getPropertyTypes()}.
     * Properties that still have their default value are represented by an empty component.
     *
     * @param aspect An aspect that has properties.
     * @return The modified property values, in the order of the aspect's property types.
     */
    @SuppressWarnings("unchecked")
    protected List<MutableComponent> getModifiedAspectPropertyValues(IAspect aspect) {
        IAspectProperties defaultProperties = aspect.getDefaultProperties();
        IAspectProperties properties = getPartState().getAspectProperties(aspect);
        if (properties == null) {
            properties = defaultProperties;
        }

        List<MutableComponent> values = Lists.newArrayList();
        for (IAspectPropertyTypeInstance property : (Collection<IAspectPropertyTypeInstance>) aspect.getPropertyTypes()) {
            IValue value = properties.getValue(property);
            IValue defaultValue = defaultProperties.getValue(property);
            if (value == null || ValueHelpers.areValuesEqual(value, defaultValue)) {
                values.add(Component.empty());
            } else {
                IValueType valueType = value.getType();
                MutableComponent compactValue = valueType.toCompactString(value);
                values.add(compactValue == null
                        ? Component.empty()
                        : compactValue.withStyle(valueType.getDisplayColorFormat()));
            }
        }
        return values;
    }

    /**
     * Get the modified property values of the given aspect, as synced from the server.
     * @param aspect An aspect.
     * @return The modified property values, in the order of {@link IAspect#getPropertyTypes()},
     *         or null if they are unknown.
     */
    @Nullable
    public List<MutableComponent> getModifiedAspectPropertyValuesSynced(IAspect aspect) {
        Integer valueId = this.aspectPropertyValueIds.get(aspect);
        if (valueId == null) {
            return null;
        }
        return ValueNotifierHelpers.getValueTextComponentList(this, valueId);
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
