package org.cyclops.integrateddynamics.block;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;


/**
 * Config for the Menril Torch.
 * @author rubensworks
 *
 */
public class BlockMenrilTorchConfig extends BlockConfig {

    public BlockMenrilTorchConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_torch",
                eConfig -> new TorchBlock(Block.Properties.of(Material.DECORATION)
                        .noCollission()
                        .strength(0)
                        .lightLevel((blockState) -> 14)
                        .sound(SoundType.WOOD), ParticleTypes.FLAME) {
                    @Override
                    @OnlyIn(Dist.CLIENT)
                    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
                        // No particles
                    }
                },
                (eConfig, block) -> new StandingAndWallBlockItem(block,
                        RegistryEntries.BLOCK_MENRIL_TORCH_WALL,
                        new Item.Properties().tab(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(getInstance(), RenderType.cutout());
    }

}
