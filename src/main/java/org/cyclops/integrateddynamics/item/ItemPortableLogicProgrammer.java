package org.cyclops.integrateddynamics.item;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.cyclopscore.item.ItemGui;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammerPortable;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerPortable;

/**
 * A portable logic programmer.
 * @author rubensworks
 */
public class ItemPortableLogicProgrammer extends ItemGui {

    private static ItemPortableLogicProgrammer _instance = null;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static ItemPortableLogicProgrammer getInstance() {
        return _instance;
    }

    /**
     * Make a new item instance.
     *
     * @param eConfig Config for this blockState.
     */
    public ItemPortableLogicProgrammer(ExtendedConfig<ItemConfig> eConfig) {
        super(eConfig);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerLogicProgrammerPortable.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiLogicProgrammerPortable.class;
    }
}
