package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import lombok.experimental.Delegate;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidContainerRegistry;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.TankInventoryTileEntity;

/**
 * A tile entity for squeezing stuff.
 * @author rubensworks
 */
public class TileSqueezer extends TankInventoryTileEntity implements CyclopsTileEntity.ITickingTile {

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

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
}
