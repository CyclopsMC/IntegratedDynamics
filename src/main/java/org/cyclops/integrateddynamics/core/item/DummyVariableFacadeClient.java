package org.cyclops.integrateddynamics.core.item;

import net.minecraft.client.renderer.item.ItemModel;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeClient;

import javax.annotation.Nullable;

/**
 * @author rubensworks
 */
public class DummyVariableFacadeClient implements IVariableFacadeClient {

    @Override
    @Nullable
    public ItemModel getItemModelOverlay(IVariableModelBaked variableModelBaked) {
        return null;
    }

}
