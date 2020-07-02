package org.cyclops.integrateddynamics.world.gen.feature;

import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.placement.AtSurfaceWithExtraConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.fml.config.ModConfig;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.WorldFeatureConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.world.gen.TreeMenril;

/**
 * Config for {@link WorldFeatureTreeMenril}.
 * @author rubensworks
 *
 */
public class WorldFeatureTreeMenrilConfig extends WorldFeatureConfig {

    @ConfigurableProperty(category = "worldgeneration", comment = "The chance at which a Menril Tree will spawn in the wild, the higher this value, the lower the chance.", minimalValue = 0, requiresMcRestart = true, configLocation = ModConfig.Type.SERVER)
    public static int wildMenrilTreeChance = 100;

    public WorldFeatureTreeMenrilConfig() {
        super(
                IntegratedDynamics._instance,
                "tree_menril",
                eConfig -> new WorldFeatureTreeMenril(TreeFeatureConfig::func_227338_a_)
        );
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();

        // Register feature in Meneglin biome
        // We must do this here because the biomes are constructed before the features.
        RegistryEntries.BIOME_MENEGLIN.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, ((WorldFeatureTreeMenril) getInstance())
                .withConfiguration(TreeMenril.getMenrilTreeConfig())
                .withPlacement(Placement.COUNT_EXTRA_HEIGHTMAP.configure(new AtSurfaceWithExtraConfig(1, 0.05F, 1))));

        // Register feature in other biomes
        GenerationStage.Decoration decoration = GenerationStage.Decoration.VEGETAL_DECORATION;
        ConfiguredFeature<?, ?> feature = ((WorldFeatureTreeMenril) getInstance())
                .withConfiguration(TreeMenril.getMenrilTreeConfig())
                .withPlacement(Placement.COUNT_EXTRA_HEIGHTMAP.configure(new AtSurfaceWithExtraConfig(0, 1F / wildMenrilTreeChance, 1)));

        Biomes.PLAINS.addFeature(decoration, feature);
        Biomes.MOUNTAINS.addFeature(decoration, feature);
        Biomes.FOREST.addFeature(decoration, feature);
        Biomes.SWAMP.addFeature(decoration, feature);
        Biomes.RIVER.addFeature(decoration, feature);
        Biomes.SNOWY_TUNDRA.addFeature(decoration, feature);
        Biomes.SNOWY_MOUNTAINS.addFeature(decoration, feature);
        Biomes.MUSHROOM_FIELDS.addFeature(decoration, feature);
        Biomes.MUSHROOM_FIELD_SHORE.addFeature(decoration, feature);
        Biomes.WOODED_HILLS.addFeature(decoration, feature);
        Biomes.TAIGA_HILLS.addFeature(decoration, feature);
        Biomes.BIRCH_FOREST.addFeature(decoration, feature);
        Biomes.JUNGLE_HILLS.addFeature(decoration, feature);
        Biomes.BIRCH_FOREST.addFeature(decoration, feature);
        Biomes.DARK_FOREST.addFeature(decoration, feature);
        Biomes.SNOWY_TAIGA_HILLS.addFeature(decoration, feature);
        Biomes.GIANT_TREE_TAIGA_HILLS.addFeature(decoration, feature);
        Biomes.SAVANNA_PLATEAU.addFeature(decoration, feature);
        Biomes.WOODED_BADLANDS_PLATEAU.addFeature(decoration, feature);
        Biomes.SUNFLOWER_PLAINS.addFeature(decoration, feature);
        Biomes.FLOWER_FOREST.addFeature(decoration, feature);
        Biomes.TAIGA_MOUNTAINS.addFeature(decoration, feature);
        Biomes.SWAMP_HILLS.addFeature(decoration, feature);
        Biomes.TALL_BIRCH_FOREST.addFeature(decoration, feature);
        Biomes.TALL_BIRCH_HILLS.addFeature(decoration, feature);
        Biomes.DARK_FOREST_HILLS.addFeature(decoration, feature);
        Biomes.SNOWY_TAIGA_MOUNTAINS.addFeature(decoration, feature);
        Biomes.GIANT_SPRUCE_TAIGA_HILLS.addFeature(decoration, feature);
    }
}
