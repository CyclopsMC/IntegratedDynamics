package org.cyclops.integrateddynamics.core.item;

import net.minecraft.client.renderer.item.ItemModel;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeClient;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;

import javax.annotation.Nullable;

/**
 * @author rubensworks
 */
public class DelayVariableFacadeClient implements IVariableFacadeClient {

    private final DelayVariableFacade variableFacade;

    public DelayVariableFacadeClient(DelayVariableFacade variableFacade) {
        this.variableFacade = variableFacade;
    }

    @Nullable
    @Override
    public ItemModel getItemModelOverlay(IVariableModelBaked variableModelBaked) {
        if(this.variableFacade.isValid()) {
            return variableModelBaked.getSubModels(VariableModelProviders.DELAY).getBakedModel();
        }
        return null;
    }

}
