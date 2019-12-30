package org.cyclops.integrateddynamics.core.inventory.container;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
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
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;

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

    private final Optional<PartTarget> target;
    private final Optional<IPartContainer> partContainer;
    private final Optional<IPartType> partType;
    private final World world;
    private final IAspect<?, ?> aspect;

    private final BiMap<Integer, IAspectPropertyTypeInstance> propertyIds = HashBiMap.create();

    public ContainerAspectSettings(int id, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
        this(id, playerInventory, new Inventory(0),
                Optional.empty(), Optional.empty(), Optional.empty(), readAspect(packetBuffer));
    }

    protected static IAspect<?, ?> readAspect(PacketBuffer packetBuffer) {
        String name = packetBuffer.readString();
        return Objects.requireNonNull(AspectRegistry.getInstance().getAspect(name),
                String.format("Could not find an aspect by name %s", name));
    }

    public ContainerAspectSettings(int id, PlayerInventory playerInventory, IInventory inventory,
                                   Optional<PartTarget> target, Optional<IPartContainer> partContainer,
                                   Optional<IPartType> partType, IAspect<?, ?> aspect) {
        super(RegistryEntries.CONTAINER_ASPECT_SETTINGS, id, playerInventory, inventory);
        this.target = target;
        this.partType = partType;
        this.partContainer = partContainer;
        this.world = player.getEntityWorld();
        this.aspect = aspect;

        addPlayerInventory(player.inventory, 8, 131);

        for(IAspectPropertyTypeInstance property : aspect.getPropertyTypes()) {
            propertyIds.put(getNextValueId(), property);
        }

        putButtonAction(ContainerAspectSettings.BUTTON_EXIT, (s, containerExtended) -> {
            if (!world.isRemote()) {
                PartHelpers.openContainerPart((ServerPlayerEntity) playerInventory.player, getTarget().get().getCenter(), getPartType().get());
            }
        });
    }

    public BiMap<Integer, IAspectPropertyTypeInstance> getPropertyIds() {
        return propertyIds;
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

    @Override
    protected void initializeValues() {
        super.initializeValues();
        IAspectProperties properties = aspect.getProperties(getPartType().get(), getTarget().get(), getPartState().get());
        for(IAspectPropertyTypeInstance property : aspect.getPropertyTypes()) {
            setValue(property, properties.getValue(property));
        }
    }

    public void setValue(IAspectPropertyTypeInstance property, IValue value) {
        ValueNotifierHelpers.setValue(this, propertyIds.inverse().get(property), ValueHelpers.serializeRaw(value));
    }

    public Optional<IPartState> getPartState() {
        return partContainer.map(p -> p.getPartState(getTarget().get().getCenter().getSide()));
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return true;
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    public <T extends IValueType<V>, V extends IValue> V getPropertyValue(IAspectPropertyTypeInstance<T, V> property) {
        if(propertyIds.containsValue(property)) {
            INBT value = ValueNotifierHelpers.getValueNbt(this, propertyIds.inverse().get(property));
            if(value != null) {
                return ValueHelpers.deserializeRaw(property.getType(), value);
            }
        }
        return null;
    }

    @Override
    public void onUpdate(int valueId, CompoundNBT value) {
        super.onUpdate(valueId, value);
        if(!world.isRemote()) {
            IAspectPropertyTypeInstance property = propertyIds.get(valueId);
            if (property != null) {
                IPartType partType = getPartType().get();
                PartTarget target = getTarget().get();
                IPartState partState = getPartState().get();

                IAspectProperties aspectProperties = aspect.getProperties(partType, target, partState);
                aspectProperties = aspectProperties.clone();
                IValue trueValue = ValueHelpers.deserializeRaw(property.getType(), value.get(ValueNotifierHelpers.KEY));
                aspectProperties.setValue(property, trueValue);
                aspect.setProperties(partType, target, partState, aspectProperties);

                // Changing the properties might cause some erroring variables to become valid again, so trigger an update.
                NetworkHelpers.getNetwork(target.getCenter())
                        .ifPresent(network -> network.getEventBus().post(new VariableContentsUpdatedEvent(network)));
            }
        }
    }
}
