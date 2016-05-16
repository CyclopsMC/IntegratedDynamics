package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import net.minecraft.init.Blocks;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockLog;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;

/**
 * Config for the Menril Log.
 * @author rubensworks
 *
 */
public class BlockMenrilLogConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockMenrilLogConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockMenrilLogConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "menrilLog",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return (ConfigurableBlockLog) new ConfigurableBlockLog(this){
            @Override
            public SoundType getSoundType() {
                return SoundType.WOOD;
            }
        }.setHardness(2.0F);
    }
    
    @Override
    public String getOreDictionaryId() {
        return Reference.DICT_WOODLOG;
    }
    
    @Override
    public void onRegistered() {
    	Blocks.FIRE.setFireInfo(getBlockInstance(), 5, 20);
    }
    
}
