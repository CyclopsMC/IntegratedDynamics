package org.cyclops.integrateddynamics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Stripped Menril Wood.
 * @author rubensworks
 *
 */
public class BlockMenrilWoodConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockMenrilWoodConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_wood",
                (eConfig, properties) -> new RotatedPillarBlock(properties
                        .mapColor(MapColor.COLOR_CYAN)
                        .strength(2.0F)
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
