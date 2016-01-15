package org.cyclops.integrateddynamics.core.item;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.config.configurable.ConfigurableItem;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartContainerFacade;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.item.ItemBlockCable;

import java.util.List;

/**
 * An item that can place parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ItemPart<P extends IPartType<P, S>, S extends IPartState<P>> extends ConfigurableItem {

    private static final List<IUseAction> USE_ACTIONS = Lists.newLinkedList();

    private final IPartType<P, S> part;

    /**
     * Make a new item instance.
     *
     * @param eConfig Config for this blockState.
     * @param part    The part this item will place.
     */
    public ItemPart(ExtendedConfig eConfig, IPartType<P, S> part) {
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
        return L10NHelpers.localize(part.getUnlocalizedName());
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer playerIn, World world, BlockPos pos, EnumFacing side,
                             float hitX, float hitY, float hitZ) {
        if(!world.isRemote) {
            IPartContainerFacade partContainerFacade = CableHelpers.getInterface(world, pos, IPartContainerFacade.class);
            if(partContainerFacade != null) {
                // Add part to existing cable
                IPartContainer partContainer = partContainerFacade.getPartContainer(world, pos);
                if(addPart(world, pos, side, partContainer, itemStack) && !playerIn.capabilities.isCreativeMode) {
                    itemStack.stackSize--;
                }
                return true;
            } else {
                // Check all third party actions
                for (IUseAction useAction : USE_ACTIONS) {
                    if (useAction.attempItemUseTarget(this, itemStack, world, pos, side)) {
                        return true;
                    }
                }

                // Place part at a new position with an unreal cable
                BlockPos target = pos.offset(side);
                if(world.getBlockState(target).getBlock().isReplaceable(world, target)) {
                    ItemBlockCable itemBlockCable = (ItemBlockCable) Item.getItemFromBlock(BlockCable.getInstance());
                    if (itemBlockCable.onItemUse(itemStack, playerIn, world, target, side, hitX, hitY, hitZ)) {
                        partContainerFacade = CableHelpers.getInterface(world, target, IPartContainerFacade.class);
                        if (partContainerFacade != null) {
                            IPartContainer partContainer = partContainerFacade.getPartContainer(world, target);
                            addPart(world, pos, side.getOpposite(), partContainer, itemStack);
                            if (world.getBlockState(target).getBlock() instanceof ICableFakeable) {
                                BlockCable.getInstance().setRealCable(world, target, false);
                            } else {
                                IntegratedDynamics.clog(Level.WARN, String.format("Tried to set a fake cable at a block that is not fakeable, got %s", world.getBlockState(target).getBlock()));
                            }
                            return true;
                        }
                    }
                } else {
                    partContainerFacade = CableHelpers.getInterface(world, target, IPartContainerFacade.class);
                    if(partContainerFacade != null) {
                        IPartContainer partContainer = partContainerFacade.getPartContainer(world, target);
                        if(addPart(world, pos, side.getOpposite(), partContainer, itemStack) && !playerIn.capabilities.isCreativeMode) {
                            itemStack.stackSize--;
                        }
                        return true;
                    }
                }
            }
        }
        return super.onItemUse(itemStack, playerIn, world, pos, side, hitX, hitY, hitZ);
    }

    protected boolean addPart(World world, BlockPos pos, EnumFacing side, IPartContainer partContainer, ItemStack itemStack) {
        IPartType partType = getPart();
        if(partContainer.canAddPart(side, partType)) {
            partContainer.setPart(side, getPart(), partType.getState(itemStack));
            System.out.println("Setting part " + getPart());
            ItemBlockCable.playPlaceSound(world, pos);
            return true;
        } else {
            System.out.println("Side occupied!");
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4) {
        if(itemStack.getTagCompound() != null) {
            int id = itemStack.getTagCompound().getInteger("id");
            list.add(L10NHelpers.localize("item.items.integrateddynamics.general.id", id));
        }
        L10NHelpers.addOptionalInfo(list, getPart().getUnlocalizedNameBase());
        super.addInformation(itemStack, entityPlayer, list, par4);
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
