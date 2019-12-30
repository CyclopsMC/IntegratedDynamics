package org.cyclops.integrateddynamics.world.gen;

import net.minecraft.block.trees.Tree;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import org.cyclops.integrateddynamics.world.gen.feature.WorldFeatureTreeMenril;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * A Menril tree.
 * @author rubensworks
 */
public class TreeMenril extends Tree {

    @Nullable
    @Override
    protected AbstractTreeFeature<NoFeatureConfig> getTreeFeature(Random random) {
        return new WorldFeatureTreeMenril(NoFeatureConfig::deserialize, true);
    }
}
