package org.cyclops.integrateddynamics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;


/**
 * Config for the Menril Torch.
 * @author rubensworks
 *
 */
public class BlockMenrilTorchConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockMenrilTorchConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_torch",
                (eConfig, properties) -> new TorchBlock(ParticleTypes.FLAME, properties
                        .noCollission()
                        .strength(0)
                        .lightLevel((blockState) -> 14)
                        .sound(SoundType.WOOD)) {
                    @Override
                    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
                        // No particles
                    }
                },
                (eConfig, block) -> new StandingAndWallBlockItem(block,
                        RegistryEntries.BLOCK_MENRIL_TORCH_WALL.get(),
                        Direction.DOWN, eConfig.createDefaultItemProperties())
        );
    }

}
