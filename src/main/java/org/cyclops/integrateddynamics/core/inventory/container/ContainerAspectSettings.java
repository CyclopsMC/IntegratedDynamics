package org.cyclops.integrateddynamics.core.inventory.container;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.part.PartStateAspectVariablesHandler;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Container for aspect settings.
 * @author rubensworks
 */
public class ContainerAspectSettings extends InventoryContainer {

    public static final String BUTTON_EXIT = "button_exit";
    public static final int BUTTON_SETTINGS = 1;
    private static final int PAGE_SIZE = 3;

    /**
     * The position of the variable slot of the active property.
     */
    public static final int VARIABLE_SLOT_X = 71;
    public static final int VARIABLE_SLOT_Y = 110;

    private final Optional<PartTarget> target;
    private final Optional<IPartContainer> partContainer;
    private final Optional<IPartType> partType;
    private final Level world;
    private final IAspect<?, ?> aspect;

    private final BiMap<Integer, IAspectPropertyTypeInstance> propertyIds = HashBiMap.create();
    private final List<IAspectPropertyTypeInstance> propertyTypes;
    private final List<Integer> propertyVariableSlotErrorIds;
    private int activePropertyIndex = 0;

    private final SimpleInventory variablesInventory;
    private boolean dirtyInv = false;

