package org.cyclops.integrateddynamics.world.gen;

import net.minecraft.block.trees.Tree;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.blockstateprovider.WeightedBlockStateProvider;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliageplacer.AcaciaFoliagePlacer;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockMenrilLogFilledConfig;
import org.cyclops.integrateddynamics.world.gen.feature.WorldFeatureTreeMenril;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * A Menril tree.
 * @author rubensworks
 */
public class TreeMenril extends Tree {

    public static TreeFeatureConfig getMenrilTreeConfig() {
        return new TreeFeatureConfig.Builder(
                new WeightedBlockStateProvider()
                    .func_227407_a_(RegistryEntries.BLOCK_MENRIL_LOG.getDefaultState(), BlockMenrilLogFilledConfig.filledMenrilLogChance)
                    .func_227407_a_(RegistryEntries.BLOCK_MENRIL_LOG_FILLED.getDefaultState(), 1),
                new SimpleBlockStateProvider(RegistryEntries.BLOCK_MENRIL_LEAVES.getDefaultState()),
                new AcaciaFoliagePlacer(2, 0))
                .baseHeight(5)
                .heightRandA(2)
                .heightRandB(2)
                .trunkHeight(0)
                .ignoreVines()
                .setSapling((net.minecraftforge.common.IPlantable) RegistryEntries.BLOCK_MENRIL_SAPLING)
                .build();
    }

    @Nullable
    @Override
    protected ConfiguredFeature<TreeFeatureConfig, ?> getTreeFeature(Random random, boolean b) {
        return new WorldFeatureTreeMenril(TreeFeatureConfig::func_227338_a_).withConfiguration(getMenrilTreeConfig());
    }

}
