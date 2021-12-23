package org.cyclops.integrateddynamics.capability.cable;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.integrateddynamics.api.block.cable.ICable;

/**
 * Default implementation of {@link ICable}.
 * @author rubensworks
 */
public abstract class CableTile<T extends CyclopsTileEntity> extends CableDefault {

    protected final T tile;

    public CableTile(T tile) {
        this.tile = tile;
    }

    @Override
    protected void setChanged() {
        tile.setChanged();
    }

    @Override
    protected void sendUpdate() {
        tile.sendUpdate();
    }

    @Override
    protected World getLevel() {
        return tile.getLevel();
    }

    @Override
    protected BlockPos getPos() {
        return tile.getBlockPos();
    }

    @Override
    public void destroy() {
        getLevel().setBlock(getPos(), Blocks.AIR.defaultBlockState(), 3);
    }
}
