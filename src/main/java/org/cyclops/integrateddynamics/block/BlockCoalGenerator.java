package org.cyclops.integrateddynamics.block;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.client.gui.GuiCoalGenerator;
import org.cyclops.integrateddynamics.core.block.BlockContainerGuiCabled;
import org.cyclops.integrateddynamics.inventory.container.ContainerCoalGenerator;
import org.cyclops.integrateddynamics.tileentity.TileCoalGenerator;

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
    public BlockCoalGenerator(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig, TileCoalGenerator.class);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, placer, itemStack);
        TileHelpers.getSafeTile(world, pos, TileCoalGenerator.class).updateBlockState();
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerCoalGenerator.class;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiCoalGenerator.class;
    }

    @Override
    protected boolean isPickBlockPersistData() {
        return true;
    }
}
