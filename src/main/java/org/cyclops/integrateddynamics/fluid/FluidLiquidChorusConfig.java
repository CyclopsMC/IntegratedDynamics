package org.cyclops.integrateddynamics.fluid;

import net.minecraft.world.item.Rarity;
import org.cyclops.cyclopscore.config.extendedconfig.FluidConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for Liquid Chorus.
 * @author rubensworks
 *
 */
public class FluidLiquidChorusConfig extends FluidConfig {

    public FluidLiquidChorusConfig() {
        super(
                IntegratedDynamics._instance,
                "liquid_chorus",
                fluidConfig -> getDefaultFluidProperties(IntegratedDynamics._instance,
                        "block/liquid_chorus",
                        builder -> builder
                                .density(1500)
                                .viscosity(3000)
                                .rarity(Rarity.EPIC)
                                .descriptionId("block.integrateddynamics.block_liquid_chorus"))
                        .bucket(RegistryEntries.ITEM_BUCKET_LIQUID_CHORUS::get)
                        .block(RegistryEntries.BLOCK_FLUID_LIQUID_CHORUS::get)
        );
    }

}
