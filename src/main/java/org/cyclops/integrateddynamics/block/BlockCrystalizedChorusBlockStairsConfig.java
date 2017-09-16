package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockStairs;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Chorus Stairs.
 * @author rubensworks
 *
 */
public class BlockCrystalizedChorusBlockStairsConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockCrystalizedChorusBlockStairsConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockCrystalizedChorusBlockStairsConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "crystalized_chorus_block_stairs",
                null,
                null
        );
    }

    @Override
    protected ConfigurableBlockStairs initSubInstance() {
        ConfigurableBlockStairs block = (ConfigurableBlockStairs) new ConfigurableBlockStairs(this, BlockMenrilLog.getInstance().getDefaultState()) {
            @SuppressWarnings("deprecation")
            @Override
            public SoundType getSoundType() {
                return SoundType.SNOW;
            }
        }.setHardness(1.5F);
        block.setHarvestLevel("pickaxe", 0);
        return block;
    }

}
