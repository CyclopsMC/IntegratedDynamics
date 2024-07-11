package org.cyclops.integrateddynamics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

import java.util.Collection;
import java.util.Collections;

/**
 * Config for the Menril Torch (wall).
 * @author rubensworks
 *
 */
public class BlockMenrilTorchWallConfig extends BlockConfig {

    public BlockMenrilTorchWallConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_torch_wall",
                eConfig -> {
                    return new WallTorchBlock(ParticleTypes.FLAME, Block.Properties.of()
                            .noCollission()
                            .strength(0)
                            .lightLevel((blockState) -> 14)
                            .sound(SoundType.WOOD)
                            .lootFrom(RegistryEntries.BLOCK_MENRIL_TORCH::get)) {
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
    protected Collection<ItemStack> defaultCreativeTabEntries() {
        return Collections.emptyList();
    }
}
