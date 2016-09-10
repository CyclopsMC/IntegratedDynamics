package org.cyclops.integrateddynamics.block;

import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.client.gui.GuiProxy;
import org.cyclops.integrateddynamics.core.block.BlockContainerGuiCabled;
import org.cyclops.integrateddynamics.inventory.container.ContainerProxy;
import org.cyclops.integrateddynamics.tileentity.TileProxy;

/**
 * A block that can expose variables.
 * @author rubensworks
 */
public class BlockProxy extends BlockContainerGuiCabled {

    @BlockProperty
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    private static BlockProxy _instance = null;

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static BlockProxy getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockProxy(ExtendedConfig eConfig) {
        super(eConfig, TileProxy.class);
    }

    @Override
    protected void onPreBlockDestroyed(World world, BlockPos pos) {
        TileProxy tile = TileHelpers.getSafeTile(world, pos, TileProxy.class);
        tile.updateConnections();
        super.onPreBlockDestroyed(world, pos);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerProxy.class;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiProxy.class;
    }
}
