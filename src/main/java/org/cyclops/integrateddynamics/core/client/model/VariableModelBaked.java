package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartItemModel;
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
public class VariableModelBaked extends IFlexibleBakedModel.Wrapper implements ISmartItemModel, IVariableModelBaked {

    private final IBakedModel parent;
    private final Map<IVariableModelProvider, IVariableModelProvider.IBakedModelProvider> subModels = Maps.newHashMap();

    public VariableModelBaked(IBakedModel parent) {
        super(parent, Attributes.DEFAULT_BAKED_FORMAT);
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IBakedModel handleItemState(ItemStack itemStack) {
        List<BakedQuad> quads = Lists.newLinkedList();
        // Add regular quads for variable
        quads.addAll(parent.getGeneralQuads());

        // Add variable type overlay
        IVariableFacade variableFacade = ItemVariable.getInstance().getVariableFacade(itemStack);
        variableFacade.addModelOverlay(this, quads);

        return new SimpleBakedModel(quads, ModelHelpers.EMPTY_FACE_QUADS, this.isAmbientOcclusion(), this.isGui3d(),
                this.getParticleTexture(), this.getItemCameraTransforms());
    }

    @Override
    public <B extends IVariableModelProvider.IBakedModelProvider> void setSubModels(IVariableModelProvider<B> provider, B subModels) {
        this.subModels.put(provider, subModels);
    }

    @Override
    public <B extends IVariableModelProvider.IBakedModelProvider> B getSubModels(IVariableModelProvider<B> provider) {
        return (B) this.subModels.get(provider);
    }
}
