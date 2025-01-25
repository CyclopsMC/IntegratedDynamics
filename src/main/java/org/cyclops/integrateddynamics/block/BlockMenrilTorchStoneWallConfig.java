package org.cyclops.integrateddynamics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

import java.util.Collection;
import java.util.Collections;

/**
 * Config for the Menril Stone Torch (wall).
 * @author rubensworks
 *
 */
public class BlockMenrilTorchStoneWallConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockMenrilTorchStoneWallConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_torch_stone_wall",
                (eConfig, properties) -> {
                    return new WallTorchBlock(ParticleTypes.FLAME, properties
                            .noCollission()
                            .strength(0)
                            .lightLevel((blockState) -> 14)
                            .sound(SoundType.STONE)) {
                        @Override
                        @OnlyIn(Dist.CLIENT)
                        public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
                            // No particles
                        }
                    };
                },
                null
        );
    }

    @Override
    public Collection<ItemStack> getDefaultCreativeTabEntries() {
        return Collections.emptyList();
    }

}
