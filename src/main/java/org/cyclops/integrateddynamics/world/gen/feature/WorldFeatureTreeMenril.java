package org.cyclops.integrateddynamics.world.gen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.LogBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.gen.IWorldGenerationReader;
import net.minecraft.world.gen.feature.AbstractSmallTreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.block.BlockMenrilLogFilled;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

/**
 * The Menril tree feature.
 * @author rubensworks
 */
public class WorldFeatureTreeMenril extends AbstractSmallTreeFeature<TreeFeatureConfig> {

    public WorldFeatureTreeMenril(Function<Dynamic<?>, TreeFeatureConfig> configIn) {
        super(configIn);
    }

    protected int baseHeight() {
        return 7;
    }

    protected int baseHeightRandomRange() {
        return 4;
    }

    @Override
    protected boolean func_225557_a_(IWorldGenerationReader world, Random rand, BlockPos blockPos,
                                     Set<BlockPos> changedBlocksTrunk, Set<BlockPos> changedBlocksFoliage,
                                     MutableBoundingBox boundingBox, TreeFeatureConfig config) {
        int treeHeight = rand.nextInt(baseHeightRandomRange()) + baseHeight();
        int worldHeight = world.getMaxHeight();

        if (blockPos.getY() >= 1 && blockPos.getY() + treeHeight + 1 <= worldHeight) {
            int xOffset;
            int yOffset;
            int zOffset;

            int x = blockPos.getX();
            int y = blockPos.getY();
            int z = blockPos.getZ();

            if (y < worldHeight - treeHeight - 1) {
                // Add leaves
                for (yOffset = y - 5 + treeHeight; yOffset <= y + treeHeight; ++yOffset) {
                    int center = 2;

                    for (xOffset = x - center; xOffset <= x + center; ++xOffset) {
                        int xPos = xOffset - x;
                        int t = xPos >> 31;
                        xPos = (xPos + t) ^ t;

                        for (zOffset = z - center; zOffset <= z + center; ++zOffset) {
                            int zPos = zOffset - z;
                            zPos = (zPos + (t = zPos >> 31)) ^ t;
                            BlockPos loopPos = new BlockPos(xOffset, yOffset, zOffset);

                            if ((xPos != center | zPos != center) &&
                                    !((yOffset == y + treeHeight || yOffset == y - 5 + treeHeight) && (xPos == center || zPos == center))) {
                                this.func_227219_b_(world, rand, loopPos, changedBlocksFoliage, boundingBox, config);
                            }
                        }
                    }
                }

                // Replace replacable blocks with logs
                List<Pair<Boolean, BlockPos>> logLocations = Lists.newLinkedList();
                for (yOffset = 0; yOffset < treeHeight; ++yOffset) {

                    logLocations.add(Pair.of(false, blockPos.add(0, yOffset, 0)));

                    if(yOffset >= 1 + treeHeight - 5 && yOffset <= 1 + treeHeight - 1) {
                        logLocations.add(Pair.of(false, blockPos.add(-1, yOffset, 0)));
                        logLocations.add(Pair.of(false, blockPos.add(1, yOffset, 0)));
                        logLocations.add(Pair.of(false, blockPos.add(0, yOffset, -1)));
                        logLocations.add(Pair.of(false, blockPos.add(0, yOffset, 1)));
                    }
                }

                // Create stump
                logLocations.add(Pair.of(true, blockPos.add(-1, 0, 0)));
                logLocations.add(Pair.of(true, blockPos.add(1, 0, 0)));
                logLocations.add(Pair.of(true, blockPos.add(0, 0, -1)));
                logLocations.add(Pair.of(true, blockPos.add(0, 0, 1)));

                for(Pair<Boolean, BlockPos> pair : logLocations) {
                    BlockPos loopPos = pair.getRight();
                    BlockState logs = config.trunkProvider.getBlockState(rand, loopPos);
                    logs = logs.getBlock() instanceof BlockMenrilLogFilled
                            ? logs.with(BlockMenrilLogFilled.SIDE, Direction.Plane.HORIZONTAL.random(rand))
                            : logs;
                    // TODO: if pair.getLeft() is true, we should show a log without inner sides visible (used to be side NONE)
                    this.func_227217_a_(world, loopPos, logs.with(LogBlock.AXIS, Direction.Axis.Y), boundingBox);
                }

                return true;
            }
        }
        return false;
    }

}
