package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartItemModel;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.item.ItemVariable;

import java.util.List;
import java.util.Map;

/**
 * A baked variable model.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class VariableModelBaked extends IFlexibleBakedModel.Wrapper implements ISmartItemModel, IVariableModelBaked {

    private final IBakedModel parent;
    private final Map<IValueType, IBakedModel> valueTypeSubModels = Maps.newHashMap();
    private final Map<IAspect, IBakedModel> aspectSubModels = Maps.newHashMap();

    public VariableModelBaked(IBakedModel parent) {
        super(parent, Attributes.DEFAULT_BAKED_FORMAT);
        this.parent = parent;
    }

    @Override
    public void addValueTypeModel(IValueType valueType, IBakedModel bakedModel) {
        valueTypeSubModels.put(valueType, bakedModel);
    }

    @Override
    public void addAspectModel(IAspect aspect, IBakedModel bakedModel) {
        aspectSubModels.put(aspect, bakedModel);
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

}
