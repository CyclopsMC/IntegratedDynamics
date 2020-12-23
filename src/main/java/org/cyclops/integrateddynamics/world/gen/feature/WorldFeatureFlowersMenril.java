package org.cyclops.integrateddynamics.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.FlowersFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.Random;

/**
 * @author rubensworks
 */
public class WorldFeatureFlowersMenril extends FlowersFeature<NoFeatureConfig> {

    public WorldFeatureFlowersMenril(Codec<NoFeatureConfig> config) {
        super(config);
    }

    @Override
    public boolean isValidPosition(IWorld world, BlockPos pos, NoFeatureConfig config) {
        return true;
    }

    @Override
    public int getFlowerCount(NoFeatureConfig config) {
        return 1;
    }

    public BlockPos getNearbyPos(Random random, BlockPos blockPos, NoFeatureConfig config) {
        return blockPos;
    }

    @Override
    public BlockState getFlowerToPlace(Random random, BlockPos blockPos, NoFeatureConfig config) {
        if (random.nextFloat() > 0.33F) {
            return Blocks.BLUE_ORCHID.getDefaultState();
        } else if (random.nextFloat() > 0.67F) {
            return Blocks.OXEYE_DAISY.getDefaultState();
        } else {
            return Blocks.WHITE_TULIP.getDefaultState();
        }
    }

}
