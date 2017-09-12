package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import net.minecraft.init.Blocks;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockStairs;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;

/**
 * Config for the Menril Wood Stairs.
 * @author rubensworks
 *
 */
public class BlockMenrilPlanksStairsConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockMenrilPlanksStairsConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockMenrilPlanksStairsConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "menril_planks_stairs",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return (ConfigurableBlockStairs) new ConfigurableBlockStairs(this, BlockMenrilLog.getInstance().getDefaultState()) {
            @SuppressWarnings("deprecation")
            @Override
            public SoundType getSoundType() {
                return SoundType.WOOD;
            }
        }.setHardness(2.0F);
    }
    
    @Override
    public String getOreDictionaryId() {
        return Reference.DICT_STAIRWOOD;
    }
    
    @Override
    public void onRegistered() {
    	Blocks.FIRE.setFireInfo(getBlockInstance(), 5, 20);
    }

}
