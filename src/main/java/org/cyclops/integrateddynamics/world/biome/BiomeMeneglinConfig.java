package org.cyclops.integrateddynamics.world.biome;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurablePropertyData;
import org.cyclops.cyclopscore.config.extendedconfig.BiomeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BiomeMeneglin}.
 * @author rubensworks
 *
 */
public class BiomeMeneglinConfig extends BiomeConfig {

    @ConfigurableProperty(category = "biome", comment = "The weight of spawning.", minimalValue = 0)
    public static int spawnWeight = 5;

    @ConfigurableProperty(category = "biome", comment = "If this biome should automatically generate in the overworld dimension.")
    public static boolean generateInOverworld = true;

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
        BiomeManager.addSpawnBiome(biome);
        BiomeDictionary.addTypes(biome,
                BiomeDictionary.Type.COLD,
                BiomeDictionary.Type.MAGICAL
        );
    }

    @Override
    public void onConfigPropertyReload(ConfigurablePropertyData<?> configProperty, boolean reload) {
        if (!reload) {
            Biome biome = this.getInstance();
            if (configProperty.getName().equals("meneglin.spawnWeight") && spawnWeight > 0) {
                BiomeManager.addBiome(BiomeManager.BiomeType.COOL, new BiomeManager.BiomeEntry(biome, spawnWeight));
            }
            if (configProperty.getName().equals("meneglin.generateInOverworld") && generateInOverworld) {
                BiomeDictionary.addTypes(biome, BiomeDictionary.Type.OVERWORLD);
            }
        }
    }
    
}
