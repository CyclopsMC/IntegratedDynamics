package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
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
    public BakedSingleVariableModelProvider bakeOverlayModels(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> spriteGetter,
                                                              IModelTransform transform, ResourceLocation location) {
        IBakedModel bakedModel = null;
        try {
            bakedModel = modelBakery.getBakedModel(this.model, transform, spriteGetter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BakedSingleVariableModelProvider(bakedModel);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableList.of(model);
    }

    @Override
    public void loadModels(ModelLoader modelLoader) {
        modelLoader.getSpecialModels().add(model);
    }

}
