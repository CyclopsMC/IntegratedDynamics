package org.cyclops.integrateddynamics.core.inventory.container;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.PartStateException;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.network.PartNetworkElement;
import org.cyclops.integrateddynamics.core.part.PartTypeRegistry;

import java.util.Objects;
import java.util.Optional;

/**
 * Container for part settings.
 * @author rubensworks
 */
public class ContainerPartSettings extends InventoryContainer {

    public static final String BUTTON_SAVE = "button_save";
    public static final String BUTTON_SETTINGS = "button_settings";
    private static final int PAGE_SIZE = 3;

    private final PartTarget target;
    private final Optional<IPartContainer> partContainer;
    private final IPartType partType;
    private final World world;

    private final int lastUpdateValueId;
    private final int lastPriorityValueId;
    private final int lastChannelValueId;
    private final int lastSideValueId;

    public ContainerPartSettings(int id, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
        this(id, playerInventory, new Inventory(0),
                PartHelpers.readPartTarget(packetBuffer), Optional.empty(), PartHelpers.readPart(packetBuffer));
    }

    public ContainerPartSettings(int id, PlayerInventory playerInventory, IInventory inventory,
                                 PartTarget target, Optional<IPartContainer> partContainer, IPartType partType) {
        super(RegistryEntries.CONTAINER_PART_SETTINGS, id, playerInventory, inventory);
        this.target = target;
        this.partContainer = partContainer;
        this.partType = partType;
        this.world = player.getEntityWorld();

        addPlayerInventory(player.inventory, 27, getPlayerInventoryOffsetY());

        lastUpdateValueId = getNextValueId();
        lastPriorityValueId = getNextValueId();
        lastChannelValueId = getNextValueId();
        lastSideValueId = getNextValueId();

        putButtonAction(ContainerPartSettings.BUTTON_SAVE, (s, containerExtended) -> {
            if(!world.isRemote()) {
                PartHelpers.openContainerPart((ServerPlayerEntity) player, target.getCenter(), getPartType());
            }
        });
    }

    public IPartType getPartType() {
        return partType;
    }

    public PartTarget getTarget() {
        return target;
    }

    public int getLastChannelValueId() {
        return lastChannelValueId;
    }

    public int getLastPriorityValueId() {
        return lastPriorityValueId;
    }

    public int getLastSideValueId() {
        return lastSideValueId;
    }

    public int getLastUpdateValueId() {
        return lastUpdateValueId;
    }

    protected int getPlayerInventoryOffsetY() {
        return 107;
    }

    @Override
    protected void initializeValues() {
        ValueNotifierHelpers.setValue(this, lastUpdateValueId, getPartType().getUpdateInterval(getPartState()));
        ValueNotifierHelpers.setValue(this, lastPriorityValueId, getPartType().getPriority(getPartState()));
        ValueNotifierHelpers.setValue(this, lastChannelValueId, getPartType().getChannel(getPartState()));
        Direction targetSide = getPartType().getTargetSideOverride(getPartState());
        ValueNotifierHelpers.setValue(this, lastSideValueId, targetSide == null ? -1 : targetSide.ordinal());
    }

    public int getLastUpdateValue() {
        return ValueNotifierHelpers.getValueInt(this, lastUpdateValueId);
    }

    public int getLastPriorityValue() {
        return ValueNotifierHelpers.getValueInt(this, lastPriorityValueId);
    }

    public int getLastChannelValue() {
        return ValueNotifierHelpers.getValueInt(this, lastChannelValueId);
    }

    public int getLastSideValue() {
        return ValueNotifierHelpers.getValueInt(this, lastSideValueId);
    }

    public IPartState getPartState() {
        return partContainer.get().getPartState(getTarget().getCenter().getSide());
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return PartHelpers.canInteractWith(getTarget(), player, this.partContainer.get());
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public void onUpdate(int valueId, CompoundNBT value) {
        super.onUpdate(valueId, value);
        try {
            if(!world.isRemote()) {
                PartTarget target = getTarget();
                DimPos dimPos = target.getCenter().getPos();
                INetwork network = NetworkHelpers.getNetworkChecked(dimPos.getWorld(true), dimPos.getBlockPos(), target.getCenter().getSide());
                updatePartSettings();
                if (getPartState().getTargetSideOverride() != null) {
                    target = target.forTargetSide(getPartState().getTargetSideOverride());
                }
                PartNetworkElement networkElement = new PartNetworkElement<>(getPartType(), target);
                network.setPriorityAndChannel(networkElement, getLastPriorityValue(), getLastChannelValue());
            }
        } catch (PartStateException e) {
            player.closeScreen();
        }
    }

    protected void updatePartSettings() {
        getPartType().setUpdateInterval(getPartState(), getLastUpdateValue());
        Direction targetSide = getLastSideValue() >= 0 ? Direction.values()[getLastSideValue()] : null;
        getPartType().setTargetSideOverride(getPartState(), targetSide);
    }
}
