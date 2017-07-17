package org.cyclops.integrateddynamics.fluid;

import org.cyclops.cyclopscore.config.extendedconfig.FluidConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link FluidLiquidChorus}.
 * @author rubensworks
 *
 */
public class FluidLiquidChorusConfig extends FluidConfig {

    /**
     * The unique instance.
     */
    public static FluidLiquidChorusConfig _instance;

    /**
     * Make a new instance.
     */
    public FluidLiquidChorusConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "liquidchorus",
                null,
                FluidLiquidChorus.class
        );
    }
}
