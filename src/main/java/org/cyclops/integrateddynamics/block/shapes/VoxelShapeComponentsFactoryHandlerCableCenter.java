package org.cyclops.integrateddynamics.block.shapes;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

    private static final VoxelShape BOUNDS = VoxelShapes.create(new AxisAlignedBB(
            CableModel.MIN, CableModel.MIN, CableModel.MIN,
            CableModel.MAX, CableModel.MAX, CableModel.MAX));
    private static final VoxelShapeComponentsFactoryHandlerCableCenter.Component COMPONENT = new Component();

    @Override
    public Collection<VoxelShapeComponents.IComponent> createComponents(BlockState blockState, IBlockReader world, BlockPos blockPos) {
        if (CableHelpers.isNoFakeCable(world, blockPos, null)) {
            return Collections.singletonList(COMPONENT);
        }
        return Collections.emptyList();
    }

    public static class Component implements VoxelShapeComponents.IComponent {

        @Override
        public VoxelShape getShape(BlockState blockState, IBlockReader world, BlockPos blockPos, ISelectionContext selectionContext) {
            return BOUNDS;
        }

        @Override
        public ItemStack getPickBlock(World world, BlockPos pos) {
            return new ItemStack(RegistryEntries.BLOCK_CABLE);
        }

        @Override
        public boolean destroy(World world, BlockPos pos, PlayerEntity player, boolean saveState) {
            if (!world.isClientSide()) {
                CableHelpers.removeCable(world, pos, player);
                return true;
            }
            return false;
        }

        @Nullable
        @Override
        @OnlyIn(Dist.CLIENT)
        public IBakedModel getBreakingBaseModel(World world, BlockPos pos) {
            return RenderHelpers.getDynamicBakedModel(world, pos);
        }

        @Override
        public ActionResultType onBlockActivated(BlockState state, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockRayTraceResultComponent hit) {
            ItemStack heldItem = player.getItemInHand(hand);
            ActionResultType actionResult = CableHelpers.onCableActivated(world, blockPos, state, player, heldItem, hit.getDirection(), null);
            if(actionResult.consumesAction()) {
                return actionResult;
            }
            return ActionResultType.PASS;
        }

    }

}
