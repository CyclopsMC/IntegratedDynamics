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
     * The weight of spawning.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.BIOME, comment = "The weight of spawning.", minimalValue = 0)
    public static int spawnWeight = 5;

    /**
     * List of dimension IDs in which the meneglin biome should not generate.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.WORLDGENERATION, comment = "List of dimension IDs in which the meneglin biome should not generate.")
    public static int[] meneglinBiomeDimensionBlacklist = new int[]{-1, 1};

    /**
     * Make a new instance.
     */
    public BiomeMeneglinConfig() {
        super(
                IntegratedDynamics._instance,
                Reference.BIOME_MENEGLIN,
                "biome_meneglin",
                null,
                BiomeMeneglin.class
        );
    }
    
    @Override
    public void registerBiomeDictionary() {
        if (spawnWeight > 0) {
            BiomeManager.addBiome(BiomeManager.BiomeType.COOL, new BiomeManager.BiomeEntry(getBiome(), spawnWeight));
        }
        BiomeManager.addSpawnBiome(getBiome());
        BiomeManager.addStrongholdBiome(getBiome());
        BiomeManager.addVillageBiome(getBiome(), true);
        BiomeDictionary.addTypes(getBiome(),
                BiomeDictionary.Type.COLD,
                BiomeDictionary.Type.MAGICAL
        );
    }
    
}
