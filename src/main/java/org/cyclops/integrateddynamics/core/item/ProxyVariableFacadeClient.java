package org.cyclops.integrateddynamics.core.item;

import net.minecraft.client.renderer.item.ItemModel;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeClient;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;

/**
 * @author rubensworks
 */
public class ProxyVariableFacadeClient implements IVariableFacadeClient {

    private final ProxyVariableFacade variableFacade;

    public ProxyVariableFacadeClient(ProxyVariableFacade variableFacade) {
        this.variableFacade = variableFacade;
    }

    @Override
    public ItemModel getItemModelOverlay(IVariableModelBaked variableModelBaked) {
        if(this.variableFacade.isValid()) {
            return variableModelBaked.getSubModels(VariableModelProviders.PROXY).getBakedModel();
        }
        return null;
    }

}
