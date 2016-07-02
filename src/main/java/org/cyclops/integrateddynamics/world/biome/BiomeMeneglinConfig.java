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
    @ConfigurableProperty(category = ConfigurableTypeCategory.BIOME, comment = "The weight of spawning.")
    public static int spawnWeight = 3;

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
        if (spawnWeight > 0) {
            BiomeManager.addBiome(BiomeManager.BiomeType.COOL, new BiomeManager.BiomeEntry(getBiome(), spawnWeight));
        }
        BiomeManager.addSpawnBiome(getBiome());
        BiomeManager.addStrongholdBiome(getBiome());
        BiomeManager.addVillageBiome(getBiome(), true);
        BiomeDictionary.registerBiomeType(getBiome(),
                BiomeDictionary.Type.COLD,
                BiomeDictionary.Type.MAGICAL
        );
    }
    
}
