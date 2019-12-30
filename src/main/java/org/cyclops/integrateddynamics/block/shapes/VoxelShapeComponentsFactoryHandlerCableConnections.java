package org.cyclops.integrateddynamics.block.shapes;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.client.model.CableModel;
import org.cyclops.integrateddynamics.core.block.BlockRayTraceResultComponent;
import org.cyclops.integrateddynamics.core.block.VoxelShapeComponents;
import org.cyclops.integrateddynamics.core.block.VoxelShapeComponentsFactory;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Shape handler for cable connections.
 * @author rubensworks
 */
public class VoxelShapeComponentsFactoryHandlerCableConnections implements VoxelShapeComponentsFactory.IHandler {

    private final static EnumFacingMap<VoxelShape> BOUNDS = EnumFacingMap.forAllValues(
            VoxelShapes.create(new AxisAlignedBB(CableModel.MIN, 0, CableModel.MIN, CableModel.MAX, CableModel.MIN, CableModel.MAX)), // DOWN
            VoxelShapes.create(new AxisAlignedBB(CableModel.MIN, CableModel.MAX, CableModel.MIN, CableModel.MAX, 1, CableModel.MAX)), // UP
            VoxelShapes.create(new AxisAlignedBB(CableModel.MIN, CableModel.MIN, 0, CableModel.MAX, CableModel.MAX, CableModel.MIN)), // NORTH
            VoxelShapes.create(new AxisAlignedBB(CableModel.MIN, CableModel.MAX, CableModel.MAX, CableModel.MAX, CableModel.MIN, 1)), // SOUTH
            VoxelShapes.create(new AxisAlignedBB(0, CableModel.MIN, CableModel.MIN, CableModel.MIN, CableModel.MAX, CableModel.MAX)), // WEST
            VoxelShapes.create(new AxisAlignedBB(CableModel.MAX, CableModel.MIN, CableModel.MIN, 1, CableModel.MAX, CableModel.MAX)) // EAST
    );

    @Override
    public Collection<VoxelShapeComponents.IComponent> createComponents(BlockState blockState, IBlockReader world, BlockPos blockPos) {
        Collection<VoxelShapeComponents.IComponent> components = Lists.newArrayList();
        if (CableHelpers.isNoFakeCable(world, blockPos, null)) {
            for (Direction direction : Direction.values()) {
                IPartContainer partContainer = null;
                if (CableHelpers.isCableConnected(world, blockPos, direction) ||
                        (partContainer = PartHelpers.getPartContainer(world, blockPos, direction).orElse(null)) != null
                                && partContainer.hasPart(direction)) {
                    components.add(new Component(direction, partContainer));
                }
            }
        }
        return components;
    }

    public static class Component extends VoxelShapeComponentsFactoryHandlerCableCenter.Component {

        private final Direction direction;
        @Nullable
        private final IPartContainer partContainer;

        public Component(Direction direction, @Nullable IPartContainer partContainer) {
            this.direction = direction;
            this.partContainer = partContainer;
        }

        @Override
        public VoxelShape getShape(BlockState blockState, IBlockReader world, BlockPos blockPos, ISelectionContext selectionContext) {
            if (partContainer == null) { // equivalent to: CableHelpers.isCableConnected(world, blockPos, direction)
                return BOUNDS.get(direction);
            }
            return partContainer.getPart(direction).getPartRenderPosition().getSidedCableBoundingBox(direction);
        }

        @Override
        public boolean onBlockActivated(BlockState state, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockRayTraceResultComponent hit) {
            ItemStack heldItem = player.getHeldItem(hand);
            if(CableHelpers.onCableActivated(world, blockPos, state, player, heldItem, hit.getFace(), direction)) {
                return true;
            }
            return false;
        }

    }

}
