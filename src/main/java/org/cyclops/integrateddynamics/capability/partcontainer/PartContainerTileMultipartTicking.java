package org.cyclops.integrateddynamics.capability.partcontainer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.block.BlockRayTraceResultComponent;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import javax.annotation.Nullable;

/**
 * Implementation of an {@link IPartContainer} for a part entity.
 * @author rubensworks
 */
public class PartContainerTileMultipartTicking extends PartContainerDefault {

    private final TileMultipartTicking tile;

    public PartContainerTileMultipartTicking(TileMultipartTicking tile) {
        this.tile = tile;
    }

    protected TileMultipartTicking getTile() {
        return tile;
    }

    @Override
    protected void markDirty() {
        getTile().markDirty();
    }

    @Override
    protected void sendUpdate() {
        getTile().sendUpdate();
    }

    @Override
    protected World getWorld() {
        return getTile().getWorld();
    }

    @Override
    protected BlockPos getPos() {
        return getTile().getPos();
    }

    @Override
    protected INetwork getNetwork() {
        return getTile().getNetwork();
    }

    @Nullable
    @Override
    public Direction getWatchingSide(World world, BlockPos pos, PlayerEntity player) {
        BlockRayTraceResultComponent rayTraceResult = ((BlockCable) world.getBlockState(pos).getBlock())
                .getSelectedShape(world.getBlockState(pos), world, pos, ISelectionContext.forEntity(player))
                .rayTrace(pos, player);
        if(rayTraceResult != null) {
            return rayTraceResult.getFace();
        }
        return null;
    }
}
