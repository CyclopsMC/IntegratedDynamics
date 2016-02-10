package org.cyclops.integrateddynamics.world.gen;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockSapling;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.world.gen.WorldGeneratorTree;
import org.cyclops.integrateddynamics.block.BlockMenrilLeavesConfig;
import org.cyclops.integrateddynamics.block.BlockMenrilLogConfig;
import org.cyclops.integrateddynamics.block.BlockMenrilSaplingConfig;

import java.util.List;
import java.util.Random;

/**
 * @author rubensworks
 */
public class WorldGeneratorMenrilTree extends WorldGeneratorTree {
    /**
     * Make a new instance.
     *
     * @param doNotify If the generator should notify the world.
     */
    public WorldGeneratorMenrilTree(boolean doNotify) {
        super(doNotify);
    }

    @Override
    protected int baseHeight() {
        return 7;
    }

    @Override
    protected int baseHeightRandomRange() {
        return 4;
    }

    @Override
    public BlockLeaves getLeaves() {
        return (BlockLeaves) BlockMenrilLeavesConfig._instance.getBlockInstance();
    }

    @Override
    public BlockLog getLogs() {
        return (BlockLog) BlockMenrilLogConfig._instance.getBlockInstance();
    }

    @Override
    public BlockSapling getSapling() {
        return (BlockSapling) BlockMenrilSaplingConfig._instance.getBlockInstance();
    }

    public boolean growTree(World world, Random rand, BlockPos blockPos) {
        int treeHeight = rand.nextInt(baseHeightRandomRange()) + baseHeight();
        int worldHeight = world.getHeight();
        Block block;

        if (blockPos.getY() >= 1 && blockPos.getY() + treeHeight + 1 <= worldHeight) {
            int xOffset;
            int yOffset;
            int zOffset;

            BlockPos basePos = blockPos.add(0, -1, 0);
            block = world.getBlockState(basePos).getBlock();
            int x = blockPos.getX();
            int y = blockPos.getY();
            int z = blockPos.getZ();

            if ((block != null && block.canSustainPlant(world, basePos, EnumFacing.UP,
                    getSapling())) && y < worldHeight - treeHeight - 1) {
                for (yOffset = y; yOffset <= y + 1 + treeHeight; ++yOffset) {
                    byte radius = 1;

                    if (yOffset == y) {
                        radius = 0;
                    }

                    if (yOffset >= y + 4) {
                        radius = 3;
                    }

                    if (yOffset >= y + 1 + treeHeight - 3) {
                        radius = 5;
                    }

                    if (yOffset >= y + 1 + treeHeight - 1) {
                        radius = 3;
                    }

                    // Check if leaves can be placed
                    if (yOffset >= 0 & yOffset < worldHeight) {
                        for (xOffset = x - radius; xOffset <= x + radius; ++xOffset) {
                            for (zOffset = z - radius; zOffset <= z + radius; ++zOffset) {
                                BlockPos loopPos = new BlockPos(xOffset, yOffset, zOffset);
                                block = world.getBlockState(loopPos).getBlock();

                                if (block != null && !(block.isLeaves(world, loopPos) ||
                                        block == Blocks.air ||
                                        block.canBeReplacedByLeaves(world, loopPos))) {
                                    return false;
                                }
                            }
                        }
                    } else {
                        return false;
                    }
                }

                block = world.getBlockState(basePos).getBlock();
                if (block != null) {
                    block.onPlantGrow(world, basePos, blockPos);

                    // Add leaves
                    for (yOffset = y - 5 + treeHeight; yOffset <= y + treeHeight; ++yOffset) {
                        int center = (yOffset <= treeHeight && yOffset >= treeHeight - 1) ? 4 : 2;

                        for (xOffset = x - center; xOffset <= x + center; ++xOffset) {
                            int xPos = xOffset - x;
                            int t = xPos >> 31;
                            xPos = (xPos + t) ^ t;

                            for (zOffset = z - center; zOffset <= z + center; ++zOffset) {
                                int zPos = zOffset - z;
                                zPos = (zPos + (t = zPos >> 31)) ^ t;
                                BlockPos loopPos = new BlockPos(xOffset, yOffset, zOffset);

                                block = world.getBlockState(loopPos).getBlock();

                                if ((xPos != center | zPos != center) &&
                                        !((yOffset == y + treeHeight || yOffset == y - 5 + treeHeight) && (xPos == center || zPos == center)) &&
                                        (block == null || block.isLeaves(world, loopPos) ||
                                                block == Blocks.air ||
                                                block.canBeReplacedByLeaves(world, loopPos))) {
                                    this.setBlockAndNotifyAdequately(world, loopPos, getLeaves().getDefaultState());
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
                    logLocations.add(Pair.of(false, blockPos.add(-1, 0, 0)));
                    logLocations.add(Pair.of(false, blockPos.add(1, 0, 0)));
                    logLocations.add(Pair.of(false, blockPos.add(0, 0, -1)));
                    logLocations.add(Pair.of(false, blockPos.add(0, 0, 1)));

                    for(Pair<Boolean, BlockPos> pair : logLocations) {
                        BlockPos loopPos = pair.getRight();
                        if (block == null || block == Blocks.air ||
                                block.isLeaves(world, loopPos) ||
                                block.isReplaceable(world, loopPos)) {
                            this.setBlockAndNotifyAdequately(world, loopPos,
                                    getLogs().getDefaultState().withProperty(BlockLog.LOG_AXIS, pair.getLeft() ? BlockLog.EnumAxis.NONE : BlockLog.EnumAxis.Y));
                        }
                    }

                    return true;
                }
            }
        }
        return false;
    }
}
