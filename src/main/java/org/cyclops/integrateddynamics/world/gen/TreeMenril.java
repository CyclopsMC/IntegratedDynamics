package org.cyclops.integrateddynamics.world.gen;

import net.minecraft.block.trees.Tree;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.blockstateprovider.WeightedBlockStateProvider;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureSpread;
import net.minecraft.world.gen.feature.TwoLayerFeature;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockMenrilLogFilledConfig;
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
                        .addWeightedBlockstate(RegistryEntries.BLOCK_MENRIL_LOG.getDefaultState(), BlockMenrilLogFilledConfig.filledMenrilLogChance)
                        .addWeightedBlockstate(RegistryEntries.BLOCK_MENRIL_LOG_FILLED.getDefaultState(), 1),
                new SimpleBlockStateProvider(RegistryEntries.BLOCK_MENRIL_LEAVES.getDefaultState()),
                new FoliagePlacerMenril(FeatureSpread.func_242252_a(2), FeatureSpread.func_242252_a(0)),
                new TrunkPlacerMenril(5, 2, 2, 3),
                new TwoLayerFeature(1, 0, 2))
                .setIgnoreVines()
                .build();
    }

    @Nullable
    @Override
    protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getTreeFeature(Random random, boolean b) {
        return Feature.TREE.withConfiguration(getMenrilTreeConfig());
    }

}
