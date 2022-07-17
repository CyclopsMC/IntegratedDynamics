package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;

import java.util.Collection;
import java.util.List;
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
                                                              ModelState transform, ResourceLocation location) {
        BakedModel bakedModel = null;
        try {
            bakedModel = modelBakery.bake(this.model, transform, spriteGetter);
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
    public void loadModels(List<ResourceLocation> subModels) {
        subModels.add(model);
    }

}
