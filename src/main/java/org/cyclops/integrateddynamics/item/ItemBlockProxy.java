package org.cyclops.integrateddynamics.item;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.item.ItemBlockNBT;
import org.cyclops.integrateddynamics.block.BlockProxy;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.List;

/**
 * The item for the proxy.
 * @author rubensworks
 */
public class ItemBlockProxy extends ItemBlockNBT {

    public ItemBlockProxy(Block block, Properties builder) {
        super(block, builder);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        if(itemStack.getTag() != null) {
            int id = itemStack.getTag().getInt(BlockProxy.NBT_ID);
            list.add(new TranslationTextComponent(L10NValues.GENERAL_ITEM_ID, id));
        }
        super.addInformation(itemStack, world, list, flag);
    }
}
