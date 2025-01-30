package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.cyclops.cyclopscore.client.model.DynamicBaseModel;
import org.cyclops.cyclopscore.helper.ModelHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;

import java.util.List;
import java.util.Map;

/**
 * A baked variable model.
 * @author rubensworks
 */
public class BakedModelVariableOverlays extends DynamicBaseModel implements IVariableModelBaked {

    private final Map<IVariableModelProvider, IVariableModelProvider.BakedModelProvider> subModels = Maps.newHashMap();
    private final RandomSource rand = RandomSource.create();
    private final ModelData modelData = ModelData.EMPTY;

    @Override
    public <B extends IVariableModelProvider.BakedModelProvider> void setSubModels(IVariableModelProvider<B> provider, B subModels) {
        this.subModels.put(provider, subModels);
    }

    @Override
    public <B extends IVariableModelProvider.BakedModelProvider> B getSubModels(IVariableModelProvider<B> provider) {
        return (B) this.subModels.get(provider);
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getModelManager().getMissingModel().getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return ModelHelpers.DEFAULT_CAMERA_TRANSFORMS_ITEM;
    }

    public BakedModel getModelForItem(ItemStack itemStack, Level world, LivingEntity entity, ItemTransforms itemTransforms) {
        List<BakedQuad> quads = Lists.newLinkedList();

        // Add variable type overlay
        IVariableFacade variableFacade = RegistryEntries.ITEM_VARIABLE.get().getVariableFacade(ValueDeseralizationContext.of(world == null ? Minecraft.getInstance().level : world), itemStack);
        variableFacade.addModelOverlay(this, quads, this.rand, this.modelData);

        BakedModel overrideModel = variableFacade.getVariableItemOverrideModel(this, itemStack, (ClientLevel) world, entity);
        if (overrideModel != null) {
            return overrideModel;
        }

        return new SimpleBakedModel(quads, ModelHelpers.EMPTY_FACE_QUADS, this.useAmbientOcclusion(), this.usesBlockLight(), this.isGui3d(),
                this.getParticleIcon(), itemTransforms, RenderTypeGroup.EMPTY);
    }
}
