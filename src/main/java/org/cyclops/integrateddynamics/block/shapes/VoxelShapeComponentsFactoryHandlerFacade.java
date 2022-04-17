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
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.capability.facadeable.FacadeableConfig;
import org.cyclops.integrateddynamics.core.block.BlockRayTraceResultComponent;
import org.cyclops.integrateddynamics.core.block.VoxelShapeComponents;
import org.cyclops.integrateddynamics.core.block.VoxelShapeComponentsFactory;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Shape handler for facades.
 * @author rubensworks
 */
public class VoxelShapeComponentsFactoryHandlerFacade implements VoxelShapeComponentsFactory.IHandler {

    private static final VoxelShape BOUNDS = VoxelShapes.create(new AxisAlignedBB(
            0.01, 0.01, 0.01,
            0.99, 0.99, 0.99));
    private static final VoxelShapeComponentsFactoryHandlerFacade.Component COMPONENT = new Component();

    @Override
    public Collection<VoxelShapeComponents.IComponent> createComponents(BlockState blockState, IBlockReader world, BlockPos blockPos) {
        if (CableHelpers.hasFacade(world, blockPos)) {
            return Collections.singletonList(COMPONENT);
        }
        return Collections.emptyList();
    }

    public static class Component implements VoxelShapeComponents.IComponent {

        @Override
        public String getStateId(BlockState blockState, IBlockReader world, BlockPos blockPos) {
            String id = "fac";
            Optional<BlockState> optionalFacade = CableHelpers.getFacade(world, blockPos);
            if (optionalFacade.isPresent()) {
                id += "(" + optionalFacade.get().toString() + ")";
            }
            return id;
        }

        @Override
        public VoxelShape getShape(BlockState blockState, IBlockReader world, BlockPos blockPos, ISelectionContext selectionContext) {
            return BOUNDS;
        }

        @Override
        public ItemStack getPickBlock(World world, BlockPos pos) {
            ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_FACADE);
            CableHelpers.getFacade(world, pos)
                    .ifPresent(facade -> RegistryEntries.ITEM_FACADE.writeFacadeBlock(itemStack, facade));
            return itemStack;
        }

        @Override
        public boolean destroy(World world, BlockPos pos, PlayerEntity player, boolean saveState) {
            if(!world.isRemote()) {
                TileHelpers.getCapability(world, pos, FacadeableConfig.CAPABILITY)
                        .ifPresent(facadeable -> {
                            BlockState blockState = facadeable.getFacade();
                            ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_FACADE);
                            RegistryEntries.ITEM_FACADE.writeFacadeBlock(itemStack, blockState);
                            facadeable.setFacade(null);
                            if (!player.isCreative()) {
                                ItemStackHelpers.spawnItemStackToPlayer(world, pos, itemStack, player);
                            }
                        });
                return true;
            }
            return false;
        }

        @Nullable
        @Override
        @OnlyIn(Dist.CLIENT)
        public IBakedModel getBreakingBaseModel(World world, BlockPos pos) {
            return CableHelpers.getFacade(world, pos)
                    .map(facade -> RenderHelpers.getBakedModel(facade.getBlockState()))
                    .orElse(null);
        }

        @Override
        public ActionResultType onBlockActivated(BlockState state, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockRayTraceResultComponent hit) {
            ItemStack heldItem = player.getHeldItem(hand);
            if(WrenchHelpers.isWrench(player, heldItem, world, blockPos, hit.getFace()) && player.isSecondaryUseActive()) {
                if (!world.isRemote()) {
                    destroy(world, blockPos, player, true);
                    world.notifyNeighborsOfStateChange(blockPos, state.getBlock());
                }
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.PASS;
        }

    }

}
