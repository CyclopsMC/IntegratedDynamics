package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.Maps;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Variable model provider for value types.
 * @author rubensworks
 */
public class ValueTypeVariableModelProvider implements IVariableModelProvider<BakedMapVariableModelProvider<IValueType>> {
    @Override
    public BakedMapVariableModelProvider<IValueType> bakeOverlayModels(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ResourceLocation location) {
        Map<IValueType, BakedModel> bakedModels = Maps.newHashMap();
        for(IValueType valueType : ValueTypes.REGISTRY.getValueTypes()) {
            try {
                ResourceLocation resourceLocation = ValueTypes.REGISTRY.getValueTypeModel(valueType);
                if(resourceLocation != null) {
                    BakedModel bakedModel = modelBakery.bake(resourceLocation, transform, spriteGetter);
                    bakedModels.put(valueType, bakedModel);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new BakedMapVariableModelProvider<>(bakedModels);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ValueTypes.REGISTRY.getValueTypeModels();
    }

    @Override
    public void loadModels(List<ResourceLocation> subModels) {
        for(IValueType valueType : ValueTypes.REGISTRY.getValueTypes()) {
            subModels.add(ValueTypes.REGISTRY.getValueTypeModel(valueType));
        }
    }

}
