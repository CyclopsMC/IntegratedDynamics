package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlock;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;

/**
 * Config for the Menril Planks.
 * @author rubensworks
 *
 */
public class BlockMenrilPlanksConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockMenrilPlanksConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockMenrilPlanksConfig() {
        super(
                IntegratedDynamics._instance,
        	true,
            "menrilPlanks",
            null,
            null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return (ConfigurableBlock) new ConfigurableBlock(this, Material.WOOD) {
            @SuppressWarnings("deprecation")
            @Override
            public SoundType getSoundType() {
                return SoundType.WOOD;
            }
        }.setHardness(2.0F);
    }
    
    @Override
    public String getOreDictionaryId() {
        return Reference.DICT_WOODPLANK;
    }
    
    @Override
    public void onRegistered() {
    	Blocks.FIRE.setFireInfo(getBlockInstance(), 5, 20);
    }
    
}
