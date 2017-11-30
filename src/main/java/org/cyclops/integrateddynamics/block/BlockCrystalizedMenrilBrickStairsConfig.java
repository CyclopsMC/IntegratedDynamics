package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockStairs;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystallized Menril Brick Stairs.
 * @author rubensworks
 *
 */
public class BlockCrystalizedMenrilBrickStairsConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockCrystalizedMenrilBrickStairsConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockCrystalizedMenrilBrickStairsConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "crystalized_menril_brick_stairs",
                null,
                null
        );
    }

    @Override
    protected ConfigurableBlockStairs initSubInstance() {
        ConfigurableBlockStairs block = (ConfigurableBlockStairs) new ConfigurableBlockStairs(this, BlockCrystalizedMenrilBrickConfig._instance.getBlockInstance().getDefaultState()) {
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
