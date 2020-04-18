package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.Maps;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * Variable model provider for aspects.
 * @author rubensworks
 */
public class AspectVariableModelProvider implements IVariableModelProvider<BakedMapVariableModelProvider<IAspect>> {
    @Override
    public BakedMapVariableModelProvider<IAspect> bakeOverlayModels(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> spriteGetter,
                                                                    IModelTransform transform, ResourceLocation location) {
        Map<IAspect, IBakedModel> bakedModels = Maps.newHashMap();
        for(IAspect aspect : Aspects.REGISTRY.getAspects()) {
            ResourceLocation resourceLocation = Aspects.REGISTRY.getAspectModel(aspect);
            IBakedModel bakedModel = modelBakery.getBakedModel(resourceLocation, transform, spriteGetter);
            bakedModels.put(aspect, bakedModel);
        }
        return new BakedMapVariableModelProvider<>(bakedModels);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Aspects.REGISTRY.getAspectModels();
    }

    @Override
    public void loadModels(ModelLoader modelLoader) {
        for(IAspect aspect : Aspects.REGISTRY.getAspects()) {
            modelLoader.getSpecialModels().add(Aspects.REGISTRY.getAspectModel(aspect));
        }
    }

}
