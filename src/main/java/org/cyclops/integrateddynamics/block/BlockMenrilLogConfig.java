package org.cyclops.integrateddynamics.block;

import net.minecraft.init.Blocks;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;

/**
 * Config for the Menril Wood.
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
                "menril_log",
                null,
                BlockMenrilLog.class
        );
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
