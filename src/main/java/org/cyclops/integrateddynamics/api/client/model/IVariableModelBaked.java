package org.cyclops.integrateddynamics.api.client.model;

import net.minecraft.client.resources.model.IBakedModel;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartItemModel;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;

import java.util.Map;

/**
 * A model for variable items.
 * @author rubensworks
 */
public interface IVariableModelBaked extends IFlexibleBakedModel, ISmartItemModel {

    /**
     * Add a sub-model for a value type.
     * @param valueType The value type.
     * @param bakedModel The sub-model.
     */
    public void addValueTypeModel(IValueType valueType, IBakedModel bakedModel);

    /**
     * @return The registered value type sub-models.
     */
    public Map<IValueType, IBakedModel> getValueTypeSubModels();

    /**
     * Add a sub-model for an aspect.
     * @param aspect The aspect.
     * @param bakedModel The sub-model.
     */
    public void addAspectModel(IAspect aspect, IBakedModel bakedModel);

    /**
     * @return The registered aspect sub-models.
     */
    public Map<IAspect, IBakedModel> getAspectSubModels();

}
