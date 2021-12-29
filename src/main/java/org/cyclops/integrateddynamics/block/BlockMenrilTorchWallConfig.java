package org.cyclops.integrateddynamics.block;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

import java.util.Random;
import java.util.function.Supplier;

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
                    WallTorchBlock block = new WallTorchBlock(Block.Properties.of(Material.DECORATION)
                            .noCollission()
                            .strength(0)
                            .lightLevel((blockState) -> 14)
                            .sound(SoundType.WOOD), ParticleTypes.FLAME) {
                        @Override
                        @OnlyIn(Dist.CLIENT)
                        public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
                            // No particles
                        }
                    };
                    ObfuscationReflectionHelper.setPrivateValue(BlockBehaviour.class, block,
                            (Supplier<ResourceLocation>) () -> RegistryEntries.BLOCK_MENRIL_TORCH.getLootTable(), "lootTableSupplier");
                    return block;
                },
                null
        );
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(getInstance(), RenderType.cutout());
    }

}
