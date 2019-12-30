package org.cyclops.integrateddynamics.world.gen.feature;

import net.minecraft.world.gen.feature.NoFeatureConfig;
import org.cyclops.cyclopscore.config.extendedconfig.WorldFeatureConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link WorldFeatureFlowersMenril}.
 * @author rubensworks
 *
 */
public class WorldFeatureFlowersMenrilConfig extends WorldFeatureConfig {

    public WorldFeatureFlowersMenrilConfig() {
        super(
                IntegratedDynamics._instance,
                "flowers_menril",
                eConfig -> new WorldFeatureFlowersMenril(NoFeatureConfig::deserialize)
        );
    }
    
}
