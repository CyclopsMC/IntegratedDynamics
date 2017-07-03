package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;

import java.util.Collection;
import java.util.function.Function;

/**
 * Variable model provider for a single model.
 * @author rubensworks
 */
public class SingleVariableModelProvider implements IVariableModelProvider<BakedSingleVariableModelProvider> {

    private final ResourceLocation model;

    public SingleVariableModelProvider(ResourceLocation model) {
        this.model = model;
    }

    @Override
    public BakedSingleVariableModelProvider bakeOverlayModels(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        IBakedModel bakedModel = null;
        try {
            IModel model = ModelLoaderRegistry.getModel(this.model);
            bakedModel = model.bake(state, format, bakedTextureGetter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BakedSingleVariableModelProvider(bakedModel);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableList.of(model);
    }

}
