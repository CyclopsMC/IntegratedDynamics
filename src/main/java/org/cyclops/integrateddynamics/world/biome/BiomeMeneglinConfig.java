package org.cyclops.integrateddynamics.world.biome;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurablePropertyData;
import org.cyclops.cyclopscore.config.extendedconfig.BiomeConfig;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.world.gen.TreeMenril;
import org.cyclops.integrateddynamics.world.gen.feature.WorldFeatures;

/**
 * Config for the meneglin biome.
 * @author rubensworks
 *
 */
public class BiomeMeneglinConfig extends BiomeConfig {

    @ConfigurableProperty(category = "biome", comment = "The weight of spawning in the overworld, 0 disables spawning.", minimalValue = 0)
    public static int spawnWeight = 5;

    @ConfigurableProperty(category = "worldgeneration", comment = "The chance at which a Menril Tree will spawn in the wild, the higher this value, the lower the chance.", minimalValue = 0, requiresMcRestart = true, configLocation = ModConfig.Type.SERVER)
    public static int wildMenrilTreeChance = 100;

    public static Holder<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE_TREE;
    public static Holder<PlacedFeature> PLACED_FEATURE_MENEGLIN;
    public static Holder<PlacedFeature> PLACED_FEATURE_GENERAL;

    public BiomeMeneglinConfig() {
        super(
                IntegratedDynamics._instance,
                "meneglin",
                eConfig -> {
                    // A lot of stuff is copied from forest biome: OverworldBiomes.forest
                    BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder();
                    //OverworldBiomes.globalOverworldGeneration(biomegenerationsettings$builder);
                    BiomeDefaultFeatures.addDefaultCarversAndLakes(biomegenerationsettings$builder);
                    BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
                    BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
                    BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
                    BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
                    BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
                    //BiomeDefaultFeatures.addForestFlowers(biomegenerationsettings$builder);

                    BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
                    BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
                    //BiomeDefaultFeatures.addOtherBirchTrees(biomegenerationsettings$builder);

                    //BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
                    BiomeDefaultFeatures.addForestGrass(biomegenerationsettings$builder);

                    biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, WorldFeatures.PLACED_TREES_MENEGLIN);
                    biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, WorldFeatures.PLACED_FLOWERS_MENEGLIN);

                    //BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
                    BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
                    MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
                    BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
                    BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
                    mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));

                    return (new Biome.BiomeBuilder())
                            .precipitation(Biome.Precipitation.RAIN)
                            .temperature(0.7F)
                            .downfall(0.25F)
                            .specialEffects((new BiomeSpecialEffects.Builder())
                                    .waterColor(4445678)
                                    .waterFogColor(Helpers.RGBToInt(85, 168, 221))
                                    .fogColor(12638463)
                                    .grassColorOverride(Helpers.RGBToInt(85, 221, 168))
                                    .foliageColorOverride(Helpers.RGBToInt(128, 208, 185))
                                    .skyColor(Helpers.RGBToInt(178, 238, 233))
                                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                    .backgroundMusic(null)
                                    .build())
                            .mobSpawnSettings(mobspawnsettings$builder.build())
                            .generationSettings(biomegenerationsettings$builder.build())
                            .build();
                }
        );
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onModSetup);
    }

    @Override
    public void onConfigPropertyReload(ConfigurablePropertyData<?> configProperty, boolean reload) {
        if (!reload) {
            if (configProperty.getName().equals("meneglin.spawnWeight") && spawnWeight > 0) {
                BiomeManager.addBiome(BiomeManager.BiomeType.COOL, new BiomeManager.BiomeEntry(getResourceKey(), spawnWeight));
            }
        }
    }

    public void onModSetup(FMLCommonSetupEvent event) {
        CONFIGURED_FEATURE_TREE = WorldFeatures.registerConfigured("tree_menril", new ConfiguredFeature<>(Feature.TREE, TreeMenril.getMenrilTreeConfig()));

        PLACED_FEATURE_MENEGLIN = WorldFeatures.registerPlaced("tree_menril_meneglin", new PlacedFeature(CONFIGURED_FEATURE_TREE,
                VegetationPlacements.treePlacement(PlacementUtils.countExtra(1, 0.05F, 1))));
        PLACED_FEATURE_GENERAL = WorldFeatures.registerPlaced("tree_menril_general", new PlacedFeature(CONFIGURED_FEATURE_TREE,
                VegetationPlacements.treePlacement(PlacementUtils.countExtra(0, 1F / wildMenrilTreeChance, 1))));
    }
}
