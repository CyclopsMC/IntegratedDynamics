package org.cyclops.integrateddynamics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Menril Fence.
 * @author rubensworks
 *
 */
public class BlockMenrilFenceConfig extends BlockConfig {

    public BlockMenrilFenceConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_fence",
                eConfig -> new FenceBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_CYAN)
                        .strength(2.0F, 3.0F)
                        .sound(SoundType.WOOD)) {
                    @Override
                    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                        return 5;
                    }

                    @Override
                    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                        return 20;
                    }
                },
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
