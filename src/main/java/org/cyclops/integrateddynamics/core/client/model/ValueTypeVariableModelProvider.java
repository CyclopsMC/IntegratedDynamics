package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.Maps;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ResolvableModel;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.joml.Matrix4fc;

import java.util.Map;

/**
 * Variable facadeModel provider for value types.
 * @author rubensworks
 */
public class ValueTypeVariableModelProvider implements IVariableModelProvider<BakedMapVariableModelProvider<IValueType>> {
    @Override
    public BakedMapVariableModelProvider<IValueType> bakeOverlayModels(ItemModel.BakingContext bakingContext, Matrix4fc matrix) {
        Map<IValueType, ItemModel> bakedModels = Maps.newHashMap();
        for(IValueType valueType : ValueTypes.REGISTRY.getValueTypes()) {
            try {
                ItemModel.Unbaked unbakedModel = ValueTypes.REGISTRY.getClient().getValueTypeModel(valueType);
                if(unbakedModel != null) {
                    ItemModel bakedModel = unbakedModel.bake(bakingContext, matrix);
                    bakedModels.put(valueType, bakedModel);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new BakedMapVariableModelProvider<>(bakedModels);
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        for(IValueType valueType : ValueTypes.REGISTRY.getValueTypes()) {
            ValueTypes.REGISTRY.getClient().getValueTypeModel(valueType).resolveDependencies(resolver);
        }
    }

}
