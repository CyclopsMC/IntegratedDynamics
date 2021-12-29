package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.Maps;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.ForgeModelBakery;
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
                                                                    ModelState transform, ResourceLocation location) {
        Map<IAspect, BakedModel> bakedModels = Maps.newHashMap();
        for(IAspect aspect : Aspects.REGISTRY.getAspects()) {
            ResourceLocation resourceLocation = Aspects.REGISTRY.getAspectModel(aspect);
            BakedModel bakedModel = modelBakery.bake(resourceLocation, transform, spriteGetter);
            bakedModels.put(aspect, bakedModel);
        }
        return new BakedMapVariableModelProvider<>(bakedModels);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Aspects.REGISTRY.getAspectModels();
    }

    @Override
    public void loadModels(ForgeModelBakery modelLoader) {
        for(IAspect aspect : Aspects.REGISTRY.getAspects()) {
            modelLoader.getSpecialModels().add(Aspects.REGISTRY.getAspectModel(aspect));
        }
    }

}
