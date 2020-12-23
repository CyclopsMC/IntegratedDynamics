package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.Maps;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * Variable model provider for value types.
 * @author rubensworks
 */
public class ValueTypeVariableModelProvider implements IVariableModelProvider<BakedMapVariableModelProvider<IValueType>> {
    @Override
    public BakedMapVariableModelProvider<IValueType> bakeOverlayModels(ModelBakery modelBakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform transform, ResourceLocation location) {
        Map<IValueType, IBakedModel> bakedModels = Maps.newHashMap();
        for(IValueType valueType : ValueTypes.REGISTRY.getValueTypes()) {
            try {
                ResourceLocation resourceLocation = ValueTypes.REGISTRY.getValueTypeModel(valueType);
                if(resourceLocation != null) {
                    IBakedModel bakedModel = modelBakery.getBakedModel(resourceLocation, transform, spriteGetter);
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
    public void loadModels(ModelLoader modelLoader) {
        for(IValueType valueType : ValueTypes.REGISTRY.getValueTypes()) {
            modelLoader.getSpecialModels().add(ValueTypes.REGISTRY.getValueTypeModel(valueType));
        }
    }

}
