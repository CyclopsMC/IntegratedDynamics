package org.cyclops.integrateddynamics.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHolder;
import org.cyclops.integrateddynamics.capability.variablefacade.VariableFacadeHolderConfig;
import org.cyclops.integrateddynamics.capability.variablefacade.VariableFacadeHolderDefault;
import org.cyclops.integrateddynamics.core.item.VariableFacadeHandlerRegistry;

import java.util.List;

/**
 * Item for storing variable references.
 * @author rubensworks
 */
public class ItemVariable extends Item {

    public ItemVariable(Item.Properties properties) {
        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        IVariableFacade variableFacade = getVariableFacade(itemStack);
        variableFacade.addInformation(list, world);
        if (variableFacade != VariableFacadeHandlerRegistry.DUMMY_FACADE && Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) {
            list.add(new TranslationTextComponent("item.integrateddynamics.variable.warning"));
        }
        super.addInformation(itemStack, world, list, flag);
    }

    @Override
    public ITextComponent getDisplayName(ItemStack itemStack) {
        IVariableFacade variableFacade = getVariableFacade(itemStack);
        String label;
        if(variableFacade.isValid() && (label = variableFacade.getLabel()) != null) {
            return new StringTextComponent(label)
                    .applyTextStyle(TextFormatting.ITALIC);
        }
        return super.getDisplayName(itemStack);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new DefaultCapabilityProvider<>(() -> VariableFacadeHolderConfig.CAPABILITY, new VariableFacadeHolderDefault(stack));
    }

    public IVariableFacade getVariableFacade(ItemStack itemStack) {
        return itemStack.getCapability(VariableFacadeHolderConfig.CAPABILITY)
                .map(IVariableFacadeHolder::getVariableFacade)
                .orElse(VariableFacadeHandlerRegistry.DUMMY_FACADE);
    }

}
