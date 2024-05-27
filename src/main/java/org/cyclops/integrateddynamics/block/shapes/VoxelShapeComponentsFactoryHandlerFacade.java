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
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.RegistryEntries;
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

    private static final VoxelShape BOUNDS = Shapes.create(new AABB(
            0, 0, 0,
            1, 1, 1));
    private static final VoxelShapeComponentsFactoryHandlerFacade.Component COMPONENT = new Component();

    @Override
    public Collection<VoxelShapeComponents.IComponent> createComponents(BlockState blockState, BlockGetter world, BlockPos blockPos) {
        if (world instanceof ILevelExtension level) {
            if (CableHelpers.hasFacade(level, blockPos)) {
                return Collections.singletonList(COMPONENT);
            }
        }
        return Collections.emptyList();
    }

    public static class Component implements VoxelShapeComponents.IComponent {

        @Override
        public String getStateId(BlockState blockState, BlockGetter world, BlockPos blockPos) {
            String id = "fac";
            Optional<BlockState> optionalFacade = CableHelpers.getFacade((Level) world, blockPos);
            if (optionalFacade.isPresent()) {
                id += "(" + optionalFacade.get().toString() + ")";
            }
            return id;
        }

        @Override
        public VoxelShape getShape(BlockState blockState, BlockGetter world, BlockPos blockPos, CollisionContext selectionContext) {
            return BOUNDS;
        }

        @Override
        public ItemStack getCloneItemStack(Level world, BlockPos pos) {
            ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_FACADE);
            CableHelpers.getFacade(world, pos)
                    .ifPresent(facade -> RegistryEntries.ITEM_FACADE.get().writeFacadeBlock(itemStack, facade));
            return itemStack;
        }

        @Override
        public boolean destroy(Level world, BlockPos pos, Player player, boolean saveState) {
            if(!world.isClientSide()) {
                BlockEntityHelpers.getCapability(world, pos, Capabilities.Facadeable.BLOCK)
                        .ifPresent(facadeable -> {
                            BlockState blockState = facadeable.getFacade();
                            ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_FACADE);
                            RegistryEntries.ITEM_FACADE.get().writeFacadeBlock(itemStack, blockState);
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
        public BakedModel getBreakingBaseModel(Level world, BlockPos pos) {
            return CableHelpers.getFacade(world, pos)
                    .map(RenderHelpers::getBakedModel)
                    .orElse(null);
        }

        @Override
        public InteractionResult onBlockActivated(BlockState state, Level world, BlockPos blockPos, Player player, InteractionHand hand, BlockRayTraceResultComponent hit) {
            ItemStack heldItem = player.getItemInHand(hand);
            if(WrenchHelpers.isWrench(player, heldItem, world, blockPos, hit.getDirection()) && player.isSecondaryUseActive()) {
                if (!world.isClientSide()) {
                    destroy(world, blockPos, player, true);
                    world.updateNeighborsAt(blockPos, state.getBlock());
                }
                return InteractionResult.SUCCESS;
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
            return true;
        }

    }

}
