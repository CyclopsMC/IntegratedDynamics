package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import org.cyclops.cyclopscore.client.model.DelegatingChildDynamicItemAndBlockModel;
import org.cyclops.cyclopscore.helper.ModelHelpers;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.item.ItemVariable;

import java.util.List;
import java.util.Map;

/**
 * A baked variable model.
 * @author rubensworks
 */
public class VariableModelBaked extends DelegatingChildDynamicItemAndBlockModel implements IVariableModelBaked {

    private final Map<IVariableModelProvider, IVariableModelProvider.IBakedModelProvider> subModels = Maps.newHashMap();

    public VariableModelBaked(IBakedModel parent) {
        super(parent);
    }

    @Override
    public <B extends IVariableModelProvider.IBakedModelProvider> void setSubModels(IVariableModelProvider<B> provider, B subModels) {
        this.subModels.put(provider, subModels);
    }

    @Override
    public <B extends IVariableModelProvider.IBakedModelProvider> B getSubModels(IVariableModelProvider<B> provider) {
        return (B) this.subModels.get(provider);
    }

    @Override
    public IBakedModel handleBlockState(IBlockState state, EnumFacing side, long rand) {
        return null;
    }

    @Override
    public IBakedModel handleItemState(ItemStack itemStack, World world, EntityLivingBase entity) {
        List<BakedQuad> quads = Lists.newLinkedList();
        // Add regular quads for variable
        quads.addAll(this.baseModel.getQuads(null, getRenderingSide(), 0L));

        // Add variable type overlay
        IVariableFacade variableFacade = ItemVariable.getInstance().getVariableFacade(itemStack);
        variableFacade.addModelOverlay(this, quads);

        return new PerspectiveMapWrapper(new SimpleBakedModel(quads, ModelHelpers.EMPTY_FACE_QUADS, this.isAmbientOcclusion(), this.isGui3d(),
                this.getParticleTexture(), this.getItemCameraTransforms(), this.getOverrides()), ModelHelpers.DEFAULT_PERSPECTIVE_TRANSFORMS_ITEM);
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.baseModel.getParticleTexture();
    }
}
