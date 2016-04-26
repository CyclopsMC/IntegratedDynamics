package org.cyclops.integrateddynamics.world.biome;

import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.BiomeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;

/**
 * Config for {@link BiomeMeneglin}.
 * @author rubensworks
 *
 */
public class BiomeMeneglinConfig extends BiomeConfig {
    
    /**
     * The unique instance.
     */
    public static BiomeMeneglinConfig _instance;

    /**
     * The weight of spawning in a cool biome type.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.BIOME, comment = "The weight of spawning in a cool biome type.")
    public static int spawnWeightCool = 4;

    /**
     * The weight of spawning in a forest biome type.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.BIOME, comment = "The weight of spawning in a forest biome type.")
    public static int spawnWeightForest = 6;

    /**
     * The weight of spawning in a magical biome type.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.BIOME, comment = "The weight of spawning in a magical biome type.")
    public static int spawnWeightMagical = 10;

    /**
     * The weight of spawning in a lush biome type.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.BIOME, comment = "The weight of spawning in a lush biome type.")
    public static int spawnWeightLush = 8;

    /**
     * Make a new instance.
     */
    public BiomeMeneglinConfig() {
        super(
                IntegratedDynamics._instance,
                Reference.BIOME_MENEGLIN,
                "biomeMeneglin",
                null,
                BiomeMeneglin.class
        );
    }
    
    @Override
    public void registerBiomeDictionary() {
        if (spawnWeightCool > 0) {
            BiomeManager.addBiome(BiomeManager.BiomeType.COOL, new BiomeManager.BiomeEntry(getBiome(), spawnWeightCool));
        }
        if (spawnWeightForest > 0) {
            BiomeManager.addBiome(BiomeManager.BiomeType.COOL, new BiomeManager.BiomeEntry(getBiome(), spawnWeightForest));
        }
        if (spawnWeightMagical > 0) {
            BiomeManager.addBiome(BiomeManager.BiomeType.COOL, new BiomeManager.BiomeEntry(getBiome(), spawnWeightMagical));
        }
        if (spawnWeightLush > 0) {
            BiomeManager.addBiome(BiomeManager.BiomeType.COOL, new BiomeManager.BiomeEntry(getBiome(), spawnWeightLush));
        }
        BiomeManager.addSpawnBiome(getBiome());
        BiomeManager.addStrongholdBiome(getBiome());
        BiomeManager.addVillageBiome(getBiome(), true);
        BiomeDictionary.registerBiomeType(getBiome(),
                BiomeDictionary.Type.COLD,
                BiomeDictionary.Type.FOREST,
                BiomeDictionary.Type.MAGICAL,
                BiomeDictionary.Type.LUSH
        );
    }
    
}
