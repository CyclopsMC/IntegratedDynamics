package org.cyclops.integrateddynamics.fluid;

import org.cyclops.cyclopscore.config.extendedconfig.FluidConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link FluidMenrilResin}.
 * @author rubensworks
 *
 */
public class FluidMenrilResinConfig extends FluidConfig {
    
    /**
     * The unique instance.
     */
    public static FluidMenrilResinConfig _instance;

    /**
     * Make a new instance.
     */
    public FluidMenrilResinConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "menrilresin",
                null,
                FluidMenrilResin.class
        );
    }
}
