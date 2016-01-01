package org.cyclops.integrateddynamics.api.client.model;

import com.google.common.base.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelState;

import java.util.Collection;

/**
 * A provider of variable overlay models.
 * @param <B> The type of baked model provider.
 * @author rubensworks
 */
public interface IVariableModelProvider<B extends IVariableModelProvider.IBakedModelProvider> {

    /**
     * Load the models for this provider.
     * @param state The model state.
     * @param format The vertex format.
     * @param bakedTextureGetter The texture retriever.
     * @return The baked moderl provider.
     */
    public B bakeOverlayModels(IModelState state, VertexFormat format,
                          Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter);

    /**
     * @return All models this provider depends on and should thus be loaded.
     */
    public Collection<ResourceLocation> getDependencies();

    /**
     * Provider for baked models.
     */
    public static interface IBakedModelProvider {

    }

}
