package org.cyclops.integrateddynamics.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHolder;
import org.cyclops.integrateddynamics.capability.variablefacade.VariableFacadeHolderConfig;
import org.cyclops.integrateddynamics.capability.variablefacade.VariableFacadeHolderDefault;
import org.cyclops.integrateddynamics.client.render.blockentity.ItemStackBlockEntityVariableRender;
import org.cyclops.integrateddynamics.core.item.VariableFacadeHandlerRegistry;

import java.util.List;
import java.util.function.Consumer;

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
    public void appendHoverText(ItemStack itemStack, Level world, List<Component> list, TooltipFlag flag) {
        IVariableFacade variableFacade = getVariableFacade(itemStack);
        variableFacade.appendHoverText(list, world);
        if (variableFacade != VariableFacadeHandlerRegistry.DUMMY_FACADE && Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) {
            list.add(Component.translatable("item.integrateddynamics.variable.warning"));
        }
        super.appendHoverText(itemStack, world, list, flag);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        IVariableFacade variableFacade = getVariableFacade(itemStack);
        String label;
        if(variableFacade.isValid() && (label = variableFacade.getLabel()) != null) {
            return Component.literal(label)
                    .withStyle(ChatFormatting.ITALIC);
        }
        return super.getName(itemStack);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new DefaultCapabilityProvider<>(() -> VariableFacadeHolderConfig.CAPABILITY, new VariableFacadeHolderDefault(stack));
    }

    public IVariableFacade getVariableFacade(ItemStack itemStack) {
        return itemStack.getCapability(VariableFacadeHolderConfig.CAPABILITY)
                .map(IVariableFacadeHolder::getVariableFacade)
                .orElse(VariableFacadeHandlerRegistry.DUMMY_FACADE);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {
            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return new ItemStackBlockEntityVariableRender();
            }
        });
    }
}
