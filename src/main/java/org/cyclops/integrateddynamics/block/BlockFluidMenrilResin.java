package org.cyclops.integrateddynamics.block;

import net.minecraft.block.material.Material;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockFluidClassic;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.fluid.FluidMenrilResin;

/**
 * A blockState for the {@link org.cyclops.integrateddynamics.fluid.FluidMenrilResin} fluid.
 * @author rubensworks
 *
 */
public class BlockFluidMenrilResin extends ConfigurableBlockFluidClassic {

    private static BlockFluidMenrilResin _instance = null;
    
    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static BlockFluidMenrilResin getInstance() {
        return _instance;
    }

    public BlockFluidMenrilResin(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig, FluidMenrilResin.getInstance(), Material.WATER);
        
        if (MinecraftHelpers.isClientSide())
            this.setParticleColor(0.654901961F, 0.870588235F, 0.780392157F);
    }

}
