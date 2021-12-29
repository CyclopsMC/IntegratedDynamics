package org.cyclops.integrateddynamics.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
    public void appendHoverText(ItemStack itemStack, Level world, List<Component> list, TooltipFlag flag) {
        if(itemStack.getTag() != null) {
            int id = itemStack.getTag().getInt(BlockProxy.NBT_ID);
            list.add(new TranslatableComponent(L10NValues.GENERAL_ITEM_ID, id));
        }
        super.appendHoverText(itemStack, world, list, flag);
    }
}
