package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockStairs;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Menril Planks Stairs.
 * @author rubensworks
 *
 */
public class BlockCrystalizedMenrilBlockStairsConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockCrystalizedMenrilBlockStairsConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockCrystalizedMenrilBlockStairsConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "crystalized_menril_block_stairs",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
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
