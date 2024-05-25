package org.cyclops.integrateddynamics.fluid;

import net.minecraft.world.item.Rarity;
import org.cyclops.cyclopscore.config.extendedconfig.FluidConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for Menril Resin.
 * @author rubensworks
 *
 */
public class FluidMenrilResinConfig extends FluidConfig {

    public FluidMenrilResinConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_resin",
                fluidConfig -> getDefaultFluidProperties(IntegratedDynamics._instance,
                        "block/menril_resin",
                        builder -> builder
                                .density(1500)
                                .viscosity(3000)
                                .rarity(Rarity.RARE)
                                .descriptionId("block.integrateddynamics.block_menril_resin"))
                        .bucket(RegistryEntries.ITEM_BUCKET_MENRIL_RESIN::get)
                        .block(RegistryEntries.BLOCK_FLUID_MENRIL_RESIN::get)
        );
    }
}
