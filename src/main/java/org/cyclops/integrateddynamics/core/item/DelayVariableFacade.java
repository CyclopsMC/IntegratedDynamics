package org.cyclops.integrateddynamics.core.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IDelayVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeClient;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

/**
 * Variable facade for variables determined by delays.
 * @author rubensworks
 */
public class DelayVariableFacade extends ProxyVariableFacade implements IDelayVariableFacade {

    public DelayVariableFacade(boolean generateId, int proxyId) {
        super(generateId, proxyId);
    }

    public DelayVariableFacade(int id, int proxyId) {
        super(id, proxyId);
    }

    @Override
    protected IVariableFacadeClient constructClient() {
        return new DelayVariableFacadeClient(this);
    }

    @Override
    protected MutableComponent getProxyNotInNetworkError() {
        return Component.translatable(L10NValues.DELAY_ERROR_DELAYNOTINNETWORK, Integer.toString(getProxyId()));
    }

    @Override
    protected MutableComponent getProxyInvalidError() {
        return Component.translatable(L10NValues.DELAY_ERROR_DELAYINVALID, Integer.toString(getProxyId()));
    }

    @Override
    protected MutableComponent getProxyInvalidTypeError(IPartNetwork network,
                                                                     IValueType containingValueType,
                                                                     IValueType actualType) {
        return Component.translatable(L10NValues.DELAY_ERROR_DELAYINVALIDTYPE,
                Component.translatable(containingValueType.getTranslationKey()),
                Component.translatable(actualType.getTranslationKey()));
    }

    protected Component getProxyTooltip() {
        return Component.translatable(L10NValues.DELAY_TOOLTIP_DELAYID, getProxyId());
    }
}
