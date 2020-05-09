package org.cyclops.integrateddynamics.core.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IDelayVariableFacade;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.List;

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

    protected L10NHelpers.UnlocalizedString getProxyNotInNetworkError() {
        return new L10NHelpers.UnlocalizedString(L10NValues.DELAY_ERROR_DELAYNOTINNETWORK, Integer.toString(getProxyId()));
    }

    protected L10NHelpers.UnlocalizedString getProxyInvalidError() {
        return new L10NHelpers.UnlocalizedString(L10NValues.DELAY_ERROR_DELAYINVALID, Integer.toString(getProxyId()));
    }

    protected L10NHelpers.UnlocalizedString getProxyInvalidTypeError(IPartNetwork network,
                                                                     IValueType containingValueType,
                                                                     IValueType actualType) {
        return new L10NHelpers.UnlocalizedString(L10NValues.DELAY_ERROR_DELAYINVALIDTYPE,
                new L10NHelpers.UnlocalizedString(containingValueType.getTranslationKey()),
                new L10NHelpers.UnlocalizedString(actualType.getTranslationKey()));
    }

    protected String getProxyTooltip() {
        return L10NHelpers.localize(L10NValues.DELAY_TOOLTIP_DELAYID, getProxyId());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addModelOverlay(IVariableModelBaked variableModelBaked, List<BakedQuad> quads) {
        if(isValid()) {
            quads.addAll(variableModelBaked.getSubModels(VariableModelProviders.DELAY).getBakedModel().getQuads(null, null, 0L));
        }
    }
}
