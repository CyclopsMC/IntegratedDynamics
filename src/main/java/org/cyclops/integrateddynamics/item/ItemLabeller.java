package org.cyclops.integrateddynamics.item;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.item.ItemGui;
import org.cyclops.integrateddynamics.client.gui.GuiLabeller;
import org.cyclops.integrateddynamics.inventory.container.ContainerLabeller;

/**
 * A labeller for variables.
 * @author rubensworks
 */
public class ItemLabeller extends ItemGui {

    private static ItemLabeller _instance = null;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static ItemLabeller getInstance() {
        return _instance;
    }

    /**
     * Make a new item instance.
     *
     * @param eConfig Config for this blockState.
     */
    public ItemLabeller(ExtendedConfig eConfig) {
        super(eConfig);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerLabeller.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiLabeller.class;
    }
}
