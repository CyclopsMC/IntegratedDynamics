package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockLog;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.integrateddynamics.item.ItemCrystalizedMenrilChunkConfig;

import java.util.List;

/**
 * Menril log block.
 * @author rubensworks
 */
public class BlockMenrilLog extends ConfigurableBlockLog {

    private static BlockMenrilLog _instance = null;

    @BlockProperty
    public static final PropertyInteger FILLED = PropertyInteger.create("filled", 0, 2);

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static BlockMenrilLog getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockMenrilLog(ExtendedConfig eConfig) {
        super(eConfig);
        this.setHardness(2.0F);
    }

    @Override
    public SoundType getSoundType() {
        return SoundType.WOOD;
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos blockPos, IBlockState blockStatedata, int fortune) {
        List<ItemStack> drops = super.getDrops(world, blockPos, blockStatedata, fortune);
        if (blockStatedata.getValue(FILLED) > 0) {
            drops.add(new ItemStack(ItemCrystalizedMenrilChunkConfig._instance.getItemInstance(), 1 + RANDOM.nextInt(3 + fortune)));
        }
        return drops;
    }
}
