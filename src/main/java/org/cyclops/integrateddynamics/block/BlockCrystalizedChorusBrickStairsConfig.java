package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockStairs;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Chorus Stairs.
 * @author rubensworks
 *
 */
public class BlockCrystalizedChorusBrickStairsConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockCrystalizedChorusBrickStairsConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockCrystalizedChorusBrickStairsConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "crystalized_chorus_brick_stairs",
                null,
                null
        );
    }

    @Override
    protected ConfigurableBlockStairs initSubInstance() {
        ConfigurableBlockStairs block = (ConfigurableBlockStairs) new ConfigurableBlockStairs(this, BlockCrystalizedChorusBrickConfig._instance.getBlockInstance().getDefaultState()) {
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
