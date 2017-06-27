package org.cyclops.integrateddynamics.fluid;

import net.minecraftforge.fluids.Fluid;
import org.cyclops.cyclopscore.config.configurable.ConfigurableFluid;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.config.extendedconfig.FluidConfig;

/**
 * The Liquid Chorus {@link Fluid}.
 * @author rubensworks
 *
 */
public class FluidLiquidChorus extends ConfigurableFluid {

    private static FluidLiquidChorus _instance = null;

    /**
     * Get the unique instance.
     * @return The unique instance.
     */
    public static FluidLiquidChorus getInstance() {
        return _instance;
    }

    public FluidLiquidChorus(ExtendedConfig<FluidConfig> eConfig) {
        super(eConfig);
        setDensity(1500); // How tick the fluid is, affects movement inside the liquid.
        setViscosity(3000); // How fast the fluid flows.
    }

}
