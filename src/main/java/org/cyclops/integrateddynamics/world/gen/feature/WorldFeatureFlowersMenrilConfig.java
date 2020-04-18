package org.cyclops.integrateddynamics.world.gen.feature;

import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;
import org.cyclops.cyclopscore.config.extendedconfig.WorldFeatureConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

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

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();

        // Register feature in Meneglin biome
        RegistryEntries.BIOME_MENEGLIN.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, ((WorldFeatureFlowersMenril) getInstance())
                .withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG)
                .withPlacement(Placement.COUNT_HEIGHTMAP_32
                        .configure(new FrequencyConfig(70))));
    }
    
}