    public ContainerAspectSettings(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory, new SimpleContainer(0),
                Optional.empty(), Optional.empty(), Optional.empty(), readAspect(packetBuffer));
    }

    protected static IAspect<?, ?> readAspect(FriendlyByteBuf packetBuffer) {
        String name = packetBuffer.readUtf();
        return Objects.requireNonNull(AspectRegistry.getInstance().getAspect(ResourceLocation.parse(name)),
                String.format("Could not find an aspect by name %s", name));
    }

    public ContainerAspectSettings(int id, Inventory playerInventory, Container inventory,
                                   Optional<PartTarget> target, Optional<IPartContainer> partContainer,
                                   Optional<IPartType> partType, IAspect<?, ?> aspect) {
        super(RegistryEntries.CONTAINER_ASPECT_SETTINGS.get(), id, playerInventory, inventory);
        this.target = target;
        this.partType = partType;
        this.partContainer = partContainer;
        this.world = player.getCommandSenderWorld();
        this.aspect = aspect;
        this.propertyTypes = PartStateAspectVariablesHandler.getPropertyTypes(aspect);

        for(IAspectPropertyTypeInstance property : aspect.getPropertyTypes()) {
            propertyIds.put(getNextValueId(), property);
        }
        this.propertyVariableSlotErrorIds = Lists.newArrayList();
        for (int i = 0; i < this.propertyTypes.size(); i++) {
            this.propertyVariableSlotErrorIds.add(getNextValueId());
        }
        // Add one variable slot for each property.
        // All of them are placed at the same position,
        // as only the slot of the active property is visible at any time.
        this.variablesInventory = new SimpleInventory(this.propertyTypes.size(), 1);
        this.variablesInventory.addDirtyMarkListener(() -> dirtyInv = true);
        if (!world.isClientSide()) {
            getPartState().ifPresent(partState -> partState
                    .loadInventoryNamed(PartStateAspectVariablesHandler.getInventoryName(aspect), this.variablesInventory));
        }
        for (int i = 0; i < this.propertyTypes.size(); i++) {
            addSlot(new SlotVariableProperty(this.variablesInventory, i, VARIABLE_SLOT_X, VARIABLE_SLOT_Y));
        }

        addPlayerInventory(player.getInventory(), 8, 155);

        putButtonAction(ContainerAspectSettings.BUTTON_EXIT, (s, containerExtended) -> {
            if (!world.isClientSide()) {
                PartHelpers.openContainerPart((ServerPlayer) playerInventory.player, getTarget().get().getCenter(), getPartType().get());
            }
        });
    }

    public BiMap<Integer, IAspectPropertyTypeInstance> getPropertyIds() {
        return propertyIds;
    }

    public List<IAspectPropertyTypeInstance> getPropertyTypes() {
        return propertyTypes;
    }

    public Optional<IPartType> getPartType() {
        return partType;
    }

    public IAspect getAspect() {
        return aspect;
    }

    public Optional<PartTarget> getTarget() {
        return target;
    }

    /**
     * @return The index of the property whose variable slot is currently visible.
     */
    public int getActivePropertyIndex() {
        return activePropertyIndex;
    }

    /**
     * Indicate which property is currently being configured, so that its variable slot becomes visible.
     * This must be called on both sides, as the client informs the server via {@link #clickMenuButton(Player, int)}.
     * @param index The property index.
     */
    public void setActivePropertyIndex(int index) {
        this.activePropertyIndex = Math.max(0, Math.min(this.propertyTypes.size() - 1, index));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < this.propertyTypes.size()) {
            setActivePropertyIndex(id);
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    /**
     * @param index A property index.
     * @return If the variable slot of the given property holds a variable.
     */
    public boolean isPropertyVariableFilled(int index) {
        return index >= 0 && index < this.variablesInventory.getContainerSize()
                && !this.variablesInventory.getItem(index).isEmpty();
    }

    /**
     * @param index A property index.
     * @return The error inside the variable slot of the given property, or null if there is no error.
     */
    @Nullable
    public Component getPropertyVariableError(int index) {
        if (index < 0 || index >= this.propertyVariableSlotErrorIds.size()) {
            return null;
        }
        // An empty component is used to indicate the absence of an error,
        // as null values are not sent over to the client.
        Component error = ValueNotifierHelpers.getValueTextComponent(this, this.propertyVariableSlotErrorIds.get(index));
        return error == null || error.getString().isEmpty() ? null : error;
    }

    @Override
    protected void initializeValues() {
        super.initializeValues();
        IAspectProperties properties = aspect.getStaticProperties(getPartType().get(), getTarget().get(), getPartState().get());
        for(IAspectPropertyTypeInstance property : aspect.getPropertyTypes()) {
            setValue(ValueDeseralizationContext.ofAllEnabled(), property, properties.getValue(property));
        }
    }

    public void setValue(ValueDeseralizationContext valueDeseralizationContext, IAspectPropertyTypeInstance property, IValue value) {
        ValueNotifierHelpers.setValue(this, propertyIds.inverse().get(property), ValueHelpers.serializeRaw(valueDeseralizationContext, value));
    }

    public Optional<IPartState> getPartState() {
        return partContainer.map(p -> p.getPartState(getTarget().get().getCenter().getSide()));
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    public <T extends IValueType<V>, V extends IValue> V getPropertyValue(ValueDeseralizationContext valueDeseralizationContext, IAspectPropertyTypeInstance<T, V> property) {
        if(propertyIds.containsValue(property)) {
            Tag value = ValueNotifierHelpers.getValueNbt(this, propertyIds.inverse().get(property));
            if(value != null) {
                return ValueHelpers.deserializeRaw(valueDeseralizationContext, property.getType(), value);
            }
        }
        return null;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (!world.isClientSide()) {
            Optional<IPartState> partStateOptional = getPartState();
            if (partStateOptional.isEmpty()) {
                return;
            }
            IPartState partState = partStateOptional.get();

            saveVariablesInventory(partState);

            for (int i = 0; i < this.propertyVariableSlotErrorIds.size(); i++) {
                MutableComponent error = partState.getAspectVariableError(aspect, i);
                ValueNotifierHelpers.setValue(this, this.propertyVariableSlotErrorIds.get(i),
                        error == null ? Component.empty() : error);
            }
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Make sure that any last-tick changes to the variable slots are persisted.
        saveVariablesInventory();
    }

    /**
     * Persist any changes to the variable slots into the part state.
     */
    public void saveVariablesInventory() {
        if (!world.isClientSide()) {
            getPartState().ifPresent(this::saveVariablesInventory);
        }
    }

    protected void saveVariablesInventory(IPartState partState) {
        if (this.dirtyInv) {
            this.dirtyInv = false;
            partState.saveInventoryNamed(PartStateAspectVariablesHandler.getInventoryName(aspect), this.variablesInventory);
            getPartType().ifPresent(partType -> partType.onAspectVariablesChanged(getTarget().get(), partState));

            // Changing the variables might cause some erroring variables to become valid again, so trigger an update.
            NetworkHelpers.getNetwork(getTarget().get().getCenter())
                    .ifPresent(network -> network.getEventBus().post(new VariableContentsUpdatedEvent(network)));
        }
    }

    @Override
    public void onUpdate(int valueId, CompoundTag value) {
        super.onUpdate(valueId, value);
        if(!world.isClientSide()) {
            IAspectPropertyTypeInstance property = propertyIds.get(valueId);
            if (property != null) {
                IPartType partType = getPartType().get();
                PartTarget target = getTarget().get();
                IPartState partState = getPartState().get();

                IAspectProperties aspectProperties = aspect.getStaticProperties(partType, target, partState);
                aspectProperties = aspectProperties.clone();
                IValue trueValue = ValueHelpers.deserializeRaw(ValueDeseralizationContext.of(world), property.getType(), value.get(ValueNotifierHelpers.KEY));
                aspectProperties.setValue(property, trueValue);
                aspect.setProperties(partType, target, partState, aspectProperties);

                // Changing the properties might cause some erroring variables to become valid again, so trigger an update.
                NetworkHelpers.getNetwork(target.getCenter())
                        .ifPresent(network -> network.getEventBus().post(new VariableContentsUpdatedEvent(network)));
            }
        }
    }

    /**
     * A variable slot that is only visible when its property is the active property.
     */
    public class SlotVariableProperty extends SlotVariable {

        private final int propertyIndex;

        public SlotVariableProperty(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
            this.propertyIndex = index;
        }

        @Override
        public boolean isActive() {
            return super.isActive() && getActivePropertyIndex() == this.propertyIndex;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isActive() && super.mayPlace(stack);
        }
    }
}
