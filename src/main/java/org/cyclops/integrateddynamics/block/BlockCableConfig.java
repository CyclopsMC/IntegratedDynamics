package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.item.ItemBlockCable;

/**
 * Config for {@link BlockCable}.
 * @author rubensworks
 */
public class BlockCableConfig extends BlockConfig {

    public BlockCableConfig() {
        super(
                IntegratedDynamics._instance,
                "cable",
                eConfig -> new BlockCable(Block.Properties.create(BlockCable.BLOCK_MATERIAL)
                        .hardnessAndResistance(BlockCable.BLOCK_HARDNESS)
                        .sound(SoundType.METAL)
                        .setOpaque((blockState, world, pos) -> false)),
                (eConfig, block) -> new ItemBlockCable(block, new Item.Properties()
                        .group(IntegratedDynamics._instance.getDefaultItemGroup()))
                );
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onModLoaded);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        // Render the cable in all layers, and handle layer checking inside the model
        RenderTypeLookup.setRenderLayer(getInstance(), (type) -> true);
    }

    @OnlyIn(Dist.CLIENT)
    public void onModLoaded(FMLLoadCompleteEvent event) {
        Minecraft.getInstance().getBlockColors().register(new BlockCable.BlockColor(), getInstance());
    }

}
