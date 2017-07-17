package org.cyclops.integrateddynamics.block;

import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.integrateddynamics.client.gui.GuiMaterializer;
import org.cyclops.integrateddynamics.core.block.BlockContainerGuiCabled;
import org.cyclops.integrateddynamics.inventory.container.ContainerMaterializer;
import org.cyclops.integrateddynamics.tileentity.TileMaterializer;

/**
 * A block that can materialize any variable to its raw value.
 * @author rubensworks
 */
public class BlockMaterializer extends BlockContainerGuiCabled {

    @BlockProperty
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    private static BlockMaterializer _instance = null;

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static BlockMaterializer getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockMaterializer(ExtendedConfig eConfig) {
        super(eConfig, TileMaterializer.class);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerMaterializer.class;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiMaterializer.class;
    }

    @Override
    public boolean saveNBTToDroppedItem() {
        return false;
    }
}
