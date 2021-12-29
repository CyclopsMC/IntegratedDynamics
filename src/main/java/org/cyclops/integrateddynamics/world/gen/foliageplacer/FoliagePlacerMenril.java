package org.cyclops.integrateddynamics.world.gen.foliageplacer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import org.cyclops.integrateddynamics.RegistryEntries;

import java.util.Random;
import java.util.function.BiConsumer;

/**
 * @author rubensworks
 */
public class FoliagePlacerMenril extends FoliagePlacer {
    public static final Codec<FoliagePlacerMenril> CODEC = RecordCodecBuilder.create((builder) -> foliagePlacerParts(builder)
            .apply(builder, FoliagePlacerMenril::new));

    public FoliagePlacerMenril(IntProvider radius, IntProvider offset) {
        super(radius, offset);
    }

    protected FoliagePlacerType<?> type() {
        return RegistryEntries.FOLIAGE_PLACER_MENRIL;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader world, BiConsumer<BlockPos, BlockState> callback, Random rand, TreeConfiguration config,
                                 int mimimumHeight, FoliagePlacer.FoliageAttachment foliage, int foliageHeight, int spread, int offset) {
        BlockPos blockpos = foliage.pos();
        for(int l = offset; l >= -foliageHeight; --l) {
            int radius = (l == offset || l == -foliageHeight) ? 1 : 2;
            this.placeLeavesRow(world, callback, rand, config, blockpos, radius, l, foliage.doubleTrunk());
        }
    }

    // MCP: foliage height
    @Override
    public int foliageHeight(Random rand, int treeHeight, TreeConfiguration config) {
        return 5;
    }

    @Override
    protected boolean shouldSkipLocation(Random rand, int p_230373_2_, int p_230373_3_, int p_230373_4_, int p_230373_5_, boolean p_230373_6_) {
        return p_230373_2_ == p_230373_5_ && p_230373_4_ == p_230373_5_ && p_230373_5_ > 0; // ???
    }
}
