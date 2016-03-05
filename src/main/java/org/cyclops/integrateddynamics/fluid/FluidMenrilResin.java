package org.cyclops.integrateddynamics.fluid;

import net.minecraftforge.fluids.Fluid;
import org.cyclops.cyclopscore.config.configurable.ConfigurableFluid;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.config.extendedconfig.FluidConfig;

/**
 * The Menril Resin {@link Fluid}.
 * @author rubensworks
 *
 */
public class FluidMenrilResin extends ConfigurableFluid {
    
    private static FluidMenrilResin _instance = null;
    
    /**
     * Get the unique instance.
     * @return The unique instance.
     */
    public static FluidMenrilResin getInstance() {
        return _instance;
    }

    public FluidMenrilResin(ExtendedConfig<FluidConfig> eConfig) {
        super(eConfig);
        setDensity(1500); // How tick the fluid is, affects movement inside the liquid.
        setViscosity(3000); // How fast the fluid flows.
    }

}
