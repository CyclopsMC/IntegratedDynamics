package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlock;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Menril block.
 * @author rubensworks
 *
 */
public class BlockCrystalizedMenrilBlockConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockCrystalizedMenrilBlockConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockCrystalizedMenrilBlockConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "crystalized_menril_block",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        ConfigurableBlock block = (ConfigurableBlock) new ConfigurableBlock(this, Material.CLAY) {
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
