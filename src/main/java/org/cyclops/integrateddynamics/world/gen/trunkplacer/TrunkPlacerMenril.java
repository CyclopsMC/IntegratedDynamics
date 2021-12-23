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

import static net.minecraft.world.gen.feature.TreeFeature.isGrassOrDirtOrFarmland;

/**
 * @author rubensworks
 */
public class TrunkPlacerMenril extends AbstractTrunkPlacer {
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
    public List<FoliagePlacer.Foliage> placeTrunk(IWorldGenerationReader world, Random rand, int height,
                                                      BlockPos pos, Set<BlockPos> changedBlocks,
                                                      MutableBoundingBox bounds, BaseTreeFeatureConfig config) {
        // Only generate if stump is fully on ground (other checks are done in TreeFeature.place)
        BlockPos basePos = pos.below();
        if (!isGrassOrDirtOrFarmland(world, basePos.north())
                || !isGrassOrDirtOrFarmland(world, basePos.east())
                || !isGrassOrDirtOrFarmland(world, basePos.south())
                || !isGrassOrDirtOrFarmland(world, basePos.west())) {
            return Collections.emptyList();
        }

        // Ensure dirt is below tree
        BlockPos posStump = pos.below();
        setDirtAt(world, posStump);
        setDirtAt(world, posStump.north());
        setDirtAt(world, posStump.east());
        setDirtAt(world, posStump.south());
        setDirtAt(world, posStump.west());

        // Create stump
        placeLog(world, rand, pos.north(), changedBlocks, bounds, config);
        placeLog(world, rand, pos.east(), changedBlocks, bounds, config);
        placeLog(world, rand, pos.south(), changedBlocks, bounds, config);
        placeLog(world, rand, pos.west(), changedBlocks, bounds, config);

        // Create base trunk
        for(int i = 0; i < height; ++i) {
            placeLog(world, rand, pos.above(i), changedBlocks, bounds, config);
        }

        // Create wider trunk
        for(int i = height; i < height + heightWider; ++i) {
            BlockPos posIt = pos.above(i);
            placeLog(world, rand, posIt, changedBlocks, bounds, config);
            placeLog(world, rand, posIt.north(), changedBlocks, bounds, config);
            placeLog(world, rand, posIt.east(), changedBlocks, bounds, config);
            placeLog(world, rand, posIt.south(), changedBlocks, bounds, config);
            placeLog(world, rand, posIt.west(), changedBlocks, bounds, config);
        }

        return ImmutableList.of(new FoliagePlacer.Foliage(pos.above(height + heightWider), 0 /*radius*/, false));
    }

    // static override
    protected static boolean placeLog(IWorldGenerationReader p_236911_0_, Random p_236911_1_, BlockPos p_236911_2_, Set<BlockPos> p_236911_3_, MutableBoundingBox p_236911_4_, BaseTreeFeatureConfig p_236911_5_) {
        if (TreeFeature.validTreePos(p_236911_0_, p_236911_2_)) {
            BlockState logs = p_236911_5_.trunkProvider.getState(p_236911_1_, p_236911_2_);
            logs = logs.getBlock() instanceof BlockMenrilLogFilled
                    ? logs.setValue(BlockMenrilLogFilled.SIDE, Direction.Plane.HORIZONTAL.getRandomDirection(p_236911_1_))
                    : logs;
            setBlock(p_236911_0_, p_236911_2_, logs, p_236911_4_);
            p_236911_3_.add(p_236911_2_.immutable());
            return true;
        } else {
            return false;
        }
    }

}
