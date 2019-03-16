package org.cyclops.integrateddynamics.core.item;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.config.configurable.ConfigurableItem;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.block.BlockCable;
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
public class ItemPart<P extends IPartType<P, S>, S extends IPartState<P>> extends ConfigurableItem {

    private static final List<IUseAction> USE_ACTIONS = Lists.newArrayList();

    private final IPartType<P, S> part;

    /**
     * Make a new item instance.
     *
     * @param eConfig Config for this blockState.
     * @param part    The part this item will place.
     */
    public ItemPart(ExtendedConfig<ItemConfig> eConfig, IPartType<P, S> part) {
        super(eConfig);
        this.part = part;
    }

    /**
     * Register a use action for the cable item.
     * @param useAction The use action.
     */
    public static void addUseAction(IUseAction useAction) {
        USE_ACTIONS.add(useAction);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return L10NHelpers.localize(part.getTranslationKey());
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack itemStack = playerIn.getHeldItem(hand);
        IPartContainer partContainerFirst = PartHelpers.getPartContainer(world, pos, side);
        if(partContainerFirst != null) {
            // Add part to existing cable
            if(PartHelpers.addPart(world, pos, side, getPart(), itemStack)) {
                if(world.isRemote) {
                    ItemBlockCable.playPlaceSound(world, pos);
                }
                if(!playerIn.capabilities.isCreativeMode) {
                    itemStack.shrink(1);
                }
            }
            return EnumActionResult.SUCCESS;
        } else {
            // Place part at a new position with an unreal cable
            BlockPos target = pos.offset(side);
            EnumFacing targetSide = side.getOpposite();
            if(world.getBlockState(target).getBlock().isReplaceable(world, target)) {
                ItemBlockCable itemBlockCable = (ItemBlockCable) Item.getItemFromBlock(BlockCable.getInstance());
                itemStack.grow(1); // Temporarily grow, because ItemBlock will shrink it.
                if (itemBlockCable.onItemUse(playerIn, world, target, hand, side.getOpposite(), hitX, hitY, hitZ) == EnumActionResult.SUCCESS) {
                    IPartContainer partContainer = PartHelpers.getPartContainer(world, target, targetSide);
                    if (partContainer != null) {
                        ICableFakeable cableFakeable = CableHelpers.getCableFakeable(world, target, targetSide);
                        if(!world.isRemote) {
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
                        return EnumActionResult.SUCCESS;
                    }
                }
                itemStack.shrink(1); // Shrink manually if failed
            } else {
                IPartContainer partContainer = PartHelpers.getPartContainer(world, target, targetSide);
                if(partContainer != null) {
                    // Add part to existing cable
                    if(PartHelpers.addPart(world, target, side.getOpposite(), getPart(), itemStack)) {
                        if(world.isRemote) {
                            ItemBlockCable.playPlaceSound(world, target);
                        }
                        if(!playerIn.capabilities.isCreativeMode) {
                            itemStack.shrink(1);
                        }
                    }
                    return EnumActionResult.SUCCESS;
                }
            }

            // Check third party actions if all else fails
            for (IUseAction useAction : USE_ACTIONS) {
                if (useAction.attempItemUseTarget(this, itemStack, world, pos, side)) {
                    return EnumActionResult.SUCCESS;
                }
            }
        }
        return super.onItemUse(playerIn, world, pos, hand, side, hitX, hitY, hitZ);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World world, List<String> list, ITooltipFlag flag) {
        if(itemStack.getTagCompound() != null
                && itemStack.getTagCompound().hasKey("id", MinecraftHelpers.NBTTag_Types.NBTTagInt.ordinal())) {
            int id = itemStack.getTagCompound().getInteger("id");
            list.add(L10NHelpers.localize(L10NValues.GENERAL_ITEM_ID, id));
        }
        getPart().loadTooltip(itemStack, list);
        L10NHelpers.addOptionalInfo(list, getPart().getTranslationKeyBase());
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
        public boolean attempItemUseTarget(ItemPart itemPart, ItemStack itemStack, World world, BlockPos pos, EnumFacing sideHit);

    }

}
