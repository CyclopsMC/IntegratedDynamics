package org.cyclops.integrateddynamics.world.gen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LogBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.gen.IWorldGenerationReader;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockMenrilLogFilled;
import org.cyclops.integrateddynamics.block.BlockMenrilLogFilledConfig;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

/**
 * The Menril tree feature.
 * @author rubensworks
 */
public class WorldFeatureTreeMenril extends AbstractTreeFeature<NoFeatureConfig> {

    public WorldFeatureTreeMenril(Function<Dynamic<?>, ? extends NoFeatureConfig> configIn, boolean doBlockNotifyIn) {
        super(configIn, doBlockNotifyIn);
        this.setSapling((net.minecraftforge.common.IPlantable) RegistryEntries.BLOCK_MENRIL_SAPLING);
    }

    protected int baseHeight() {
        return 7;
    }

    protected int baseHeightRandomRange() {
        return 4;
    }

    public BlockState getLeaves() {
        return RegistryEntries.BLOCK_MENRIL_LEAVES.getDefaultState();
    }

    public BlockState getLogs() {
        return RegistryEntries.BLOCK_MENRIL_LOG.getDefaultState();
    }

    public BlockState getLogsFilled() {
        return RegistryEntries.BLOCK_MENRIL_LOG_FILLED.getDefaultState();
    }

    @Override
    protected boolean place(Set<BlockPos> changedBlocks, IWorldGenerationReader world, Random rand,
                            BlockPos blockPos, MutableBoundingBox boundingBox) {
        int treeHeight = rand.nextInt(baseHeightRandomRange()) + baseHeight();
        int worldHeight = world.getMaxHeight();
        Block block;

        if (blockPos.getY() >= 1 && blockPos.getY() + treeHeight + 1 <= worldHeight) {
            int xOffset;
            int yOffset;
            int zOffset;

            BlockPos basePos = blockPos.add(0, -1, 0);
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
                                this.setLogState(changedBlocks, world, loopPos, getLeaves(), boundingBox);
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
                    boolean filled = BlockMenrilLogFilledConfig.filledMenrilLogChance > 0
                            && rand.nextInt(BlockMenrilLogFilledConfig.filledMenrilLogChance) == 0;
                    BlockState logs = filled ? getLogsFilled()
                            .with(BlockMenrilLogFilled.SIDE, Direction.Plane.HORIZONTAL.random(rand)) : getLogs();
                    this.setLogState(changedBlocks, world, loopPos, logs
                                    .with(LogBlock.AXIS, pair.getLeft() ? Direction.Axis.X : Direction.Axis.Y),
                            boundingBox);
                }

                return true;
            }
        }
        return false;
    }

}
