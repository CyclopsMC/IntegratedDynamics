package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.util.ResourceLocation;
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
 * Config for the Menril Torch (wall).
 * @author rubensworks
 *
 */
public class BlockMenrilTorchWallConfig extends BlockConfig {

    public BlockMenrilTorchWallConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_torch_wall",
                eConfig -> new WallTorchBlock(Block.Properties.create(Material.MISCELLANEOUS)
                        .doesNotBlockMovement()
                        .hardnessAndResistance(0)
                        .lightValue(14)
                        .sound(SoundType.WOOD)) {
                    @Override
                    @OnlyIn(Dist.CLIENT)
                    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
                        // No particles
                    }

                    @Override
                    public ResourceLocation getLootTable() {
                        return RegistryEntries.BLOCK_MENRIL_TORCH.getLootTable();
                    }
                },
                null
        );
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        RenderTypeLookup.setRenderLayer(getInstance(), RenderType.getCutout());
    }

}
