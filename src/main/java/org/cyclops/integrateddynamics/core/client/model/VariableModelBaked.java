package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import org.cyclops.cyclopscore.client.model.DelegatingChildDynamicItemAndBlockModel;
import org.cyclops.cyclopscore.helper.ModelHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * A baked variable model.
 * @author rubensworks
 */
public class VariableModelBaked extends DelegatingChildDynamicItemAndBlockModel implements IVariableModelBaked {

    private final Map<IVariableModelProvider, IVariableModelProvider.BakedModelProvider> subModels = Maps.newHashMap();

    public VariableModelBaked(BakedModel parent) {
        super(parent);
    }

    @Override
    public <B extends IVariableModelProvider.BakedModelProvider> void setSubModels(IVariableModelProvider<B> provider, B subModels) {
        this.subModels.put(provider, subModels);
    }

    @Override
    public <B extends IVariableModelProvider.BakedModelProvider> B getSubModels(IVariableModelProvider<B> provider) {
        return (B) this.subModels.get(provider);
    }

    @Override
    public BakedModel handleBlockState(BlockState state, Direction side, RandomSource rand, IModelData modelData) {
        return null;
    }

    @Override
    public BakedModel handleItemState(ItemStack itemStack, Level world, LivingEntity entity) {
        List<BakedQuad> quads = Lists.newLinkedList();
        // Add regular quads for variable
        quads.addAll(this.baseModel.getQuads(null, getRenderingSide(), this.rand, this.modelData));

        // Add variable type overlay
        IVariableFacade variableFacade = RegistryEntries.ITEM_VARIABLE.getVariableFacade(itemStack);
        variableFacade.addModelOverlay(this, quads, this.rand, this.modelData);

        return new SimpleBakedModel(quads, ModelHelpers.EMPTY_FACE_QUADS, this.useAmbientOcclusion(), this.usesBlockLight(), this.isGui3d(),
                this.getParticleIcon(), this.getTransforms(), this.getOverrides());
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.baseModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return ModelHelpers.DEFAULT_CAMERA_TRANSFORMS_ITEM;
    }

    @Override
    public ItemOverrides getOverrides() {
        return new ItemOverrides() {
            @Nullable
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity livingEntity, int id) {
                IVariableFacade variableFacade = RegistryEntries.ITEM_VARIABLE.getVariableFacade(stack);
                BakedModel overrideModel = variableFacade.getVariableItemOverrideModel(model, stack, world, livingEntity);
                if (overrideModel != null) {
                    return overrideModel;
                }
                return VariableModelBaked.super.getOverrides().resolve(model, stack, world, livingEntity, id);
            }

            @Override
            public ImmutableList<BakedOverride> getOverrides() {
                return baseModel.getOverrides().getOverrides();
            }
        };
    }
}
