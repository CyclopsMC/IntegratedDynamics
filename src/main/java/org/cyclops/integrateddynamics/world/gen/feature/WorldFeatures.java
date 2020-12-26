package org.cyclops.integrateddynamics.world.gen.feature;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.blockplacer.SimpleBlockPlacer;
import net.minecraft.world.gen.blockstateprovider.WeightedBlockStateProvider;
import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.MultipleRandomFeatureConfig;
import net.minecraft.world.gen.placement.AtSurfaceWithExtraConfig;
import net.minecraft.world.gen.placement.Placement;
import org.cyclops.integrateddynamics.Reference;

/**
 * @author rubensworks
 */
public class WorldFeatures {

    public static final ConfiguredFeature<?, ?> FLOWERS_MENEGLIN = register("flowers_meneglin", Feature.RANDOM_PATCH
            .withConfiguration((new BlockClusterFeatureConfig.Builder((new WeightedBlockStateProvider())
                    .addWeightedBlockstate(Blocks.BLUE_ORCHID.getDefaultState(), 200)
                    .addWeightedBlockstate(Blocks.OXEYE_DAISY.getDefaultState(), 70)
                    .addWeightedBlockstate(Blocks.WHITE_TULIP.getDefaultState(), 70)
                    .addWeightedBlockstate(Blocks.LILY_OF_THE_VALLEY.getDefaultState(), 70), SimpleBlockPlacer.PLACER))
                    .tries(64).build())
            .withPlacement(Features.Placements.VEGETATION_PLACEMENT)
            .withPlacement(Features.Placements.HEIGHTMAP_PLACEMENT)
            .func_242731_b(2));
    public static final ConfiguredFeature<?, ?> TREES_MENEGLIN = register("trees_meneglin", Feature.RANDOM_SELECTOR
            .withConfiguration(new MultipleRandomFeatureConfig(ImmutableList.of(Features.FANCY_OAK_BEES_0002.withChance(0.1F)), Features.OAK_BEES_0002))
            .withPlacement(Features.Placements.HEIGHTMAP_PLACEMENT)
            .withPlacement(Placement.COUNT_EXTRA
                    .configure(new AtSurfaceWithExtraConfig(2, 0.1F, 1))));

    private static <FC extends IFeatureConfig> ConfiguredFeature<FC, ?> register(String key, ConfiguredFeature<FC, ?> feature) {
        return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation(Reference.MOD_ID, key), feature);
    }

    public static void load() {
        // Just to trigger class loading
    }

}
