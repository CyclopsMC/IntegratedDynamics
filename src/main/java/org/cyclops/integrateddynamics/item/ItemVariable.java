package org.cyclops.integrateddynamics.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.configurable.ConfigurableItem;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.capability.variablefacade.VariableFacadeHolderConfig;
import org.cyclops.integrateddynamics.capability.variablefacade.VariableFacadeHolderDefault;
import org.cyclops.integrateddynamics.core.item.VariableFacadeHandlerRegistry;

import java.util.List;

/**
 * Item for storing variable references.
 * @author rubensworks
 */
public class ItemVariable extends ConfigurableItem {

    private static ItemVariable _instance = null;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static ItemVariable getInstance() {
        return _instance;
    }

    /**
     * Make a new item instance.
     *
     * @param eConfig Config for this blockState.
     */
    public ItemVariable(ExtendedConfig<ItemConfig> eConfig) {
        super(eConfig);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World world, List<String> list, ITooltipFlag flag) {
        IVariableFacade variableFacade = getVariableFacade(itemStack);
        variableFacade.addInformation(list, world);
        if (variableFacade != VariableFacadeHandlerRegistry.DUMMY_FACADE && Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isCreative()) {
            list.add(L10NHelpers.localize("item.items.integrateddynamics.variable.warning"));
        }
        super.addInformation(itemStack, world, list, flag);
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack) {
        IVariableFacade variableFacade = getVariableFacade(itemStack);
        String label;
        if(variableFacade.isValid() && (label = variableFacade.getLabel()) != null) {
            return TextFormatting.ITALIC + label;
        }
        return super.getItemStackDisplayName(itemStack);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new DefaultCapabilityProvider<>(() -> VariableFacadeHolderConfig.CAPABILITY, new VariableFacadeHolderDefault(stack));
    }

    public IVariableFacade getVariableFacade(ItemStack itemStack) {
        return itemStack.hasCapability(VariableFacadeHolderConfig.CAPABILITY, null)
                ? itemStack.getCapability(VariableFacadeHolderConfig.CAPABILITY, null).getVariableFacade()
                : VariableFacadeHandlerRegistry.DUMMY_FACADE;
    }

}
