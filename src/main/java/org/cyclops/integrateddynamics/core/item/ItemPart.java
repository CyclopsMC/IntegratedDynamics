package org.cyclops.integrateddynamics.core.item;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.item.ItemBlockCable;

import java.util.List;

/**
 * An item that can place parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ItemPart<P extends IPartType<P, S>, S extends IPartState<P>> extends Item {

    private static final List<IUseAction> USE_ACTIONS = Lists.newArrayList();

    private final IPartType<P, S> part;

    public ItemPart(Item.Properties properties, IPartType<P, S> part) {
        super(properties);
        this.part = part;
    }

    @Override
    public String getTranslationKey() {
        return part.getTranslationKey();
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
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
    public ITextComponent getDisplayName(ItemStack p_200295_1_) {
        return new TranslationTextComponent(getTranslationKey());
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        BlockPos pos = context.getPos();
        Direction side = context.getFace();

        ItemStack itemStack = player.getHeldItem(hand);
        IPartContainer partContainerFirst = PartHelpers.getPartContainer(world, pos, side).orElse(null);
        if(partContainerFirst != null) {
            // Add part to existing cable
            if(PartHelpers.addPart(world, pos, side, getPart(), itemStack)) {
                if(world.isRemote()) {
                    ItemBlockCable.playPlaceSound(world, pos);
                }
                if(!player.isCreative()) {
                    itemStack.shrink(1);
                }
            }
            return ActionResultType.SUCCESS;
        } else {
            // Place part at a new position with an unreal cable
            BlockPos target = pos.offset(side);
            Direction targetSide = side.getOpposite();
            BlockRayTraceResult targetRayTrace = new BlockRayTraceResult(new Vec3d(
                    (double) target.getX() + 0.5D + (double) targetSide.getXOffset() * 0.5D,
                    (double) target.getY() + 0.5D + (double) targetSide.getYOffset() * 0.5D,
                    (double) target.getZ() + 0.5D + (double) targetSide.getZOffset() * 0.5D),
                    targetSide, target, false);
            if(world.getBlockState(target).getBlock().isReplaceable(world.getBlockState(target),
                    new BlockItemUseContext(world, player, hand, itemStack, targetRayTrace))) {
                ItemBlockCable itemBlockCable = (ItemBlockCable) Item.getItemFromBlock(RegistryEntries.BLOCK_CABLE);
                itemStack.grow(1); // Temporarily grow, because ItemBlock will shrink it.
                if (itemBlockCable.onItemUse(new ItemUseContext(player, hand, targetRayTrace)) == ActionResultType.SUCCESS) {
                    IPartContainer partContainer = PartHelpers.getPartContainer(world, target, targetSide).orElse(null);
                    if (partContainer != null) {
                        ICableFakeable cableFakeable = CableHelpers.getCableFakeable(world, target, targetSide).orElse(null);
                        if(!world.isRemote()) {
                            PartHelpers.addPart(world, target, side.getOpposite(), getPart(), itemStack);
                            if (cableFakeable != null) {
                                CableHelpers.onCableRemoving(world, target, false, false);
                                cableFakeable.setRealCable(false);
                                CableHelpers.onCableRemoved(world, target, CableHelpers.ALL_SIDES);
                            } else {
                                IntegratedDynamics.clog(Level.WARN, String.format("Tried to set a fake cable at a block that is not fakeable at %s", target));
                            }
                        } else {
                            cableFakeable.setRealCable(false);
                        }
                        itemStack.shrink(1);
                        return ActionResultType.SUCCESS;
                    }
                }
                itemStack.shrink(1); // Shrink manually if failed
            } else {
                IPartContainer partContainer = PartHelpers.getPartContainer(world, target, targetSide).orElse(null);
                if(partContainer != null) {
                    // Add part to existing cable
                    if(PartHelpers.addPart(world, target, side.getOpposite(), getPart(), itemStack)) {
                        if(world.isRemote()) {
                            ItemBlockCable.playPlaceSound(world, target);
                        }
                        if(!player.isCreative()) {
                            itemStack.shrink(1);
                        }
                    }
                    return ActionResultType.SUCCESS;
                }
            }

            // Check third party actions if all else fails
            for (IUseAction useAction : USE_ACTIONS) {
                if (useAction.attempItemUseTarget(this, itemStack, world, pos, side)) {
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return super.onItemUse(context);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        if(itemStack.getTag() != null
                && itemStack.getTag().contains("id", Constants.NBT.TAG_INT)) {
            int id = itemStack.getTag().getInt("id");
            list.add(new TranslationTextComponent(L10NValues.GENERAL_ITEM_ID, id));
        }
        getPart().loadTooltip(itemStack, list);
        super.addInformation(itemStack, world, list, flag);
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
        public boolean attempItemUseTarget(ItemPart itemPart, ItemStack itemStack, World world, BlockPos pos, Direction sideHit);

    }

}
