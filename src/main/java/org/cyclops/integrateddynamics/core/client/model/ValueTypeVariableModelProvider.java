package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Variable model provider for value types.
 * @author rubensworks
 */
public class ValueTypeVariableModelProvider implements IVariableModelProvider<BakedMapVariableModelProvider<IValueType>> {
    @Override
    public BakedMapVariableModelProvider<IValueType> bakeOverlayModels(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        Map<IValueType, IBakedModel> bakedModels = Maps.newHashMap();
        for(IValueType valueType : ValueTypes.REGISTRY.getValueTypes()) {
            try {
                ResourceLocation resourceLocation = ValueTypes.REGISTRY.getValueTypeModel(valueType);
                if(resourceLocation != null) {
                    IModel model = ModelLoaderRegistry.getModel(resourceLocation);
                    IBakedModel bakedValueTypeModel = model.bake(state, format, bakedTextureGetter);
                    bakedModels.put(valueType, bakedValueTypeModel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new BakedMapVariableModelProvider<>(bakedModels);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ValueTypes.REGISTRY.getValueTypeModels();
    }

}
