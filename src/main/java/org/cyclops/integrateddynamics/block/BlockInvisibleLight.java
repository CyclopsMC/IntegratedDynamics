package org.cyclops.integrateddynamics.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlock;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;

import java.util.List;
import java.util.Random;

/**
 * An invisible light source with variable intensity.
 * @author rubensworks
 */
public class BlockInvisibleLight extends ConfigurableBlock {

    @BlockProperty(ignore = true)
    public static final PropertyInteger LIGHT = PropertyInteger.create("light", 0, 15);

    private static BlockInvisibleLight _instance = null;

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static BlockInvisibleLight getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockInvisibleLight(ExtendedConfig eConfig) {
        super(eConfig, Material.air);

        setHardness(3.0F);
        setStepSound(soundTypeMetal);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public Item getItemDropped(IBlockState blockState, Random random, int zero) {
        return null;
    }

    @Override
    public int getMobilityFlag() {
        return 0;
    }

    @Override
    public boolean isReplaceable(World world, BlockPos blockPos) {
        return true;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos blockPos, IBlockState blockState) {
        return null;
    }

    /*@Override
    public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos blockPos) {
        return AxisAlignedBB.fromBounds(0, 0, 0, 0, 0, 0);
    }*/

    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        // Do not appear in creative tab
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos) {
        return world.getBlockState(pos).getValue(LIGHT);
    }
}
