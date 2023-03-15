package org.cyclops.integrateddynamics.core.item;

import com.google.common.collect.Lists;
import lombok.EqualsAndHashCode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

/**
 * An item that can place parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
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

    @Override
    public String getDescriptionId() {
        return part.getTranslationKey();
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return part.getTranslationKey();
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

        ItemStack itemStack = player.getItemInHand(hand);
        IPartContainer partContainerFirst = PartHelpers.getPartContainer(world, pos, side).orElse(null);
        if(partContainerFirst != null) {
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
            if(world.getBlockState(target).getBlock().canBeReplaced(world.getBlockState(target),
                    new BlockPlaceContext(world, player, hand, itemStack, targetRayTrace))) {
                ItemBlockCable itemBlockCable = (ItemBlockCable) Item.byBlock(RegistryEntries.BLOCK_CABLE);
                itemStack.grow(1); // Temporarily grow, because ItemBlock will shrink it.
                if (itemBlockCable.useOn(new UseOnContext(player, hand, targetRayTrace)).consumesAction()) {
                    IPartContainer partContainer = PartHelpers.getPartContainer(world, target, targetSide).orElse(null);
                    if (partContainer != null) {
                        ICableFakeable cableFakeable = CableHelpers.getCableFakeable(world, target, targetSide).orElse(null);
                        if(!world.isClientSide()) {
                            PartHelpers.addPart(world, target, side.getOpposite(), getPart(), itemStack);
                            if (cableFakeable != null) {
                                CableHelpers.onCableRemoving(world, target, false, false);
                                cableFakeable.setRealCable(false);
                                CableHelpers.onCableRemoved(world, target, CableHelpers.ALL_SIDES);
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
                IPartContainer partContainer = PartHelpers.getPartContainer(world, target, targetSide).orElse(null);
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, Level world, List<Component> list, TooltipFlag flag) {
        getPart().loadTooltip(itemStack, list);
        super.appendHoverText(itemStack, world, list, flag);
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
