package org.cyclops.integrateddynamics.item;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.cyclopscore.item.ItemGui;
import org.cyclops.integrateddynamics.client.gui.GuiOnTheDynamicsOfIntegration;

/**
 * On the Dynamics of Integration book.
 * @author rubensworks
 */
public class ItemOnTheDynamicsOfIntegration extends ItemGui {

    private static ItemOnTheDynamicsOfIntegration _instance = null;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static ItemOnTheDynamicsOfIntegration getInstance() {
        return _instance;
    }

    public ItemOnTheDynamicsOfIntegration(ExtendedConfig<ItemConfig> eConfig) {
        super(eConfig);
        this.setMaxStackSize(1);
    }

    @Override
    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.UNCOMMON;
    }

    @Override
    public Class<? extends Container> getContainer() {
        // We don't set a container, since this book does not require any server component.
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Class<? extends GuiScreen> getGui() {
        return GuiOnTheDynamicsOfIntegration.class;
    }

    @Override
    protected boolean isClientSideOnlyGui() {
        return true;
    }
}
