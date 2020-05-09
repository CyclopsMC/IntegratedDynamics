package org.cyclops.integrateddynamics.world.gen;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.world.gen.WorldGeneratorTree;
import org.cyclops.integrateddynamics.Configs;
import org.cyclops.integrateddynamics.block.BlockMenrilLeavesConfig;
import org.cyclops.integrateddynamics.block.BlockMenrilLogConfig;
import org.cyclops.integrateddynamics.block.BlockMenrilLogFilled;
import org.cyclops.integrateddynamics.block.BlockMenrilLogFilledConfig;
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
            IBlockState blockState = world.getBlockState(basePos);
            block = blockState.getBlock();
            int x = blockPos.getX();
            int y = blockPos.getY();
            int z = blockPos.getZ();

            if ((block != null && block.canSustainPlant(blockState, world, basePos, EnumFacing.UP,
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
                                IBlockState loopBlockState = world.getBlockState(loopPos);
                                block = loopBlockState.getBlock();

                                if (block != null && !(block.isLeaves(loopBlockState, world, loopPos) ||
                                        block == Blocks.AIR ||
                                        block.canBeReplacedByLeaves(loopBlockState, world, loopPos))) {
                                    return false;
                                }
                            }
                        }
                    } else {
                        return false;
                    }
                }

                if (block != null) {
                    block.onPlantGrow(blockState, world, basePos, blockPos);

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
                                IBlockState loopBlockState = world.getBlockState(loopPos);
                                block = loopBlockState.getBlock();

                                if ((xPos != center | zPos != center) &&
                                        !((yOffset == y + treeHeight || yOffset == y - 5 + treeHeight) && (xPos == center || zPos == center)) &&
                                        (block == null || block.isLeaves(loopBlockState, world, loopPos) ||
                                                block == Blocks.AIR ||
                                                block.canBeReplacedByLeaves(loopBlockState, world, loopPos))) {
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
                    logLocations.add(Pair.of(true, blockPos.add(-1, 0, 0)));
                    logLocations.add(Pair.of(true, blockPos.add(1, 0, 0)));
                    logLocations.add(Pair.of(true, blockPos.add(0, 0, -1)));
                    logLocations.add(Pair.of(true, blockPos.add(0, 0, 1)));

                    for(Pair<Boolean, BlockPos> pair : logLocations) {
                        BlockPos loopPos = pair.getRight();
                        IBlockState loopBlockState = world.getBlockState(loopPos);
                        block = loopBlockState.getBlock();
                        if (block == null || block == Blocks.AIR ||
                                block.isLeaves(loopBlockState, world, loopPos) ||
                                block.isReplaceable(world, loopPos)) {
                            boolean filled = Configs.isEnabled(BlockMenrilLogFilledConfig.class)
                                    && BlockMenrilLogFilledConfig.filledMenrilLogChance > 0
                                    && rand.nextInt(BlockMenrilLogFilledConfig.filledMenrilLogChance) == 0;
                            IBlockState logs = filled ? BlockMenrilLogFilled.getInstance().getDefaultState()
                                    .withProperty(BlockMenrilLogFilled.SIDE, rand.nextInt(4)) : getLogs().getDefaultState();
                            this.setBlockAndNotifyAdequately(world, loopPos,
                                    logs.withProperty(BlockLog.LOG_AXIS, pair.getLeft() ? BlockLog.EnumAxis.NONE : BlockLog.EnumAxis.Y)
                            );
                        }
                    }

                    return true;
                }
            }
        }
        return false;
    }
}
