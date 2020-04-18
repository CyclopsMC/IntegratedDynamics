package org.cyclops.integrateddynamics.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.FlowersFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.Random;
import java.util.function.Function;

/**
 * @author rubensworks
 */
public class WorldFeatureFlowersMenril extends FlowersFeature<NoFeatureConfig> {

    public WorldFeatureFlowersMenril(Function<Dynamic<?>, ? extends NoFeatureConfig> config) {
        super(config);
    }

    public boolean func_225559_a_(IWorld world, BlockPos blockPos, NoFeatureConfig config) {
        return true; // Set block
    }

    public int func_225560_a_(NoFeatureConfig config) {
        return 1; // Attempts
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
