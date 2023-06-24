package org.cyclops.integrateddynamics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Menril Leaves.
 * @author rubensworks
 *
 */
public class BlockMenrilLeavesConfig extends BlockConfig {

    public BlockMenrilLeavesConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_leaves",
                eConfig -> new LeavesBlock(Block.Properties.of()
                        .replaceable()
                        .strength(0.2F)
                        .randomTicks()
                        .sound(SoundType.GRASS)
                        .noOcclusion()) {
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

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        ComposterBlock.COMPOSTABLES.put(getItemInstance(), 0.3F);
    }

}
