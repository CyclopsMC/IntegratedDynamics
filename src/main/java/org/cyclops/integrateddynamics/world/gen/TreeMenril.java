package org.cyclops.integrateddynamics.world.gen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.cyclops.integrateddynamics.Reference;

import javax.annotation.Nullable;

/**
 * A Menril tree.
 * @author rubensworks
 */
public class TreeMenril extends AbstractTreeGrower {
    @Nullable
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean b) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(Reference.MOD_ID, "tree_menril"));
    }
}
