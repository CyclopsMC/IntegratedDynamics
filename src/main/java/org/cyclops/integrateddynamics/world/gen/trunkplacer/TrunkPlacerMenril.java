package org.cyclops.integrateddynamics.world.gen.trunkplacer;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.gen.IWorldGenerationReader;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.foliageplacer.FoliagePlacer;
import net.minecraft.world.gen.trunkplacer.AbstractTrunkPlacer;
import net.minecraft.world.gen.trunkplacer.TrunkPlacerType;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockMenrilLogFilled;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static net.minecraft.world.gen.feature.TreeFeature.isDirtOrFarmlandAt;

/**
 * @author rubensworks
 */
public class TrunkPlacerMenril extends AbstractTrunkPlacer {
    public static final Codec<TrunkPlacerMenril> CODEC = RecordCodecBuilder.create((builder) -> func_236915_a_(builder)
            .and(Codec.intRange(0, 32).fieldOf("trunk_height_wider").forGetter((placer) -> placer.heightWider))
            .apply(builder, TrunkPlacerMenril::new));

    protected final int heightWider;

    public TrunkPlacerMenril(int baseHeight, int heightRandA, int heightRandB, int heightWider) {
        super(baseHeight, heightRandA, heightRandB);
        this.heightWider = heightWider;
    }

    @Override
    protected TrunkPlacerType<?> func_230381_a_() {
        return RegistryEntries.TRUNK_PLACER_MENRIL;
    }

    @Override
    public List<FoliagePlacer.Foliage> func_230382_a_(IWorldGenerationReader world, Random rand, int height,
                                                      BlockPos pos, Set<BlockPos> changedBlocks,
                                                      MutableBoundingBox bounds, BaseTreeFeatureConfig config) {
        // Only generate if stump is fully on ground (other checks are done in TreeFeature.place)
        BlockPos basePos = pos.down();
        if (!isDirtOrFarmlandAt(world, basePos.north())
                || !isDirtOrFarmlandAt(world, basePos.east())
                || !isDirtOrFarmlandAt(world, basePos.south())
                || !isDirtOrFarmlandAt(world, basePos.west())) {
            return Collections.emptyList();
        }

        // Ensure dirt is below tree
        BlockPos posStump = pos.down();
        func_236909_a_(world, posStump);
        func_236909_a_(world, posStump.north());
        func_236909_a_(world, posStump.east());
        func_236909_a_(world, posStump.south());
        func_236909_a_(world, posStump.west());

        // Create stump
        func_236911_a_(world, rand, pos.north(), changedBlocks, bounds, config);
        func_236911_a_(world, rand, pos.east(), changedBlocks, bounds, config);
        func_236911_a_(world, rand, pos.south(), changedBlocks, bounds, config);
        func_236911_a_(world, rand, pos.west(), changedBlocks, bounds, config);

        // Create base trunk
        for(int i = 0; i < height; ++i) {
            func_236911_a_(world, rand, pos.up(i), changedBlocks, bounds, config);
        }

        // Create wider trunk
        for(int i = height; i < height + heightWider; ++i) {
            BlockPos posIt = pos.up(i);
            func_236911_a_(world, rand, posIt, changedBlocks, bounds, config);
            func_236911_a_(world, rand, posIt.north(), changedBlocks, bounds, config);
            func_236911_a_(world, rand, posIt.east(), changedBlocks, bounds, config);
            func_236911_a_(world, rand, posIt.south(), changedBlocks, bounds, config);
            func_236911_a_(world, rand, posIt.west(), changedBlocks, bounds, config);
        }

        return ImmutableList.of(new FoliagePlacer.Foliage(pos.up(height + heightWider), 0 /*radius*/, false));
    }

    // static override
    protected static boolean func_236911_a_(IWorldGenerationReader p_236911_0_, Random p_236911_1_, BlockPos p_236911_2_, Set<BlockPos> p_236911_3_, MutableBoundingBox p_236911_4_, BaseTreeFeatureConfig p_236911_5_) {
        if (TreeFeature.isReplaceableAt(p_236911_0_, p_236911_2_)) {
            BlockState logs = p_236911_5_.trunkProvider.getBlockState(p_236911_1_, p_236911_2_);
            logs = logs.getBlock() instanceof BlockMenrilLogFilled
                    ? logs.with(BlockMenrilLogFilled.SIDE, Direction.Plane.HORIZONTAL.random(p_236911_1_))
                    : logs;
            func_236913_a_(p_236911_0_, p_236911_2_, logs, p_236911_4_);
            p_236911_3_.add(p_236911_2_.toImmutable());
            return true;
        } else {
            return false;
        }
    }

}
