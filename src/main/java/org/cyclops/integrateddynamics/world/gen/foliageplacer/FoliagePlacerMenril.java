package org.cyclops.integrateddynamics.world.gen.foliageplacer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.gen.IWorldGenerationReader;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.FeatureSpread;
import net.minecraft.world.gen.foliageplacer.FoliagePlacer;
import net.minecraft.world.gen.foliageplacer.FoliagePlacerType;
import org.cyclops.integrateddynamics.RegistryEntries;

import java.util.Random;
import java.util.Set;

import net.minecraft.world.gen.foliageplacer.FoliagePlacer.Foliage;

/**
 * @author rubensworks
 */
public class FoliagePlacerMenril extends FoliagePlacer {
    public static final Codec<FoliagePlacerMenril> CODEC = RecordCodecBuilder.create((builder) -> foliagePlacerParts(builder)
            .apply(builder, FoliagePlacerMenril::new));

    public FoliagePlacerMenril(FeatureSpread radius, FeatureSpread offset) {
        super(radius, offset);
    }

    protected FoliagePlacerType<?> type() {
        return RegistryEntries.FOLIAGE_PLACER_MENRIL;
    }

    @Override
    protected void createFoliage(IWorldGenerationReader world, Random rand, BaseTreeFeatureConfig config,
                                  int mimimumHeight, Foliage foliage, int foliageHeight, int spread,
                                  Set<BlockPos> changedBlocks, int offset, MutableBoundingBox bounds) {
        BlockPos blockpos = foliage.foliagePos();
        for(int l = offset; l >= -foliageHeight; --l) {
            int radius = (l == offset || l == -foliageHeight) ? 1 : 2;
            this.placeLeavesRow(world, rand, config, blockpos, radius, changedBlocks, l, foliage.doubleTrunk(), bounds);
        }
    }

    // MCP: foliage height
    @Override
    public int foliageHeight(Random rand, int treeHeight, BaseTreeFeatureConfig config) {
        return 5;
    }

    @Override
    protected boolean shouldSkipLocation(Random rand, int p_230373_2_, int p_230373_3_, int p_230373_4_, int p_230373_5_, boolean p_230373_6_) {
        return p_230373_2_ == p_230373_5_ && p_230373_4_ == p_230373_5_ && p_230373_5_ > 0; // ???
    }
}
