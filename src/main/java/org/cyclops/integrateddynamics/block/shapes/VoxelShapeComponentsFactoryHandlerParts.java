package org.cyclops.integrateddynamics.block.shapes;

import com.google.common.collect.Lists;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
    public Collection<VoxelShapeComponents.IComponent> createComponents(BlockState blockState, BlockGetter world, BlockPos blockPos) {
        Collection<VoxelShapeComponents.IComponent> components = Lists.newArrayList();
        for (Direction direction : Direction.values()) {
            IPartContainer partContainer = PartHelpers.getPartContainer((Level) world, blockPos, direction).orElse(null);
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

        public Optional<IPartType> getPart() {
            return Optional.ofNullable(partContainer.getPart(direction));
        }

        @Override
        public String getStateId(BlockState blockState, BlockGetter world, BlockPos blockPos) {
            return getPart()
                    .map(part -> "part(" + part.getPartRenderPosition().toCompactString() + ")")
                    .orElse("part");
        }

        @Override
        public VoxelShape getShape(BlockState blockState, BlockGetter world, BlockPos blockPos, CollisionContext selectionContext) {
            return getPart()
                    .map(part -> part.getPartRenderPosition().getBoundingBox(direction))
                    .orElse(Shapes.empty());
        }

        @Override
        public ItemStack getCloneItemStack(Level world, BlockPos pos) {
            return getPart()
                    .map(part -> part.getCloneItemStack(world, pos, partContainer.getPartState(direction)))
                    .orElse(ItemStack.EMPTY);
        }

        @Override
        public boolean destroy(Level world, BlockPos pos, Player player, boolean saveState) {
            if(!world.isClientSide()) {
                return PartHelpers.removePart(world, pos, direction, player, true, true, saveState);
            }
            return false;
        }

        @Nullable
        @Override
        @OnlyIn(Dist.CLIENT)
        public BakedModel getBreakingBaseModel(Level world, BlockPos pos) {
            return RenderHelpers.getBakedModel(getPart()
                    .map(part -> part.getBlockState(partContainer, direction))
                    .orElse(null));
        }

        @Override
        public InteractionResult onBlockActivated(BlockState state, Level world, BlockPos blockPos, Player player, InteractionHand hand, BlockRayTraceResultComponent hit) {
            ItemStack heldItem = player.getItemInHand(hand);
            if(WrenchHelpers.isWrench(player, heldItem, world, blockPos, hit.getDirection()) && player.isSecondaryUseActive()) {
                // Remove part from cable
                if (!world.isClientSide()) {
                    destroy(world, blockPos, player, true);
                    ItemBlockCable.playBreakSound(world, blockPos, state);
                }
                return InteractionResult.SUCCESS;
            } else if(CableHelpers.isNoFakeCable(world, blockPos, hit.getDirection())) {
                // Delegate activated call to part
                return getPart()
                        .map(part -> part.onPartActivated(partContainer.getPartState(direction), blockPos, world,
                                player, hand, heldItem, hit.withDirection(direction)))
                        .orElse(InteractionResult.FAIL);
            }
            return InteractionResult.PASS;
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
