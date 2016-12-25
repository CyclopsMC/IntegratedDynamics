package org.cyclops.integrateddynamics.block;


import org.cyclops.cyclopscore.config.extendedconfig.BlockFluidConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockFluidMenrilResin}.
 * @author rubensworks
 *
 */
public class BlockFluidMenrilResinConfig extends BlockFluidConfig {
    
    /**
     * The unique instance.
     */
    public static BlockFluidMenrilResinConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockFluidMenrilResinConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "block_menril_resin",
                null,
                BlockFluidMenrilResin.class
        );
    }
    
    @Override
    public boolean isDisableable() {
        return false;
    }
    
}
