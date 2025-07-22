package org.cyclops.integrateddynamics.core.item;

import net.minecraft.client.renderer.item.ItemModel;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeClient;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;

/**
 * @author rubensworks
 */
public class OperatorVariableFacadeClient implements IVariableFacadeClient {

    private final OperatorVariableFacade variableFacade;

    public OperatorVariableFacadeClient(OperatorVariableFacade variableFacade) {
        this.variableFacade = variableFacade;
    }

    @Override
    public ItemModel getItemModelOverlay(IVariableModelBaked variableModelBaked) {
        if(this.variableFacade.isValid()) {
            IValueType valueType = this.variableFacade.getOperator().getOutputType();
            return variableModelBaked.getSubModels(VariableModelProviders.VALUETYPE).getBakedModels().get(valueType);
        }
        return null;
    }
}
