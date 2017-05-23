package org.cyclops.integrateddynamics.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.item.ItemBlockNBT;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.List;

/**
 * The item for the delay.
 * @author rubensworks
 */
public class ItemBlockDelay extends ItemBlockNBT {

    /**
     * Make a new instance.
     *
     * @param block The blockState instance.
     */
    public ItemBlockDelay(Block block) {
        super(block);
        this.setMaxStackSize(64);
    }

    @SuppressWarnings("rawtypes")
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4) {
        if(itemStack.getTagCompound() != null) {
            int id = itemStack.getTagCompound().getInteger("delayId");
            list.add(L10NHelpers.localize(L10NValues.GENERAL_ITEM_ID, id));
        }
        super.addInformation(itemStack, entityPlayer, list, par4);
    }
}
