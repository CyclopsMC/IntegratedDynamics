package org.cyclops.integrateddynamics.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

import java.util.Random;
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
                    WallTorchBlock block = new WallTorchBlock(Block.Properties.create(Material.MISCELLANEOUS)
                            .doesNotBlockMovement()
                            .hardnessAndResistance(0)
                            .setLightLevel((blockState) -> 14)
                            .sound(SoundType.STONE), ParticleTypes.FLAME) {
                        @Override
                        @OnlyIn(Dist.CLIENT)
                        public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
                            // No particles
                        }
                    };
                    ObfuscationReflectionHelper.setPrivateValue(AbstractBlock.class, block,
                            (Supplier<ResourceLocation>) () -> RegistryEntries.BLOCK_MENRIL_TORCH_STONE.getLootTable(), "lootTableSupplier");
                    return block;
                },
                null
        );
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        RenderTypeLookup.setRenderLayer(getInstance(), RenderType.getCutout());
    }

}
