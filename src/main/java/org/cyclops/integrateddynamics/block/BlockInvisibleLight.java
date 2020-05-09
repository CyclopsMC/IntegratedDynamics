package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlock;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;

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
    public BlockInvisibleLight(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig, Material.AIR);

        setHardness(3.0F);
        setSoundType(SoundType.METAL);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(IBlockState blockState) {
        return false;
    }

    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public EnumBlockRenderType getRenderType(IBlockState blockState) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public Item getItemDropped(IBlockState blockState, Random random, int zero) {
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public EnumPushReaction getPushReaction(IBlockState blockState) {
        return EnumPushReaction.NORMAL;
    }

    @Override
    public boolean isReplaceable(IBlockAccess world, BlockPos blockPos) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess world, BlockPos blockPos) {
        return null;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        // Do not appear in creative tab
    }

    @Override
    public int getLightValue(IBlockState blockState, IBlockAccess world, BlockPos pos) {
        return world.getBlockState(pos).getValue(LIGHT);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getLightValue(IBlockState blockState) {
        return 15; // Required for light update when this block is removed
    }
}
