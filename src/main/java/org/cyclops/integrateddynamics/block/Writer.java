package org.cyclops.integrateddynamics.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlock;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;

/**
 * A writer.
 * @author rubensworks
 */
public class Writer extends ConfigurableBlock {

    @BlockProperty
    public static final PropertyEnum FACING = PropertyDirection.create("facing", EnumFacing.class);

    private static Writer _instance = null;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static Writer getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     * @param eConfig Config for this block.
     */
    public Writer(ExtendedConfig eConfig) {
        super(eConfig, Material.glass);

        setHardness(3.0F);
        setStepSound(soundTypeMetal);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public boolean isNormalCube() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.TRANSLUCENT;
    }

}
