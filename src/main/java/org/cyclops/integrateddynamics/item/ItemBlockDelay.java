package org.cyclops.integrateddynamics.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.item.ItemBlockNBT;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.List;

/**
 * The item for the delay.
 * @author rubensworks
 */
public class ItemBlockDelay extends ItemBlockNBT {

    public ItemBlockDelay(Block block, Properties builder) {
        super(block, builder);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, Level world, List<Component> list, TooltipFlag flag) {
        if(itemStack.getTag() != null) {
            int id = itemStack.getTag().getInt("delayId");
            list.add(Component.translatable(L10NValues.GENERAL_ITEM_ID, id));
        }
        super.appendHoverText(itemStack, world, list, flag);
    }
}
