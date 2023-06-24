package org.cyclops.integrateddynamics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

import java.util.function.Supplier;

/**
 * Config for the Menril Stone Torch (wall).
 * @author rubensworks
 *
 */
public class BlockMenrilTorchStoneWallConfig extends BlockConfig {

    public BlockMenrilTorchStoneWallConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_torch_stone_wall",
                eConfig -> {
                    WallTorchBlock block = new WallTorchBlock(Block.Properties.of()
                            .noCollission()
                            .strength(0)
                            .lightLevel((blockState) -> 14)
                            .sound(SoundType.STONE), ParticleTypes.FLAME) {
                        @Override
                        @OnlyIn(Dist.CLIENT)
                        public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
                            // No particles
                        }
                    };
                    ObfuscationReflectionHelper.setPrivateValue(BlockBehaviour.class, block,
                            (Supplier<ResourceLocation>) () -> RegistryEntries.BLOCK_MENRIL_TORCH_STONE.getLootTable(), "lootTableSupplier");
                    return block;
                },
                null
        );
    }

}
