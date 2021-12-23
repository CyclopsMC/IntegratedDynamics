package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.Item;
import net.minecraft.item.WallOrFloorItem;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

import java.util.Random;

/**
 * Config for the Menril Stone Torch.
 * @author rubensworks
 *
 */
public class BlockMenrilTorchStoneConfig extends BlockConfig {

    public BlockMenrilTorchStoneConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_torch_stone",
                eConfig -> new TorchBlock(Block.Properties.of(Material.DECORATION)
                        .noCollission()
                        .strength(0)
                        .lightLevel((blockState) -> 14)
                        .sound(SoundType.STONE), ParticleTypes.FLAME) {
                    @Override
                    @OnlyIn(Dist.CLIENT)
                    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
                        // No particles
                    }
                },
                (eConfig, block) -> new WallOrFloorItem(block,
                        RegistryEntries.BLOCK_MENRIL_TORCH_STONE_WALL,
                        new Item.Properties().tab(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        RenderTypeLookup.setRenderLayer(getInstance(), RenderType.cutout());
    }

}
