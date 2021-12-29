package org.cyclops.integrateddynamics.capability.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.integrateddynamics.api.block.cable.ICable;

/**
 * Default implementation of {@link ICable}.
 * @author rubensworks
 */
public abstract class CableTile<T extends CyclopsBlockEntity> extends CableDefault {

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
    protected Level getLevel() {
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
