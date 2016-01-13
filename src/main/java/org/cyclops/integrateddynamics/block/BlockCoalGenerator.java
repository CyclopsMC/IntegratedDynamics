package org.cyclops.integrateddynamics.block;

import com.google.common.collect.Sets;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.client.gui.GuiCoalGenerator;
import org.cyclops.integrateddynamics.core.block.BlockContainerGuiCabled;
import org.cyclops.integrateddynamics.inventory.container.ContainerCoalGenerator;
import org.cyclops.integrateddynamics.network.CoalGeneratorNetworkElement;
import org.cyclops.integrateddynamics.tileentity.TileCoalGenerator;

import java.util.Collection;

/**
 * A block that can generate energy from coal.
 * @author rubensworks
 */
public class BlockCoalGenerator extends BlockContainerGuiCabled {

    @BlockProperty
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    @BlockProperty
    public static final PropertyBool ON = PropertyBool.create("on");

    private static BlockCoalGenerator _instance = null;

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static BlockCoalGenerator getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockCoalGenerator(ExtendedConfig eConfig) {
        super(eConfig, TileCoalGenerator.class);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, placer, itemStack);
        TileHelpers.getSafeTile(world, pos, TileCoalGenerator.class).updateBlockState();
    }

    @Override
    public Collection<INetworkElement<IPartNetwork>> createNetworkElements(World world, BlockPos blockPos) {
        return Sets.<INetworkElement<IPartNetwork>>newHashSet(new CoalGeneratorNetworkElement(DimPos.of(world, blockPos)));
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerCoalGenerator.class;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiCoalGenerator.class;
    }
}
