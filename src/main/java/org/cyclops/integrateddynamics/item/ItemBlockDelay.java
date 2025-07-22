package org.cyclops.integrateddynamics.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.cyclops.cyclopscore.item.ItemBlockNBT;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.function.Consumer;

/**
 * The item for the delay.
 * @author rubensworks
 */
public class ItemBlockDelay extends ItemBlockNBT {

    public ItemBlockDelay(Block block, Properties builder) {
        super(block, builder);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        if(itemStack.has(RegistryEntries.DATACOMPONENT_PROXY_ID)) {
            int id = itemStack.get(RegistryEntries.DATACOMPONENT_PROXY_ID);
            tooltipAdder.accept(Component.translatable(L10NValues.GENERAL_ITEM_ID, id));
        }
        super.appendHoverText(itemStack, context, tooltipDisplay, tooltipAdder, flag);
    }
}
