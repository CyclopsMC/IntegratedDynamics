package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.Maps;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ResolvableModel;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.part.aspect.Aspects;
import org.joml.Matrix4fc;

import java.util.Map;

/**
 * Variable facadeModel provider for aspects.
 * @author rubensworks
 */
public class AspectVariableModelProvider implements IVariableModelProvider<BakedMapVariableModelProvider<IAspect>> {
    @Override
    public BakedMapVariableModelProvider<IAspect> bakeOverlayModels(ItemModel.BakingContext bakingContext, Matrix4fc matrix) {
        Map<IAspect, ItemModel> bakedModels = Maps.newHashMap();
        for(IAspect aspect : Aspects.REGISTRY.getAspects()) {
            ItemModel.Unbaked unbakedModel = Aspects.REGISTRY.getClient().getAspectModel(aspect);
            ItemModel bakedModel = unbakedModel.bake(bakingContext, matrix);
            bakedModels.put(aspect, bakedModel);
        }
        return new BakedMapVariableModelProvider<>(bakedModels);
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        for(IAspect aspect : Aspects.REGISTRY.getAspects()) {
            Aspects.REGISTRY.getClient().getAspectModel(aspect).resolveDependencies(resolver);
        }
    }

}
