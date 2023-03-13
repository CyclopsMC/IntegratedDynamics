package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.PartStateException;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.network.PartNetworkElement;

import javax.annotation.Nullable;
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
    private final Level world;

    private final int lastUpdateValueId;
    private final int lastPriorityValueId;
    private final int lastChannelValueId;
    private final int lastSideValueId;
    private final int lastMinUpdateValueId;

    public ContainerPartSettings(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory, new SimpleContainer(0),
                PartHelpers.readPartTarget(packetBuffer), Optional.empty(), PartHelpers.readPart(packetBuffer));
    }

    public ContainerPartSettings(int id, Inventory playerInventory, Container inventory,
                                 PartTarget target, Optional<IPartContainer> partContainer, IPartType partType) {
      this(RegistryEntries.CONTAINER_PART_SETTINGS, id, playerInventory, inventory, target, partContainer, partType);
    }

    public ContainerPartSettings(@Nullable MenuType<?> type, int id, Inventory playerInventory, Container inventory,
                                 PartTarget target, Optional<IPartContainer> partContainer, IPartType partType) {
        super(type, id, playerInventory, inventory);
        this.target = target;
        this.partContainer = partContainer;
        this.partType = partType;
        this.world = player.getCommandSenderWorld();

        addPlayerInventory(player.getInventory(), 27, getPlayerInventoryOffsetY());

        lastUpdateValueId = getNextValueId();
        lastPriorityValueId = getNextValueId();
        lastChannelValueId = getNextValueId();
        lastSideValueId = getNextValueId();
        lastMinUpdateValueId = getNextValueId();

        putButtonAction(ContainerPartSettings.BUTTON_SAVE, (s, containerExtended) -> {
            if(!world.isClientSide()) {
                PartHelpers.openContainerPart((ServerPlayer) player, target.getCenter(), getPartType());
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

    public int getLastMinUpdateValueId() {
        return lastMinUpdateValueId;
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
        ValueNotifierHelpers.setValue(this, lastMinUpdateValueId, getPartType().getMinimumUpdateInterval(getPartState()));
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

    public int getLastMinUpdateValue() {
        return ValueNotifierHelpers.getValueInt(this, lastMinUpdateValueId);
    }

    public IPartState getPartState() {
        return partContainer.get().getPartState(getTarget().getCenter().getSide());
    }

    @Override
    public boolean stillValid(Player player) {
        return PartHelpers.canInteractWith(getTarget(), player, this.partContainer.get());
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public void onUpdate(int valueId, CompoundTag value) {
        super.onUpdate(valueId, value);
        try {
            if(!world.isClientSide()) {
                PartTarget target = getTarget();
                DimPos dimPos = target.getCenter().getPos();
                INetwork network = NetworkHelpers.getNetworkChecked(dimPos.getLevel(true), dimPos.getBlockPos(), target.getCenter().getSide());
                updatePartSettings();
                PartNetworkElement networkElement = new PartNetworkElement<>(getPartType(), target.getCenter());
                network.setPriorityAndChannel(networkElement, getLastPriorityValue(), getLastChannelValue());
            }
        } catch (PartStateException e) {
            player.closeContainer();
        }
    }

    protected void updatePartSettings() {
        getPartType().setUpdateInterval(getPartState(), getLastUpdateValue());
        Direction targetSide = getLastSideValue() >= 0 ? Direction.values()[getLastSideValue()] : null;
        getPartType().setTargetSideOverride(getPartState(), targetSide);
    }
}
