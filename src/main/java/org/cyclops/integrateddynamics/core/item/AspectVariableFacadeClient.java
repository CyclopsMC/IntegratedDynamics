package org.cyclops.integrateddynamics.core.item;

import net.minecraft.client.renderer.item.CompositeModel;
import net.minecraft.client.renderer.item.ItemModel;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeClient;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;

import java.util.List;

/**
 * @author rubensworks
 */
public class AspectVariableFacadeClient implements IVariableFacadeClient {

    private final AspectVariableFacade variableFacade;

    public AspectVariableFacadeClient(AspectVariableFacade variableFacade) {
        this.variableFacade = variableFacade;
    }

    @Override
    public ItemModel getItemModelOverlay(IVariableModelBaked variableModelBaked) {
        if(this.variableFacade.isValid()) {
            IAspect aspect = this.variableFacade.getAspect();
            IValueType valueType = aspect.getValueType();
            return new CompositeModel(List.of(
                    variableModelBaked.getSubModels(VariableModelProviders.VALUETYPE).getBakedModels().get(valueType),
                    variableModelBaked.getSubModels(VariableModelProviders.ASPECT).getBakedModels().get(aspect)
            ));
        }
        return null;
    }

}
