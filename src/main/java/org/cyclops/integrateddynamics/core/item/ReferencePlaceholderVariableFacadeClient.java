package org.cyclops.integrateddynamics.core.item;

import net.minecraft.client.renderer.item.ItemModel;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeClient;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;

/**
 * @author rubensworks
 */
public class ReferencePlaceholderVariableFacadeClient implements IVariableFacadeClient {

    private final ReferencePlaceholderVariableFacade variableFacade;

    public ReferencePlaceholderVariableFacadeClient(ReferencePlaceholderVariableFacade variableFacade) {
        this.variableFacade = variableFacade;
    }

    @Override
    public ItemModel getItemModelOverlay(IVariableModelBaked variableModelBaked) {
        IValueType<?> valueType = this.variableFacade.getValueType();
        return valueType == null ? null : variableModelBaked.getSubModels(VariableModelProviders.VALUETYPE).getBakedModels().get(valueType);
    }
}
