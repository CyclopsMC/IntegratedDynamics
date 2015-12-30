package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Variable model provider for aspects.
 * @author rubensworks
 */
public class AspectVariableModelProvider implements IVariableModelProvider<BakedMapVariableModelProvider<IAspect>> {
    @Override
    public BakedMapVariableModelProvider<IAspect> bakeOverlayModels(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        Map<IAspect, IBakedModel> bakedModels = Maps.newHashMap();
        for(IAspect aspect : Aspects.REGISTRY.getAspects()) {
            try {
                IModel model = ModelLoaderRegistry.getModel(Aspects.REGISTRY.getAspectModel(aspect));
                IBakedModel bakedAspectModel = model.bake(state, format, bakedTextureGetter);
                bakedModels.put(aspect, bakedAspectModel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new BakedMapVariableModelProvider<>(bakedModels);
    }

    @Override
    public Collection<ModelResourceLocation> getDependencies() {
        return Aspects.REGISTRY.getAspectModels();
    }

}
