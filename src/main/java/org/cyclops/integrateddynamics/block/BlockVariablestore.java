package org.cyclops.integrateddynamics.block;

import com.google.common.collect.Sets;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;
import org.cyclops.integrateddynamics.api.block.IVariableContainerFacade;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.client.gui.GuiVariablestore;
import org.cyclops.integrateddynamics.core.block.BlockContainerGuiCabled;
import org.cyclops.integrateddynamics.inventory.container.ContainerVariablestore;
import org.cyclops.integrateddynamics.network.VariablestoreNetworkElement;
import org.cyclops.integrateddynamics.tileentity.TileVariablestore;

import java.util.Collection;

/**
 * A block that can hold defined variables so that they can be referred to elsewhere in the network.
 *
 * @author rubensworks
 */
public class BlockVariablestore extends BlockContainerGuiCabled implements IVariableContainerFacade {

    @BlockProperty
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    private static BlockVariablestore _instance = null;

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static BlockVariablestore getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockVariablestore(ExtendedConfig eConfig) {
        super(eConfig, TileVariablestore.class);
    }

    @Override
    public boolean saveNBTToDroppedItem() {
        return false;
    }

    @Override
    public Collection<INetworkElement<IPartNetwork>> createNetworkElements(World world, BlockPos blockPos) {
        return Sets.<INetworkElement<IPartNetwork>>newHashSet(new VariablestoreNetworkElement(DimPos.of(world, blockPos)));
    }

    @Override
    public IVariableContainer getVariableContainer(World world, BlockPos pos) {
        return TileHelpers.getSafeTile(world, pos, IVariableContainer.class);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerVariablestore.class;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiVariablestore.class;
    }
}
