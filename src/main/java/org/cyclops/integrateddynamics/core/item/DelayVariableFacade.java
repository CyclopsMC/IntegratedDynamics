package org.cyclops.integrateddynamics.core.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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

    protected ITextComponent getProxyNotInNetworkError() {
        return new TranslationTextComponent(L10NValues.DELAY_ERROR_DELAYNOTINNETWORK, Integer.toString(getProxyId()));
    }

    protected ITextComponent getProxyInvalidError() {
        return new TranslationTextComponent(L10NValues.DELAY_ERROR_DELAYINVALID, Integer.toString(getProxyId()));
    }

    protected ITextComponent getProxyInvalidTypeError(IPartNetwork network,
                                                                     IValueType containingValueType,
                                                                     IValueType actualType) {
        return new TranslationTextComponent(L10NValues.DELAY_ERROR_DELAYINVALIDTYPE,
                new TranslationTextComponent(containingValueType.getTranslationKey()),
                new TranslationTextComponent(actualType.getTranslationKey()));
    }

    protected ITextComponent getProxyTooltip() {
        return new TranslationTextComponent(L10NValues.DELAY_TOOLTIP_DELAYID, getProxyId());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addModelOverlay(IVariableModelBaked variableModelBaked, List<BakedQuad> quads, Random random, IModelData modelData) {
        if(isValid()) {
            quads.addAll(variableModelBaked.getSubModels(VariableModelProviders.DELAY).getBakedModel().getQuads(null, null, random, modelData));
        }
    }
}
