package org.cyclops.integrateddynamics.world.gen.trunkplacer;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockMenrilLogFilled;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author rubensworks
 */
public class TrunkPlacerMenril extends TrunkPlacer {
    public static final MapCodec<TrunkPlacerMenril> CODEC = RecordCodecBuilder.mapCodec((builder) -> trunkPlacerParts(builder)
            .and(Codec.intRange(0, 32).fieldOf("trunk_height_wider").forGetter((placer) -> placer.heightWider))
            .apply(builder, TrunkPlacerMenril::new));

    protected final int heightWider;

    public TrunkPlacerMenril(int baseHeight, int heightRandA, int heightRandB, int heightWider) {
        super(baseHeight, heightRandA, heightRandB);
        this.heightWider = heightWider;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return RegistryEntries.TRUNK_PLACER_MENRIL.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(WorldGenLevel world, BiConsumer<BlockPos, BlockState> callback, RandomSource rand, int height,
                                                            BlockPos pos, TreeFeature tree) {
        // Only generate if stump is fully on ground (other checks are done in TreeFeature.place)
        BlockPos basePos = pos;
        if (!TreeFeature.isAirOrLeaves(world, basePos.north())
                && !TreeFeature.isAirOrLeaves(world, basePos.east())
                && !TreeFeature.isAirOrLeaves(world, basePos.south())
                && !TreeFeature.isAirOrLeaves(world, basePos.west())) {
            // all adjacent positions are blocked, skip
            return ImmutableList.of();
        }

        // Ensure dirt is below tree
        BlockPos posStump = pos.below();
        setDirtAt(world, callback, rand, posStump, tree);

        // Create stump
        if (placeLog(world, callback, rand, pos.north(), tree, Function.identity())) {
            setDirtAt(world, callback, rand, posStump.north(), tree);
        }
        if (placeLog(world, callback, rand, pos.east(), tree, Function.identity())) {
            setDirtAt(world, callback, rand, posStump.east(), tree);
        }
        if (placeLog(world, callback, rand, pos.south(), tree, Function.identity())) {
            setDirtAt(world, callback, rand, posStump.south(), tree);
        }
        if (placeLog(world, callback, rand, pos.west(), tree, Function.identity())) {
            setDirtAt(world, callback, rand, posStump.west(), tree);
        }

        // Create base trunk
        for(int i = 0; i < height; ++i) {
            placeLog(world, callback, rand, pos.above(i), tree, Function.identity());
        }

        // Create wider trunk
        for(int i = height; i < height + heightWider; ++i) {
            BlockPos posIt = pos.above(i);
            placeLog(world, callback, rand, posIt, tree, Function.identity());
            placeLog(world, callback, rand, posIt.north(), tree, Function.identity());
            placeLog(world, callback, rand, posIt.east(), tree, Function.identity());
            placeLog(world, callback, rand, posIt.south(), tree, Function.identity());
            placeLog(world, callback, rand, posIt.west(), tree, Function.identity());
        }

        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(pos.above(height + heightWider), 0 /*radius*/, false));
    }

    protected boolean placeLog(WorldGenLevel world, BiConsumer<BlockPos, BlockState> callback, RandomSource rand, BlockPos pos, TreeFeature tree, Function<BlockState, BlockState> transformer) {
        if (TreeFeature.validTreePos(world, pos)) {
            BlockState logs = transformer.apply(tree.trunkProvider().value().getState(world, rand, pos));
            logs = logs.getBlock() instanceof BlockMenrilLogFilled
                    ? logs.setValue(BlockMenrilLogFilled.SIDE, Direction.Plane.HORIZONTAL.getRandomDirection(rand))
                    : logs;
            callback.accept(pos, logs);
            return true;
        } else {
            return false;
        }
    }

    protected void setDirtAt(WorldGenLevel world, BiConsumer<BlockPos, BlockState> callback, RandomSource rand, BlockPos pos, TreeFeature tree) {
        placeBelowTrunkBlock(world, callback, rand, pos, tree);
    }

}
