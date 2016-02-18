package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.experimental.Delegate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidContainerRegistry;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.TankInventoryTileEntity;
import org.cyclops.integrateddynamics.block.BlockSqueezer;

/**
 * A tile entity for squeezing stuff.
 * @author rubensworks
 */
public class TileSqueezer extends TankInventoryTileEntity implements CyclopsTileEntity.ITickingTile {

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    @Getter
    private int itemHeight = 1;

    public TileSqueezer() {
        super(1, "squeezerInventory", 1, FluidContainerRegistry.BUCKET_VOLUME, "squeezerTank");

        addSlotsToSide(EnumFacing.UP, Sets.newHashSet(1));
        addSlotsToSide(EnumFacing.DOWN, Sets.newHashSet(1));
        addSlotsToSide(EnumFacing.NORTH, Sets.newHashSet(1));
        addSlotsToSide(EnumFacing.SOUTH, Sets.newHashSet(1));
        addSlotsToSide(EnumFacing.WEST, Sets.newHashSet(1));
        addSlotsToSide(EnumFacing.EAST, Sets.newHashSet(1));
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack, EnumFacing side) {
        return getWorld().getBlockState(getPos()).getValue(BlockSqueezer.HEIGHT) == 1 && super.canInsertItem(slot, itemStack, side);
    }

    @Override
    public void setInventorySlotContents(int slotId, ItemStack itemstack) {
        super.setInventorySlotContents(slotId, itemstack);
        if(itemstack == null) {
            itemHeight = 1;
        }
        sendUpdate();
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
        sendUpdate();
    }
}
