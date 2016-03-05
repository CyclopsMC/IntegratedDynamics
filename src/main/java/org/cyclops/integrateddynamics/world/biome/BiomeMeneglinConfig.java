package org.cyclops.integrateddynamics.world.biome;

import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
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
        BiomeManager.addBiome(BiomeManager.BiomeType.COOL, new BiomeManager.BiomeEntry(getBiome(), 10));
        BiomeManager.addSpawnBiome(getBiome());
        BiomeManager.addStrongholdBiome(getBiome());
        BiomeManager.addVillageBiome(getBiome(), true);
        BiomeDictionary.registerBiomeType(getBiome(),
                BiomeDictionary.Type.FOREST, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.LUSH);
    }
    
}
