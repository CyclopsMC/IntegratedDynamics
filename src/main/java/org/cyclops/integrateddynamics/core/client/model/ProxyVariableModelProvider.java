package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;

import java.util.Collection;

/**
 * Variable model provider for the proxy model.
 * @author rubensworks
 */
public class ProxyVariableModelProvider implements IVariableModelProvider<BakedSingleVariableModelProvider> {

    private ResourceLocation model = new ResourceLocation(Reference.MOD_ID + ":customoverlay/proxy");

    @Override
    public BakedSingleVariableModelProvider bakeOverlayModels(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        IBakedModel bakedModel = null;
        try {
            IModel model = ModelLoaderRegistry.getModel(this.model);
            bakedModel = model.bake(state, format, bakedTextureGetter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BakedSingleVariableModelProvider(bakedModel);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Lists.newArrayList(model);
    }

}
