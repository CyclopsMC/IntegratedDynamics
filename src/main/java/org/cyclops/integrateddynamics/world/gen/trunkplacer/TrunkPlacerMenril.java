package org.cyclops.integrateddynamics.world.gen.trunkplacer;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockMenrilLogFilled;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author rubensworks
 */
public class TrunkPlacerMenril extends TrunkPlacer {
    public static final Codec<TrunkPlacerMenril> CODEC = RecordCodecBuilder.create((builder) -> trunkPlacerParts(builder)
            .and(Codec.intRange(0, 32).fieldOf("trunk_height_wider").forGetter((placer) -> placer.heightWider))
            .apply(builder, TrunkPlacerMenril::new));

    protected final int heightWider;

    public TrunkPlacerMenril(int baseHeight, int heightRandA, int heightRandB, int heightWider) {
        super(baseHeight, heightRandA, heightRandB);
        this.heightWider = heightWider;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return RegistryEntries.TRUNK_PLACER_MENRIL;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader world, BiConsumer<BlockPos, BlockState> callback, RandomSource rand, int height,
                                                            BlockPos pos, TreeConfiguration config) {
        // Only generate if stump is fully on ground (other checks are done in TreeFeature.place)
        BlockPos basePos = pos.below();
        if (!TreeFeature.isGrassOrDirt(world, basePos.north())
                || !TreeFeature.isGrassOrDirt(world, basePos.east())
                || !TreeFeature.isGrassOrDirt(world, basePos.south())
                || !TreeFeature.isGrassOrDirt(world, basePos.west())) {
            return Collections.emptyList();
        }

        // Ensure dirt is below tree
        BlockPos posStump = pos.below();
        setDirtAt(world, callback, rand, posStump, config);
        setDirtAt(world, callback, rand, posStump.north(), config);
        setDirtAt(world, callback, rand, posStump.east(), config);
        setDirtAt(world, callback, rand, posStump.south(), config);
        setDirtAt(world, callback, rand, posStump.west(), config);

        // Create stump
        placeLog(world, callback, rand, pos.north(), config, Function.identity());
        placeLog(world, callback, rand, pos.east(), config, Function.identity());
        placeLog(world, callback, rand, pos.south(), config, Function.identity());
        placeLog(world, callback, rand, pos.west(), config, Function.identity());

        // Create base trunk
        for(int i = 0; i < height; ++i) {
            placeLog(world, callback, rand, pos.above(i), config, Function.identity());
        }

        // Create wider trunk
        for(int i = height; i < height + heightWider; ++i) {
            BlockPos posIt = pos.above(i);
            placeLog(world, callback, rand, posIt, config, Function.identity());
            placeLog(world, callback, rand, posIt.north(), config, Function.identity());
            placeLog(world, callback, rand, posIt.east(), config, Function.identity());
            placeLog(world, callback, rand, posIt.south(), config, Function.identity());
            placeLog(world, callback, rand, posIt.west(), config, Function.identity());
        }

        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(pos.above(height + heightWider), 0 /*radius*/, false));
    }

    protected boolean placeLog(LevelSimulatedReader p_161887_, BiConsumer<BlockPos, BlockState> p_161888_, RandomSource p_161889_, BlockPos p_161890_, TreeConfiguration p_161891_, Function<BlockState, BlockState> p_161892_) {
        if (TreeFeature.validTreePos(p_161887_, p_161890_)) {
            BlockState logs = p_161892_.apply(p_161891_.trunkProvider.getState(p_161889_, p_161890_));
            logs = logs.getBlock() instanceof BlockMenrilLogFilled
                    ? logs.setValue(BlockMenrilLogFilled.SIDE, Direction.Plane.HORIZONTAL.getRandomDirection(p_161889_))
                    : logs;
            p_161888_.accept(p_161890_, logs);
            return true;
        } else {
            return false;
        }
    }

}
