package org.cyclops.integrateddynamics.api.client.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.ForgeModelBakery;

import java.util.Collection;
import java.util.function.Function;

/**
 * A provider of variable overlay models.
 * @param <B> The type of baked model provider.
 * @author rubensworks
 */
public interface IVariableModelProvider<B extends IVariableModelProvider.BakedModelProvider> {

    /**
     * Load the models for this provider.
     * @param modelBakery The model state.
     * @param spriteGetter The texture retriever.
     * @param transform The model transformation.
     * @param location The model location.
     * @return The baked moderl provider.
     */
    public B bakeOverlayModels(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> spriteGetter,
                               ModelState transform, ResourceLocation location);

    /**
     * @return All models this provider depends on and should thus be loaded.
     */
    public Collection<ResourceLocation> getDependencies();

    /**
     * Load all required models for this model provider into the given model loader.
     * @param modelLoader A model loader.
     */
    void loadModels(ForgeModelBakery modelLoader);

    /**
     * Provider for baked models.
     */
    public static interface BakedModelProvider {

    }

}
