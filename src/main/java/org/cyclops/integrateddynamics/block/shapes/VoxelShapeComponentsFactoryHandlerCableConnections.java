package org.cyclops.integrateddynamics.block.shapes;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
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
            Shapes.create(new AABB(CableModel.MIN, 0, CableModel.MIN, CableModel.MAX, CableModel.MIN, CableModel.MAX)), // DOWN
            Shapes.create(new AABB(CableModel.MIN, CableModel.MAX, CableModel.MIN, CableModel.MAX, 1, CableModel.MAX)), // UP
            Shapes.create(new AABB(CableModel.MIN, CableModel.MIN, 0, CableModel.MAX, CableModel.MAX, CableModel.MIN)), // NORTH
            Shapes.create(new AABB(CableModel.MIN, CableModel.MAX, CableModel.MAX, CableModel.MAX, CableModel.MIN, 1)), // SOUTH
            Shapes.create(new AABB(0, CableModel.MIN, CableModel.MIN, CableModel.MIN, CableModel.MAX, CableModel.MAX)), // WEST
            Shapes.create(new AABB(CableModel.MAX, CableModel.MIN, CableModel.MIN, 1, CableModel.MAX, CableModel.MAX)) // EAST
    );

    @Override
    public Collection<VoxelShapeComponents.IComponent> createComponents(BlockState blockState, BlockGetter world, BlockPos blockPos) {
        Collection<VoxelShapeComponents.IComponent> components = Lists.newArrayList();
        if (CableHelpers.isNoFakeCable((Level) world, blockPos, null)) {
            for (Direction direction : Direction.values()) {
                IPartContainer partContainer = null;
                if (CableHelpers.isCableConnected((Level) world, blockPos, direction) ||
                        (partContainer = PartHelpers.getPartContainer((Level) world, blockPos, direction).orElse(null)) != null
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
        public String getStateId(BlockState blockState, BlockGetter world, BlockPos blockPos) {
            return "conn(" + direction.ordinal() + ")";
        }

        @Override
        public VoxelShape getShape(BlockState blockState, BlockGetter world, BlockPos blockPos, CollisionContext selectionContext) {
            if (partContainer == null) { // equivalent to: CableHelpers.isCableConnected(world, blockPos, direction)
                return BOUNDS.get(direction);
            }
            IPartType part = partContainer.getPart(direction);
            if (part == null) {
                // Can happen rarely on client desyncs
                return BOUNDS.get(direction);
            }
            return part.getPartRenderPosition().getSidedCableBoundingBox(direction);
        }

        @Override
        public InteractionResult onBlockActivated(BlockState state, Level world, BlockPos blockPos, Player player, InteractionHand hand, BlockRayTraceResultComponent hit) {
            ItemStack heldItem = player.getItemInHand(hand);
            InteractionResult actionResult = CableHelpers.onCableActivated(world, blockPos, state, player, heldItem, hit.getDirection(), direction);
            if(actionResult.consumesAction()) {
                return actionResult;
            }
            return InteractionResult.PASS;
        }

    }

}
