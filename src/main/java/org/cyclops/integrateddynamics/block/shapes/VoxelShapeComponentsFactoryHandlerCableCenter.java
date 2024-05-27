package org.cyclops.integrateddynamics.block.shapes;

import net.minecraft.client.resources.model.BakedModel;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.client.model.CableModel;
import org.cyclops.integrateddynamics.core.block.BlockRayTraceResultComponent;
import org.cyclops.integrateddynamics.core.block.VoxelShapeComponents;
import org.cyclops.integrateddynamics.core.block.VoxelShapeComponentsFactory;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * Shape handler for cable centers.
 * @author rubensworks
 */
public class VoxelShapeComponentsFactoryHandlerCableCenter implements VoxelShapeComponentsFactory.IHandler {

    private static final VoxelShape BOUNDS = Shapes.create(new AABB(
            CableModel.MIN, CableModel.MIN, CableModel.MIN,
            CableModel.MAX, CableModel.MAX, CableModel.MAX));
    private static final VoxelShapeComponentsFactoryHandlerCableCenter.Component COMPONENT = new Component();

    @Override
    public Collection<VoxelShapeComponents.IComponent> createComponents(BlockState blockState, BlockGetter world, BlockPos blockPos) {
        if (world instanceof ILevelExtension level) {
            if (CableHelpers.isNoFakeCable(level, blockPos, null)) {
                return Collections.singletonList(COMPONENT);
            }
        }
        return Collections.emptyList();
    }

    public static class Component implements VoxelShapeComponents.IComponent {

        public String getStateId(BlockState blockState, BlockGetter world, BlockPos blockPos) {
            return "cent";
        }

        @Override
        public VoxelShape getShape(BlockState blockState, BlockGetter world, BlockPos blockPos, CollisionContext selectionContext) {
            return BOUNDS;
        }

        @Override
        public ItemStack getCloneItemStack(Level world, BlockPos pos) {
            return new ItemStack(RegistryEntries.BLOCK_CABLE.get());
        }

        @Override
        public boolean destroy(Level world, BlockPos pos, Player player, boolean saveState) {
            if (!world.isClientSide()) {
                CableHelpers.removeCable(world, pos, player);
                return true;
            }
            return false;
        }

        @Nullable
        @Override
        @OnlyIn(Dist.CLIENT)
        public BakedModel getBreakingBaseModel(Level world, BlockPos pos) {
            return RenderHelpers.getDynamicBakedModel(world, pos);
        }

        @Override
        public InteractionResult onBlockActivated(BlockState state, Level world, BlockPos blockPos, Player player, InteractionHand hand, BlockRayTraceResultComponent hit) {
            ItemStack heldItem = player.getItemInHand(hand);
            InteractionResult actionResult = CableHelpers.onCableActivated(world, blockPos, state, player, heldItem, hit.getDirection(), null);
            if(actionResult.consumesAction()) {
                return actionResult;
            }
            return InteractionResult.PASS;
        }

        @Nullable
        @Override
        public Direction getRaytraceDirection() {
            return null;
        }

        @Override
        public boolean isRaytraceLastForFace() {
            return false;
        }

    }

}
