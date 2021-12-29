package org.cyclops.integrateddynamics.world.gen;

import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
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
public class TreeMenril extends AbstractTreeGrower {

    public static TreeConfiguration getMenrilTreeConfig() {
        return new TreeConfiguration.TreeConfigurationBuilder(
                new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                        .add(RegistryEntries.BLOCK_MENRIL_LOG.defaultBlockState(), BlockMenrilLogFilledConfig.filledMenrilLogChance)
                        .add(RegistryEntries.BLOCK_MENRIL_LOG_FILLED.defaultBlockState(), 1)),
                new TrunkPlacerMenril(5, 2, 2, 3),
                BlockStateProvider.simple(RegistryEntries.BLOCK_MENRIL_LEAVES),
                new FoliagePlacerMenril(ConstantInt.of(2), ConstantInt.of(0)),
                new TwoLayersFeatureSize(1, 0, 2))
                .ignoreVines()
                .build();
    }

    @Nullable
    @Override
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random random, boolean b) {
        return Feature.TREE.configured(getMenrilTreeConfig());
    }

}
