package org.cyclops.integrateddynamics.block;

import com.google.common.collect.Sets;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.client.gui.GuiMaterializer;
import org.cyclops.integrateddynamics.core.block.BlockContainerGuiCabled;
import org.cyclops.integrateddynamics.inventory.container.ContainerMaterializer;
import org.cyclops.integrateddynamics.network.MaterializerNetworkElement;
import org.cyclops.integrateddynamics.tileentity.TileMaterializer;

import java.util.Collection;

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
    public Collection<INetworkElement<IPartNetwork>> createNetworkElements(World world, BlockPos blockPos) {
        return Sets.<INetworkElement<IPartNetwork>>newHashSet(new MaterializerNetworkElement(DimPos.of(world, blockPos)));
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerMaterializer.class;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiMaterializer.class;
    }
}
