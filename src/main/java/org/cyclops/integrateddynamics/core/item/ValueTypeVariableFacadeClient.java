package org.cyclops.integrateddynamics.core.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeClient;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;

/**
 * @author rubensworks
 */
public class ValueTypeVariableFacadeClient<V extends IValue> implements IVariableFacadeClient {

    private final ValueTypeVariableFacade<V> variableFacade;

    public ValueTypeVariableFacadeClient(ValueTypeVariableFacade<V> variableFacade) {
        this.variableFacade = variableFacade;
    }

    @Override
    public ItemModel getItemModelOverlay(IVariableModelBaked variableModelBaked) {
        if(this.variableFacade.isValid()) {
            return variableModelBaked.getSubModels(VariableModelProviders.VALUETYPE).getBakedModels().get(this.variableFacade.getValueType());
        }
        return null;
    }

    @Override
    public void renderISTER(ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if(this.variableFacade.isValid()) {
            this.variableFacade.getValueType().getClient().renderISTER(this.variableFacade.getValue(), stack, transformType, matrixStack, buffer, combinedLight, combinedOverlay);
        }
    }
}
