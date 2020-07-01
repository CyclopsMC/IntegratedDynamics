package org.cyclops.integrateddynamics.world.gen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.block.LogBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
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
    
    private static final int RADIUS = 2;

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

        if (!isSoil(world, blockPos.down(), config.getSapling())
                || !isSoil(world, blockPos.down().offset(Direction.NORTH), config.getSapling())
                || !isSoil(world, blockPos.down().offset(Direction.SOUTH), config.getSapling())
                || !isSoil(world, blockPos.down().offset(Direction.EAST), config.getSapling())
                || !isSoil(world, blockPos.down().offset(Direction.WEST), config.getSapling())
                || !this.hasSpace((IWorld) world, blockPos, treeHeight)) {
            return false;
        }

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
                    for (xOffset = x - RADIUS; xOffset <= x + RADIUS; ++xOffset) {
                        int xPos = xOffset - x;
                        int t = xPos >> 31;
                        xPos = (xPos + t) ^ t;

                        for (zOffset = z - RADIUS; zOffset <= z + RADIUS; ++zOffset) {
                            int zPos = zOffset - z;
                            zPos = (zPos + (t = zPos >> 31)) ^ t;
                            BlockPos loopPos = new BlockPos(xOffset, yOffset, zOffset);

                            if ((xPos != RADIUS | zPos != RADIUS) &&
                                    !((yOffset == y + treeHeight || yOffset == y - 5 + treeHeight) && (xPos == RADIUS || zPos == RADIUS))) {
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

    public boolean hasSpace(IWorld world, BlockPos pos, int height) {
        for (int y = 0; y <= height; y++) {
            for (int x = -RADIUS; x <= RADIUS; x++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    BlockPos pos1 = pos.add(x, y, z);
                    if (pos1.getY() >= 255 || !(world.getBlockState(pos).canBeReplacedByLeaves(world, pos1)
                            || world.getBlockState(pos1).getBlock().isIn(BlockTags.SAPLINGS)
                            || world.getBlockState(pos1).getBlock() == Blocks.VINE
                            || world.getBlockState(pos1).getBlock() instanceof BushBlock)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
