package org.cyclops.integrateddynamics.world.biome;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeAmbience;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.biome.BiomeMaker;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.biome.MoodSoundAmbience;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureSpread;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.TwoLayerFeature;
import net.minecraft.world.gen.feature.structure.StructureFeatures;
import net.minecraft.world.gen.foliageplacer.MegaPineFoliagePlacer;
import net.minecraft.world.gen.placement.AtSurfaceWithExtraConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilders;
import net.minecraft.world.gen.treedecorator.AlterGroundTreeDecorator;
import net.minecraft.world.gen.trunkplacer.GiantTrunkPlacer;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
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

    public static ConfiguredFeature<?, ?> CONFIGURED_FEATURE_GENERAL;
    public static ConfiguredFeature<?, ?> CONFIGURED_FEATURE_MENEGLIN;

    public BiomeMeneglinConfig() {
        super(
                IntegratedDynamics._instance,
                "meneglin",
                eConfig -> {
                    // A lot of stuff is copied from forest biome: BiomeMaker.makeGenericForestBiome
                    BiomeGenerationSettings.Builder generationBuilder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.GRASS);
                    DefaultBiomeFeatures.addDefaultOverworldLandStructures(generationBuilder);
                    generationBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
                    DefaultBiomeFeatures.addDefaultCarvers(generationBuilder);
                    DefaultBiomeFeatures.addDefaultLakes(generationBuilder);
                    DefaultBiomeFeatures.addDefaultMonsterRoom(generationBuilder);
                    // DefaultBiomeFeatures.withAllForestFlowerGeneration(biomegenerationsettings$builder);

                    DefaultBiomeFeatures.addDefaultUndergroundVariety(generationBuilder);
                    DefaultBiomeFeatures.addDefaultOres(generationBuilder);
                    DefaultBiomeFeatures.addDefaultSoftDisks(generationBuilder);
                    //DefaultBiomeFeatures.withForestBirchTrees(generationBuilder);
                    generationBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, WorldFeatures.TREES_MENEGLIN);
                    // DefaultBiomeFeatures.withDefaultFlowers(generationBuilder);
                    generationBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, WorldFeatures.FLOWERS_MENEGLIN);
                    DefaultBiomeFeatures.addForestGrass(generationBuilder);

                    // DefaultBiomeFeatures.withNormalMushroomGeneration(generationBuilder);
                    DefaultBiomeFeatures.addDefaultExtraVegetation(generationBuilder);
                    DefaultBiomeFeatures.addDefaultSprings(generationBuilder);
                    DefaultBiomeFeatures.addSurfaceFreezing(generationBuilder);
                    return (new Biome.Builder())
                            .precipitation(Biome.RainType.RAIN)
                            .biomeCategory(Biome.Category.FOREST)
                            .depth(0.4F)
                            .scale(0.4F)
                            .temperature(0.7F)
                            .downfall(0.25F)
                            .specialEffects((new BiomeAmbience.Builder())
                                    .waterColor(4445678)
                                    .waterFogColor(Helpers.RGBToInt(85, 168, 221))
                                    .fogColor(12638463)
                                    .grassColorOverride(Helpers.RGBToInt(85, 221, 168))
                                    .foliageColorOverride(Helpers.RGBToInt(128, 208, 185))
                                    .skyColor(Helpers.RGBToInt(178, 238, 233))
                                    .ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS)
                                    .build())
                            .mobSpawnSettings(BiomeMaker.defaultSpawns()
                                    .addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.RABBIT, 4, 2, 3))
                                    .build())
                            .generationSettings(generationBuilder.build())
                            .build();
                }
        );
        MinecraftForge.EVENT_BUS.addListener(this::onBiomeLoadingEvent);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onModSetup);
    }

    @Override
    public void onConfigPropertyReload(ConfigurablePropertyData<?> configProperty, boolean reload) {
        if (!reload) {
            if (configProperty.getName().equals("meneglin.spawnWeight") && spawnWeight > 0) {
                BiomeManager.addBiome(BiomeManager.BiomeType.COOL, new BiomeManager.BiomeEntry(getRegistryKey(), spawnWeight));
                BiomeDictionary.addTypes(getRegistryKey(), BiomeDictionary.Type.OVERWORLD);
                BiomeDictionary.addTypes(getRegistryKey(),
                        BiomeDictionary.Type.COLD,
                        BiomeDictionary.Type.DENSE,
                        BiomeDictionary.Type.WET,
                        BiomeDictionary.Type.CONIFEROUS,
                        BiomeDictionary.Type.MAGICAL,
                        BiomeDictionary.Type.FOREST);
            }
        }
    }

    public void onModSetup(FMLCommonSetupEvent event) {
        CONFIGURED_FEATURE_MENEGLIN = Registry.register(WorldGenRegistries.CONFIGURED_FEATURE,
                new ResourceLocation(getMod().getModId(), "tree_menril_meneglin"),
                Feature.TREE
                        .configured(TreeMenril.getMenrilTreeConfig())
                        .decorated(Placement.COUNT_EXTRA.configured(new AtSurfaceWithExtraConfig(1, 0.05F, 1))));
        CONFIGURED_FEATURE_GENERAL = Registry.register(WorldGenRegistries.CONFIGURED_FEATURE,
                new ResourceLocation(getMod().getModId(), "tree_menril_general"),
                Feature.TREE
                        .configured(TreeMenril.getMenrilTreeConfig())
                        .decorated(Placement.COUNT_EXTRA.configured(new AtSurfaceWithExtraConfig(0, 1F / wildMenrilTreeChance, 1))));
    }

    public void onBiomeLoadingEvent(BiomeLoadingEvent event) {
        if (event.getName().equals(new ResourceLocation("integrateddynamics:meneglin"))) {
            event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION)
                    .add(() -> CONFIGURED_FEATURE_MENEGLIN);
        } else if (BiomeDictionary.getTypes(RegistryKey.create(RegistryKey.createRegistryKey(getRegistry().getRegistryName()), event.getName()))
                .contains(BiomeDictionary.Type.OVERWORLD)) {
            event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION)
                    .add(() -> CONFIGURED_FEATURE_GENERAL);
        }
    }
    
}
