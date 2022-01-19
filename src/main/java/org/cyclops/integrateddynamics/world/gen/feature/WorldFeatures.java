package org.cyclops.integrateddynamics.world.gen.feature;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.TreePlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import org.cyclops.integrateddynamics.Reference;

import java.util.List;

/**
 * @author rubensworks
 */
public class WorldFeatures {

    public static final ConfiguredFeature<?, ?> CONFIGURED_FLOWERS_MENEGLIN = registerConfigured("flowers_meneglin", Feature.FLOWER
            .configured(FeatureUtils.simpleRandomPatchConfiguration(64, Feature.SIMPLE_BLOCK
                    .configured(new SimpleBlockConfiguration(new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                            .add(Blocks.BLUE_ORCHID.defaultBlockState(), 200)
                            .add(Blocks.OXEYE_DAISY.defaultBlockState(), 70)
                            .add(Blocks.WHITE_TULIP.defaultBlockState(), 70)
                            .add(Blocks.LILY_OF_THE_VALLEY.defaultBlockState(), 70)
                    ))).onlyWhenEmpty())));
    public static final ConfiguredFeature<?, ?> CONFIGURED_TREES_MENEGLIN = registerConfigured("trees_meneglin", Feature.RANDOM_SELECTOR
            .configured(new RandomFeatureConfiguration(List.of(
                    new WeightedPlacedFeature(TreePlacements.FANCY_OAK_BEES_002, 0.1F)
            ), TreePlacements.OAK_BEES_002)));
    public static final PlacedFeature PLACED_FLOWERS_MENEGLIN = registerPlaced("flowers_meneglin", CONFIGURED_FLOWERS_MENEGLIN.placed(
            CountPlacement.of(3),
            RarityFilter.onAverageOnceEvery(2),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP,
            BiomeFilter.biome()));
    public static final PlacedFeature PLACED_TREES_MENEGLIN = registerPlaced("trees_meneglin", CONFIGURED_TREES_MENEGLIN.placed(
            VegetationPlacements.treePlacement(PlacementUtils.countExtra(6, 0.1F, 1))
    ));

    public static <FC extends FeatureConfiguration> ConfiguredFeature<FC, ?> registerConfigured(String key, ConfiguredFeature<FC, ?> feature) {
        return Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(Reference.MOD_ID, key), feature);
    }

    public static PlacedFeature registerPlaced(String key, PlacedFeature feature) {
        return Registry.register(BuiltinRegistries.PLACED_FEATURE, new ResourceLocation(Reference.MOD_ID, key), feature);
    }

    public static void load() {
        // Just to trigger class loading
    }

}
