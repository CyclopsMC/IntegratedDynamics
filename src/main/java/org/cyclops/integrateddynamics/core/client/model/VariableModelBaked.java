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
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.List;
import java.util.Map;

/**
 * A baked variable model.
 * @author rubensworks
 */
public class VariableModelBaked extends IFlexibleBakedModel.Wrapper implements ISmartItemModel {

    private final IBakedModel parent;
    private final Map<IValueType, IBakedModel> valueTypeSubModels = Maps.newHashMap();
    private final Map<IAspect, IBakedModel> aspectSubModels = Maps.newHashMap();

    public VariableModelBaked(IBakedModel parent) {
        super(parent, Attributes.DEFAULT_BAKED_FORMAT);
        this.parent = parent;
    }

    public void addValueTypeModel(IValueType valueType, IBakedModel bakedModel) {
        valueTypeSubModels.put(valueType, bakedModel);
    }

    public void addAspectModel(IAspect aspect, IBakedModel bakedModel) {
        aspectSubModels.put(aspect, bakedModel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IBakedModel handleItemState(ItemStack itemStack) {
        List<BakedQuad> quads = Lists.newLinkedList();
        // Add regular quads for variable
        quads.addAll(parent.getGeneralQuads());

        // Optionally add variable type overlay
        Pair<Integer, IAspect> aspectInfo = Aspects.REGISTRY.readAspect(itemStack);
        if(aspectInfo != null) {
            IAspect aspect = aspectInfo.getRight();
            IValueType valueType = aspect.getValueType();
            quads.addAll(valueTypeSubModels.get(valueType).getGeneralQuads());
            quads.addAll(aspectSubModels.get(aspect).getGeneralQuads());
        }

        return new SimpleBakedModel(quads, ModelHelpers.EMPTY_FACE_QUADS, this.isAmbientOcclusion(), this.isGui3d(),
                this.getTexture(), this.getItemCameraTransforms());
    }

}
