package org.cyclops.integrateddynamics.capability.partcontainer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.block.BlockRayTraceResultComponent;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;

import javax.annotation.Nullable;

/**
 * Implementation of an {@link IPartContainer} for a part entity.
 * @author rubensworks
 */
public class PartContainerTileMultipartTicking extends PartContainerDefault {

    private final BlockEntityMultipartTicking tile;

    public PartContainerTileMultipartTicking(BlockEntityMultipartTicking tile) {
        this.tile = tile;
    }

    protected BlockEntityMultipartTicking getTile() {
        return tile;
    }

    @Override
    protected void setChanged() {
        getTile().setChanged();
    }

    @Override
    protected void sendUpdate() {
        getTile().sendUpdate();
    }

    @Override
    protected Level getLevel() {
        return getTile().getLevel();
    }

    @Override
    protected BlockPos getPos() {
        return getTile().getBlockPos();
    }

    @Override
    protected INetwork getNetwork() {
        return getTile().getNetwork();
    }

    @Nullable
    @Override
    public Direction getWatchingSide(Level world, BlockPos pos, Player player) {
        BlockRayTraceResultComponent rayTraceResult = ((BlockCable) world.getBlockState(pos).getBlock())
                .getSelectedShape(world.getBlockState(pos), world, pos, CollisionContext.of(player))
                .rayTrace(pos, player);
        if(rayTraceResult != null) {
            return rayTraceResult.getComponent().getRaytraceDirection();
        }
        return null;
    }
}
