package org.cyclops.integrateddynamics.world.gen;

import net.minecraft.block.trees.Tree;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.blockstateprovider.WeightedBlockStateProvider;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureSpread;
import net.minecraft.world.gen.feature.TwoLayerFeature;
import net.minecraft.world.gen.foliageplacer.MegaPineFoliagePlacer;
import net.minecraft.world.gen.trunkplacer.GiantTrunkPlacer;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockMenrilLogFilledConfig;
import org.cyclops.integrateddynamics.world.biome.BiomeMeneglinConfig;
import org.cyclops.integrateddynamics.world.gen.foliageplacer.FoliagePlacerMenril;
import org.cyclops.integrateddynamics.world.gen.trunkplacer.TrunkPlacerMenril;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * A Menril tree.
 * @author rubensworks
 */
public class TreeMenril extends Tree {

    public static BaseTreeFeatureConfig getMenrilTreeConfig() {
        return new BaseTreeFeatureConfig.Builder(
                new WeightedBlockStateProvider()
                        .add(RegistryEntries.BLOCK_MENRIL_LOG.defaultBlockState(), BlockMenrilLogFilledConfig.filledMenrilLogChance)
                        .add(RegistryEntries.BLOCK_MENRIL_LOG_FILLED.defaultBlockState(), 1),
                new SimpleBlockStateProvider(RegistryEntries.BLOCK_MENRIL_LEAVES.defaultBlockState()),
                new FoliagePlacerMenril(FeatureSpread.fixed(2), FeatureSpread.fixed(0)),
                new TrunkPlacerMenril(5, 2, 2, 3),
                new TwoLayerFeature(1, 0, 2))
                .ignoreVines()
                .build();
    }

    @Nullable
    @Override
    protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getConfiguredFeature(Random random, boolean b) {
        return Feature.TREE.configured(getMenrilTreeConfig());
    }

}
