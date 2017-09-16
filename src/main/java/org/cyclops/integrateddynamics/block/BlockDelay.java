package org.cyclops.integrateddynamics.block;

import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.integrateddynamics.client.gui.GuiDelay;
import org.cyclops.integrateddynamics.core.block.BlockContainerGuiCabled;
import org.cyclops.integrateddynamics.inventory.container.ContainerDelay;
import org.cyclops.integrateddynamics.tileentity.TileDelay;

/**
 * A block that can delay variables.
 * @author rubensworks
 */
public class BlockDelay extends BlockContainerGuiCabled {

    @BlockProperty
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    private static BlockDelay _instance = null;

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static BlockDelay getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockDelay(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig, TileDelay.class);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerDelay.class;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiDelay.class;
    }
}
