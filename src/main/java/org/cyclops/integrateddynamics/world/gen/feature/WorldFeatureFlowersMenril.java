package org.cyclops.integrateddynamics.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.FlowersFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.Random;
import java.util.function.Function;

/**
 * @author rubensworks
 */
public class WorldFeatureFlowersMenril extends FlowersFeature {

    public WorldFeatureFlowersMenril(Function<Dynamic<?>, ? extends NoFeatureConfig> config) {
        super(config);
    }

    @Override
    public BlockState getRandomFlower(Random random, BlockPos blockPos) {
        if (random.nextFloat() > 0.33F) {
            return Blocks.BLUE_ORCHID.getDefaultState();
        } else if (random.nextFloat() > 0.67F) {
            return Blocks.OXEYE_DAISY.getDefaultState();
        } else {
            return Blocks.WHITE_TULIP.getDefaultState();
        }
    }

}
