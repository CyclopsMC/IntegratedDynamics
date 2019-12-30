package org.cyclops.integrateddynamics.api.client.model;

import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.function.Function;

/**
 * A provider of variable overlay models.
 * @param <B> The type of baked model provider.
 * @author rubensworks
 */
public interface IVariableModelProvider<B extends IVariableModelProvider.IBakedModelProvider> {

    /**
     * Load the models for this provider.
     * @param modelBakery The model state.
     * @param spriteGetter The texture retriever.
     * @param sprite The sprite.
     * @param format The vertex format.
     * @return The baked moderl provider.
     */
    public B bakeOverlayModels(ModelBakery modelBakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter,
                               ISprite sprite, VertexFormat format);

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
