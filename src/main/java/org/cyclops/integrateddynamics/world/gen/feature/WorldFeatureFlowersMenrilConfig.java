package org.cyclops.integrateddynamics.world.gen.feature;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
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
                eConfig -> new WorldFeatureFlowersMenril(NoFeatureConfig.field_236558_a_)
        );
        MinecraftForge.EVENT_BUS.addListener(this::onBiomeLoadingEvent);
    }

    public void onBiomeLoadingEvent(BiomeLoadingEvent event) {
        if (event.getName().equals(new ResourceLocation("integrateddynamics:meneglin"))) {
            event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).add(() -> ((WorldFeatureFlowersMenril) getInstance())
                    .withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG)
                    .withPlacement(Features.Placements.VEGETATION_PLACEMENT));
        }
    }
    
}
