package org.cyclops.integrateddynamics.block.shapes;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.block.BlockRayTraceResultComponent;
import org.cyclops.integrateddynamics.core.block.VoxelShapeComponents;
import org.cyclops.integrateddynamics.core.block.VoxelShapeComponentsFactory;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.cyclops.integrateddynamics.item.ItemBlockCable;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;

/**
 * Shape handler for parts.
 * @author rubensworks
 */
public class VoxelShapeComponentsFactoryHandlerParts implements VoxelShapeComponentsFactory.IHandler {

    @Override
    public Collection<VoxelShapeComponents.IComponent> createComponents(BlockState blockState, IBlockReader world, BlockPos blockPos) {
        Collection<VoxelShapeComponents.IComponent> components = Lists.newArrayList();
        for (Direction direction : Direction.values()) {
            IPartContainer partContainer = PartHelpers.getPartContainer(world, blockPos, direction).orElse(null);
            if (partContainer != null && partContainer.hasPart(direction)) {
                components.add(new Component(direction, partContainer));
            }
        }
        return components;
    }

    public static class Component implements VoxelShapeComponents.IComponent {

        private final Direction direction;
        private final IPartContainer partContainer;

        public Component(Direction direction, IPartContainer partContainer) {
            this.direction = direction;
            this.partContainer = partContainer;
        }

        protected Optional<IPartType> getPart() {
            return Optional.ofNullable(partContainer.getPart(direction));
        }

        @Override
        public String getStateId(BlockState blockState, IBlockReader world, BlockPos blockPos) {
            return getPart()
                    .map(part -> "part(" + part.getPartRenderPosition().toCompactString() + ")")
                    .orElse("part");
        }

        @Override
        public VoxelShape getShape(BlockState blockState, IBlockReader world, BlockPos blockPos, ISelectionContext selectionContext) {
            return getPart()
                    .map(part -> part.getPartRenderPosition().getBoundingBox(direction))
                    .orElse(VoxelShapes.empty());
        }

        @Override
        public ItemStack getPickBlock(World world, BlockPos pos) {
            return getPart()
                    .map(part -> part.getPickBlock(world, pos, partContainer.getPartState(direction)))
                    .orElse(ItemStack.EMPTY);
        }

        @Override
        public boolean destroy(World world, BlockPos pos, PlayerEntity player, boolean saveState) {
            if(!world.isRemote()) {
                return PartHelpers.removePart(world, pos, direction, player, true, true, saveState);
            }
            return false;
        }

        @Nullable
        @Override
        @OnlyIn(Dist.CLIENT)
        public IBakedModel getBreakingBaseModel(World world, BlockPos pos) {
            return RenderHelpers.getBakedModel(getPart()
                    .map(part -> part.getBlockState(partContainer, direction))
                    .orElse(null));
        }

        @Override
        public ActionResultType onBlockActivated(BlockState state, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockRayTraceResultComponent hit) {
            ItemStack heldItem = player.getHeldItem(hand);
            if(WrenchHelpers.isWrench(player, heldItem, world, blockPos, hit.getFace()) && player.isSecondaryUseActive()) {
                // Remove part from cable
                if (!world.isRemote()) {
                    destroy(world, blockPos, player, true);
                    ItemBlockCable.playBreakSound(world, blockPos, state);
                }
                return ActionResultType.SUCCESS;
            } else if(CableHelpers.isNoFakeCable(world, blockPos, hit.getFace())) {
                // Delegate activated call to part
                return getPart()
                        .map(part -> part.onPartActivated(partContainer.getPartState(direction), blockPos, world,
                                player, hand, heldItem, hit.withFace(direction)))
                        .orElse(ActionResultType.FAIL);
            }
            return ActionResultType.PASS;
        }

        @Nullable
        @Override
        public Direction getRaytraceDirection() {
            return direction;
        }

        @Override
        public boolean isRaytraceLastForFace() {
            return false;
        }

    }

}
