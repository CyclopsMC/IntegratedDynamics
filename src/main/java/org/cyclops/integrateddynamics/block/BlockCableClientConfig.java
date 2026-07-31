package org.cyclops.integrateddynamics.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.client.model.standalone.UnbakedStandaloneModel;
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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class BlockCableClientConfig extends BlockClientConfig<IntegratedDynamics> {

    public static TextureAtlasSprite BLOCK_TEXTURE;

    public BlockCableClientConfig(BlockConfigCommon<IntegratedDynamics> blockConfig) {
        super(blockConfig);
        blockConfig.getMod().getModEventBus().addListener(this::onRegisterColors);
        blockConfig.getMod().getModEventBus().addListener(this::registerClientExtensions);
        blockConfig.getMod().getModEventBus().addListener(this::postTextureStitch);
        blockConfig.getMod().getModEventBus().addListener((RegisterItemModelsEvent event) -> event.register(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "cable"), ItemModelCable.Unbaked.MAP_CODEC));
        blockConfig.getMod().getModEventBus().addListener((ModelEvent.RegisterStandalone event) -> event.register(new StandaloneModelKey<>(() -> Reference.MOD_ID + ":CableModelHack"), new StandaloneCableModel()));
    }

    public void onRegisterColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(new BlockCableClientConfig.BlockColor()), getBlockConfig().getInstance());
    }

    public void postTextureStitch(TextureAtlasStitchedEvent event) {
        if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            BLOCK_TEXTURE = event.getAtlas().getSprite(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "block/cable"));
        }
    }

    public void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerBlock(new IClientBlockExtensions() {
            @Override
            public boolean addHitEffects(BlockState blockState, Level world, HitResult target, ParticleEngine particleManager) {
                BlockPos blockPos = ((BlockHitResult) target).getBlockPos();
                if(CableHelpers.hasFacade(world, blockPos, blockState)) {
                    CableHelpers.getFacade(world, blockPos, blockState)
                            .ifPresent(facadeState -> IModHelpers.get().getRenderHelpers().addBlockHitEffects(particleManager, (ClientLevel) world, facadeState, blockPos, ((BlockHitResult) target).getDirection()));
                    return true;
                } else {
                    return false;
                }
            }
        }, getBlockConfig().getInstance());
    }

    @Override
    public IDynamicModelElementCommon getDynamicModelElement() {
        return new DynamicModel();
    }

    public static class BlockColor implements BlockTintSource {
        @Override
        public int color(BlockState blockState) {
            return -1;
        }

        @Override
        public int colorInWorld(BlockState blockState, BlockAndTintGetter world, BlockPos blockPos) {
            return CableHelpers.getFacadeMultipartTicking(world, blockPos)
                    .map(facadeState -> {
                        BlockTintSource tintSource = Minecraft.getInstance().getBlockColors().getTintSource(facadeState, 0);
                        if (tintSource != null) {
                            return tintSource.colorInWorld(facadeState, world, blockPos);
                        }
                        return -1;
                    })
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
        public ItemModel createDynamicItemModel(Consumer<Pair<Identifier, ItemModel>> modelConsumer, Function<Identifier, ItemModel> modelRetriever) {
            ItemModelCable model = new ItemModelCable(new CableModel());
            Identifier registryName = BuiltInRegistries.BLOCK.getKey(RegistryEntries.BLOCK_CABLE.get());
            modelConsumer.accept(Pair.of(registryName, model));
            return model;
        }
    }

    // This class is only necessary to gain access to the ModelBaker,
    // since it's needed at CableModel.MODEL_BAKER.
    // Yes, this is a hack, and may be possible to do in a cleaner way, but would probably require a CableModel rewrite.
    public static class StandaloneCableModel implements UnbakedStandaloneModel<Object> {
        @Override
        public void resolveDependencies(Resolver resolver) {
            // Do nothing
        }

        @Override
        public Object bake(ModelBaker baker, ModelDebugName name) {
            CableModel.MODEL_BAKER = baker;
            return null;
        }
    }
}
