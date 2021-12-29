package org.cyclops.integrateddynamics.core.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IDelayVariableFacade;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.List;
import java.util.Random;

/**
 * Variable facade for variables determined by delays.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DelayVariableFacade extends ProxyVariableFacade implements IDelayVariableFacade {

    public DelayVariableFacade(boolean generateId, int proxyId) {
        super(generateId, proxyId);
    }

    public DelayVariableFacade(int id, int proxyId) {
        super(id, proxyId);
    }

    @Override
    protected MutableComponent getProxyNotInNetworkError() {
        return new TranslatableComponent(L10NValues.DELAY_ERROR_DELAYNOTINNETWORK, Integer.toString(getProxyId()));
    }

    @Override
    protected MutableComponent getProxyInvalidError() {
        return new TranslatableComponent(L10NValues.DELAY_ERROR_DELAYINVALID, Integer.toString(getProxyId()));
    }

    @Override
    protected MutableComponent getProxyInvalidTypeError(IPartNetwork network,
                                                                     IValueType containingValueType,
                                                                     IValueType actualType) {
        return new TranslatableComponent(L10NValues.DELAY_ERROR_DELAYINVALIDTYPE,
                new TranslatableComponent(containingValueType.getTranslationKey()),
                new TranslatableComponent(actualType.getTranslationKey()));
    }

    protected Component getProxyTooltip() {
        return new TranslatableComponent(L10NValues.DELAY_TOOLTIP_DELAYID, getProxyId());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addModelOverlay(IVariableModelBaked variableModelBaked, List<BakedQuad> quads, Random random, IModelData modelData) {
        if(isValid()) {
            quads.addAll(variableModelBaked.getSubModels(VariableModelProviders.DELAY).getBakedModel().getQuads(null, null, random, modelData));
        }
    }
}
