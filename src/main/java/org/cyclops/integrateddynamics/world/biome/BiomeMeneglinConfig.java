package org.cyclops.integrateddynamics.world.biome;

import com.google.common.collect.Lists;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BiomeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

import java.util.List;

/**
 * Config for {@link BiomeMeneglin}.
 * @author rubensworks
 *
 */
public class BiomeMeneglinConfig extends BiomeConfig {

    @ConfigurableProperty(category = "biome", comment = "The weight of spawning.", minimalValue = 0)
    public static int spawnWeight = 5;

    @ConfigurableProperty(category = "worldgeneration", comment = "List of dimension IDs in which the meneglin biome should not generate.")
    public static List<String> meneglinBiomeDimensionBlacklist = Lists.newArrayList(
            "minecraft:nether",
            "minecraft:the_end"
    );

    public BiomeMeneglinConfig() {
        super(
                IntegratedDynamics._instance,
                "meneglin",
                eConfig -> new BiomeMeneglin()
        );
    }
    
    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        Biome biome = this.getInstance();
        if (spawnWeight > 0) {
            BiomeManager.addBiome(BiomeManager.BiomeType.COOL, new BiomeManager.BiomeEntry(biome, spawnWeight));
        }
        BiomeManager.addSpawnBiome(biome);
        BiomeDictionary.addTypes(biome,
                BiomeDictionary.Type.OVERWORLD,
                BiomeDictionary.Type.COLD,
                BiomeDictionary.Type.MAGICAL
        );
    }
    
}
