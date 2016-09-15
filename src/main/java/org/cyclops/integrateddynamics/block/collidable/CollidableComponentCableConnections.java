package org.cyclops.integrateddynamics.block.collidable;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Collidable component for the connections of a cable.
 * @author rubensworks
 */
public class CollidableComponentCableConnections extends CollidableComponentCableCenter {

    private AxisAlignedBB getCableBoundingBoxWithPart(World world, BlockPos pos, EnumFacing side) {
        if (side == null) {
            return BlockCable.CABLE_CENTER_BOUNDINGBOX;
        } else {
            IPartContainer partContainer = PartHelpers.getPartContainer(world, pos);
            return partContainer != null ? partContainer.getPart(side).getPartRenderPosition().getSidedCableBoundingBox(side) : BlockCable.NULL_AABB;
        }
    }

    @Override
    public Collection<EnumFacing> getPossiblePositions() {
        return Arrays.asList(EnumFacing.VALUES);
    }

    @Override
    public boolean isActive(BlockCable block, World world, BlockPos pos, EnumFacing position) {
        return super.isActive(block, world, pos, position)
                && (CableHelpers.isCableConnected(world, pos, position)
                || PartHelpers.getPartContainer(world, pos).hasPart(position));
    }

    @Override
    public List<AxisAlignedBB> getBounds(BlockCable block, World world, BlockPos pos, EnumFacing position) {
        return Collections.singletonList(CableHelpers.isCableConnected(world, pos, position)
                ? block.getCableBoundingBox(position) : getCableBoundingBoxWithPart(world, pos, position));
    }

}
