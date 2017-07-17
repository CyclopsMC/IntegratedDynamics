package org.cyclops.integrateddynamics.block;


import org.cyclops.cyclopscore.config.extendedconfig.BlockFluidConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockFluidLiquidChorus}.
 * @author rubensworks
 *
 */
public class BlockFluidLiquidChorusConfig extends BlockFluidConfig {

    /**
     * The unique instance.
     */
    public static BlockFluidLiquidChorusConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockFluidLiquidChorusConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "block_liquid_chorus",
                null,
                BlockFluidLiquidChorus.class
        );
    }
    
    @Override
    public boolean isDisableable() {
        return false;
    }
    
}
