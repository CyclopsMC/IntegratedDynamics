package org.cyclops.integrateddynamics.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHolder;
import org.cyclops.integrateddynamics.core.item.VariableFacadeHandlerRegistry;

import java.util.function.Consumer;

/**
 * Item for storing variable references.
 * @author rubensworks
 */
public class ItemVariable extends Item {

    public ItemVariable(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        IVariableFacade variableFacade = getVariableFacade(ValueDeseralizationContext.ofClient(), itemStack);
        variableFacade.appendHoverText(tooltipAdder, context);
        if (variableFacade != VariableFacadeHandlerRegistry.DUMMY_FACADE && Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) {
            tooltipAdder.accept(Component.translatable("item.integrateddynamics.variable.warning"));
        }
        super.appendHoverText(itemStack, context, tooltipDisplay, tooltipAdder, flag);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        IVariableFacade variableFacade = getVariableFacade(ValueDeseralizationContext.ofAllEnabled(), itemStack);
        String label;
        if(variableFacade.isValid() && (label = variableFacade.getLabel()) != null) {
            return Component.literal(label)
                    .withStyle(ChatFormatting.ITALIC);
        }
        return super.getName(itemStack);
    }

    public IVariableFacade getVariableFacade(ValueDeseralizationContext valueDeseralizationContext, ItemStack itemStack) {
        IVariableFacadeHolder holder = itemStack.getCapability(Capabilities.VariableFacade.ITEM);
        if (holder != null) {
            return holder.getVariableFacade(valueDeseralizationContext);
        }
        return VariableFacadeHandlerRegistry.DUMMY_FACADE;
    }
}
