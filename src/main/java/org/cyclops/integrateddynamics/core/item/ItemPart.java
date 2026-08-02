package org.cyclops.integrateddynamics.core.item;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.item.ItemBlockCable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An item that can place parts.
 * @author rubensworks
 */
public class ItemPart<P extends IPartType<P, S>, S extends IPartState<P>> extends Item {

    private static final List<IUseAction> USE_ACTIONS = Lists.newArrayList();

    private final IPartType<P, S> part;

    public ItemPart(Item.Properties properties, IPartType<P, S> part) {
        super(properties);
        this.part = part;
    }

    public IPartType<P, S> getPart() {
        return part;
    }

    /**
     * Register a use action for the cable item.
     * @param useAction The use action.
     */
    public static void addUseAction(IUseAction useAction) {
        USE_ACTIONS.add(useAction);
    }

    @Override
    public Component getName(ItemStack p_200295_1_) {
        return Component.translatable(getDescriptionId());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        BlockState blockState = world.getBlockState(pos);

        ItemStack itemStack = player.getItemInHand(hand);
        IPartContainer partContainerFirst = PartHelpers.getPartContainer(world, pos, side, blockState).orElse(null);
        if(partContainerFirst != null && !partContainerFirst.hasPart(side)) {
            // Add part to existing cable
            if(PartHelpers.addPart(world, pos, side, getPart(), itemStack)) {
                if(world.isClientSide()) {
                    ItemBlockCable.playPlaceSound(world, pos);
                }
                if(!player.isCreative()) {
                    itemStack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        } else {
            // Place part at a new position with an unreal cable
            BlockPos target = pos.relative(side);
            Direction targetSide = side.getOpposite();
            BlockHitResult targetRayTrace = new BlockHitResult(new Vec3(
                    (double) target.getX() + 0.5D + (double) targetSide.getStepX() * 0.5D,
                    (double) target.getY() + 0.5D + (double) targetSide.getStepY() * 0.5D,
                    (double) target.getZ() + 0.5D + (double) targetSide.getStepZ() * 0.5D),
                    targetSide, target, false);
            if(world.getBlockState(target).canBeReplaced(new BlockPlaceContext(world, player, hand, itemStack, targetRayTrace.withPosition(target)))) {
                ItemBlockCable itemBlockCable = (ItemBlockCable) Item.byBlock(RegistryEntries.BLOCK_CABLE.get());
                itemStack.grow(1); // Temporarily grow, because ItemBlock will shrink it.
                if (itemBlockCable.useOn(new UseOnContext(player, hand, targetRayTrace)).consumesAction()) {
                    BlockState targetBlockState = world.getBlockState(target);
                    IPartContainer partContainer = PartHelpers.getPartContainer(world, target, targetSide, targetBlockState).orElse(null);
                    if (partContainer != null) {
                        ICableFakeable cableFakeable = CableHelpers.getCableFakeable(world, target, targetSide, targetBlockState).orElse(null);
                        if(!world.isClientSide()) {
                            PartHelpers.addPart(world, target, side.getOpposite(), getPart(), itemStack);
                            if (cableFakeable != null) {
                                CableHelpers.onCableRemoving(world, target, false, false, world.getBlockState(target), world.getBlockEntity(target), false);
                                cableFakeable.setRealCable(false);
                                CableHelpers.overrideCableRemovingConnections(world, target, CableHelpers.ALL_SIDES);
                                CableHelpers.onCableRemoved(world, target);
                            } else {
                                IntegratedDynamics.clog(org.apache.logging.log4j.Level.WARN, String.format("Tried to set a fake cable at a block that is not fakeable at %s", target));
                            }
                        } else {
                            cableFakeable.setRealCable(false);
                        }
                        itemStack.shrink(1);
                        return InteractionResult.SUCCESS;
                    }
                }
                itemStack.shrink(1); // Shrink manually if failed
            } else {
                BlockState targetBlockState = world.getBlockState(target);
                IPartContainer partContainer = PartHelpers.getPartContainer(world, target, targetSide, targetBlockState).orElse(null);
                if(partContainer != null) {
                    // Edge-case: if the pos was a full network block (part of the same network as target), make sure that we disconnect this part of the network first
                    if (!world.isClientSide() && NetworkHelpers.getNetwork(PartPos.of(world, pos, side)).isPresent() && partContainer.canAddPart(targetSide, getPart())) {
                        CableHelpers.getCable(world, target, targetSide)
                                .ifPresent(cable -> CableHelpers.disconnectCable(world, target, targetSide, cable, targetSide));
                    }

                    // Add part to existing cable
                    if(PartHelpers.addPart(world, target, side.getOpposite(), getPart(), itemStack)) {
                        if(world.isClientSide()) {
                            ItemBlockCable.playPlaceSound(world, target);
                        }
                        if(!player.isCreative()) {
                            itemStack.shrink(1);
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            // Check third party actions if all else fails
            for (IUseAction useAction : USE_ACTIONS) {
                if (useAction.attempItemUseTarget(this, itemStack, world, pos, side)) {
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        getPart().loadTooltip(itemStack, tooltipAdder);
        super.appendHoverText(itemStack, context, tooltipDisplay, tooltipAdder, flag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPart<?, ?> itemPart = (ItemPart<?, ?>) o;
        return Objects.equals(part, itemPart.part);
    }

    @Override
    public int hashCode() {
        return Objects.hash(part);
    }

    public static interface IUseAction {

        /**
         * Attempt to use the given item.
         * @param itemPart The part item instance.
         * @param itemStack The item stack that is being used.
         * @param world The world.
         * @param pos The position.
         * @param sideHit The side that is being hit.
         * @return If the use action was applied.
         */
        public boolean attempItemUseTarget(ItemPart itemPart, ItemStack itemStack, Level world, BlockPos pos, Direction sideHit);

    }

}
