package org.cyclops.integrateddynamics.core.item;

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
import org.cyclops.cyclopscore.config.configurable.ConfigurableItem;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.item.ItemBlockCable;

import java.util.List;

/**
 * An item that can place parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ItemPart<P extends IPartType<P, S>, S extends IPartState<P>> extends ConfigurableItem {

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

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return L10NHelpers.localize(part.getUnlocalizedName());
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer playerIn, World world, BlockPos pos, EnumFacing side,
                             float hitX, float hitY, float hitZ) {
        if(!world.isRemote) {
            if(world.getTileEntity(pos) instanceof IPartContainer) {
                // Add part to existing cable
                IPartContainer partContainer = (IPartContainer) world.getTileEntity(pos);
                if(!partContainer.hasPart(side)) {
                    partContainer.setPart(side, getPart(), getPart().getState(itemStack));
                    System.out.println("Setting part " + getPart());
                    ItemBlockCable.playPlaceSound(world, pos);
                } else {
                    System.out.println("Side occupied!");
                }
                itemStack.stackSize--;
                return true;
            } else {
                // Place part at a new position with an unreal cable
                BlockPos target = pos.offset(side);
                ItemBlockCable itemBlockCable = (ItemBlockCable) Item.getItemFromBlock(BlockCable.getInstance());
                if(itemBlockCable.onItemUse(itemStack, playerIn, world, target, side, hitX, hitY, hitZ)) {
                    IPartContainer partContainer = (IPartContainer) world.getTileEntity(target);
                    if(partContainer != null) {
                        partContainer.setPart(side.getOpposite(), getPart(), getPart().getState(itemStack));
                        System.out.println("Setting part " + getPart());
                        ItemBlockCable.playPlaceSound(world, pos);
                    }
                    BlockCable.getInstance().setRealCable(world, target, false);
                    return true;
                }
            }
        }
        return super.onItemUse(itemStack, playerIn, world, pos, side, hitX, hitY, hitZ);
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

}
