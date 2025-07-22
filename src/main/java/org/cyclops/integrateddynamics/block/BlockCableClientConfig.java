package org.cyclops.integrateddynamics.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.client.model.IDynamicModelElementCommon;
import org.cyclops.cyclopscore.config.extendedconfig.BlockClientConfig;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.client.model.CableModel;
import org.cyclops.integrateddynamics.core.client.model.ItemModelCable;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

public class BlockCableClientConfig extends BlockClientConfig<IntegratedDynamics> {

    public static TextureAtlasSprite BLOCK_TEXTURE;

    public BlockCableClientConfig(BlockConfigCommon<IntegratedDynamics> blockConfig) {
        super(blockConfig);
        blockConfig.getMod().getModEventBus().addListener(this::onRegisterColors);
        blockConfig.getMod().getModEventBus().addListener(this::registerClientExtensions);
        blockConfig.getMod().getModEventBus().addListener(this::postTextureStitch);
        blockConfig.getMod().getModEventBus().addListener((RegisterItemModelsEvent event) -> event.register(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "cable"), ItemModelCable.Unbaked.MAP_CODEC));
    }

    public void onRegisterColors(RegisterColorHandlersEvent.Block event) {
        event.register(new BlockCableClientConfig.BlockColor(), getBlockConfig().getInstance());
    }

    public void postTextureStitch(TextureAtlasStitchedEvent event) {
        if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            BLOCK_TEXTURE = event.getAtlas().getSprite(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/cable"));
        }
    }

    public void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerBlock(new IClientBlockExtensions() {
            @Override
            public boolean addHitEffects(BlockState blockState, Level world, HitResult target, ParticleEngine particleManager) {
                BlockPos blockPos = ((BlockHitResult) target).getBlockPos();
                if(CableHelpers.hasFacade(world, blockPos)) {
                    CableHelpers.getFacade(world, blockPos)
                            .ifPresent(facadeState -> IModHelpers.get().getRenderHelpers().addBlockHitEffects(particleManager, (ClientLevel) world, facadeState, blockPos, ((BlockHitResult) target).getDirection()));
                    return true;
                } else {
                    return false;
                }
            }
        }, getBlockConfig().getInstance());
    }

    @Override
    @Nullable
    public IDynamicModelElementCommon getDynamicModelElement() {
        return new DynamicModel();
    }

    public static class BlockColor implements net.minecraft.client.color.block.BlockColor {
        @Override
        public int getColor(BlockState blockState, @Nullable BlockAndTintGetter world, @Nullable BlockPos blockPos, int color) {
            // Only modify color if we have a facade
            return blockPos == null || (!(world instanceof ILevelExtension levelExtension)) ?
                    -1 : CableHelpers.getFacade(levelExtension, blockPos)
                    .map(facadeState -> Minecraft.getInstance().getBlockColors().getColor(facadeState, world, blockPos, color))
                    .orElse(-1);
        }
    }

    public static class DynamicModel implements IDynamicModelElementCommon {
        @Override
        public BlockStateModel createDynamicBlockModel(Consumer<Pair<BlockState, BlockStateModel>> modelConsumer, Function<BlockState, BlockStateModel> modelRetriever) {
            CableModel model = new CableModel();
            modelConsumer.accept(Pair.of(RegistryEntries.BLOCK_CABLE.get().defaultBlockState(), model));
            modelConsumer.accept(Pair.of(RegistryEntries.BLOCK_CABLE.get().defaultBlockState().setValue(BlockCable.WATERLOGGED, true), model));
            return model;
        }

        @Override
        public ItemModel createDynamicItemModel(Consumer<Pair<ResourceLocation, ItemModel>> modelConsumer, Function<ResourceLocation, ItemModel> modelRetriever) {
            ItemModelCable model = new ItemModelCable(new CableModel());
            ResourceLocation registryName = BuiltInRegistries.BLOCK.getKey(RegistryEntries.BLOCK_CABLE.get());
            modelConsumer.accept(Pair.of(registryName, model));
            return model;
        }
    }
}
