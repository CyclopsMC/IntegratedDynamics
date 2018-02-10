package org.cyclops.integrateddynamics.core.inventory.container;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.cyclopscore.inventory.container.ExtendedInventoryContainer;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.cyclopscore.inventory.container.button.IButtonActionServer;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.PartStateException;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.client.gui.ExtendedGuiHandler;
import org.cyclops.integrateddynamics.core.client.gui.container.GuiPartSettings;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.network.PartNetworkElement;

/**
 * Container for part settings.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ContainerPartSettings extends ExtendedInventoryContainer {

    public static final int BUTTON_SETTINGS = 1;
    private static final int PAGE_SIZE = 3;

    private final PartTarget target;
    private final IPartContainer partContainer;
    private final IPartType partType;
    private final World world;
    private final BlockPos pos;

    private final int lastUpdateValueId;
    private final int lastPriorityValueId;
    private final int lastChannelValueId;
    private final int lastSideValueId;

    /**
     * Make a new instance.
     * @param target The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The part type.
     */
    public ContainerPartSettings(final EntityPlayer player, PartTarget target, IPartContainer partContainer, IPartType partType) {
        super(player.inventory, (IGuiContainerProvider) partType);
        this.target = target;
        this.partContainer = partContainer;
        this.partType = partType;
        this.world = player.getEntityWorld();
        this.pos = player.getPosition();

        addPlayerInventory(player.inventory, 27, getPlayerInventoryOffsetY());

        lastUpdateValueId = getNextValueId();
        lastPriorityValueId = getNextValueId();
        lastChannelValueId = getNextValueId();
        lastSideValueId = getNextValueId();

        putButtonAction(GuiPartSettings.BUTTON_SAVE, new IButtonActionServer<InventoryContainer>() {
            @Override
            public void onAction(int buttonId, InventoryContainer container) {
                if (!(getPartType() instanceof IGuiContainerProvider) || ((IGuiContainerProvider) getPartType()).getContainer() != ContainerPartSettings.this.getClass()) {
                    if(!world.isRemote) {
                        IntegratedDynamics._instance.getGuiHandler().setTemporaryData(ExtendedGuiHandler.PART, getTarget().getCenter().getSide());
                        BlockPos pos = getTarget().getCenter().getPos().getBlockPos();
                        player.openGui(IntegratedDynamics._instance.getModId(), ((IGuiContainerProvider) getPartType()).getGuiID(), world, pos.getX(), pos.getY(), pos.getZ());
                    }
                } else {
                    player.closeScreen();
                }
            }
        });
    }

    protected int getPlayerInventoryOffsetY() {
        return 107;
    }

    @Override
    protected void initializeValues() {
        ValueNotifierHelpers.setValue(this, lastUpdateValueId, getPartType().getUpdateInterval(getPartState()));
        ValueNotifierHelpers.setValue(this, lastPriorityValueId, getPartType().getPriority(getPartState()));
        ValueNotifierHelpers.setValue(this, lastChannelValueId, getPartType().getChannel(getPartState()));
        EnumFacing targetSide = getPartType().getTargetSideOverride(getPartState());
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
        return partContainer.getPartState(getTarget().getCenter().getSide());
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return PartHelpers.canInteractWith(getTarget(), player, this.partContainer);
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public void onUpdate(int valueId, NBTTagCompound value) {
        super.onUpdate(valueId, value);
        try {
            if(!world.isRemote) {
                DimPos dimPos = getTarget().getCenter().getPos();
                INetwork network = NetworkHelpers.getNetwork(dimPos.getWorld(), dimPos.getBlockPos(), getTarget().getCenter().getSide());
                PartTarget target = getTarget();
                updatePartSettings();
                if (getPartState().getTargetSideOverride() != null) {
                    target = target.forTargetSide(getPartState().getTargetSideOverride());
                }
                PartNetworkElement networkElement = new PartNetworkElement(getPartType(), target);
                network.setPriorityAndChannel(networkElement, getLastPriorityValue(), getLastChannelValue());
            }
        } catch (PartStateException e) {
            player.closeScreen();
        }
    }

    protected void updatePartSettings() {
        getPartType().setUpdateInterval(getPartState(), getLastUpdateValue());
        EnumFacing targetSide = getLastSideValue() >= 0 ? EnumFacing.VALUES[getLastSideValue()] : null;
        getPartType().setTargetSideOverride(getPartState(), targetSide);
    }
}
