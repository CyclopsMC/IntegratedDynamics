package org.cyclops.integrateddynamics.core.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IProxyVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeClient;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.blockentity.BlockEntityProxy;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Variable facade for variables determined by proxies.
 * @author rubensworks
 */
public class ProxyVariableFacade extends VariableFacadeBase implements IProxyVariableFacade {

    private final int proxyId;
    private boolean isValidatingVariable = false;
    private boolean isGettingVariable = false;

    public ProxyVariableFacade(boolean generateId, int proxyId) {
        super(generateId);
        this.proxyId = proxyId;
    }

    public ProxyVariableFacade(int id, int proxyId) {
        super(id);
        this.proxyId = proxyId;
    }

    public int getProxyId() {
        return proxyId;
    }

    public boolean isValidatingVariable() {
        return isValidatingVariable;
    }

    public void setValidatingVariable(boolean validatingVariable) {
        isValidatingVariable = validatingVariable;
    }

    public boolean isGettingVariable() {
        return isGettingVariable;
    }

    public void setGettingVariable(boolean gettingVariable) {
        isGettingVariable = gettingVariable;
    }

    protected Optional<BlockEntityProxy> getProxy(IPartNetwork network) {
        DimPos dimPos = network.getProxy(proxyId);
        if(dimPos != null) {
            Level level = dimPos.getLevel(false);
            if (level != null) {
                return IModHelpers.get().getBlockEntityHelpers().get(level, dimPos.getBlockPos(), BlockEntityProxy.class);
            }
        }
        return Optional.empty();
    }

    protected Optional<IVariable> getTargetVariable(IPartNetwork network) {
        return getProxy(network)
                .map(tile -> tile.getVariable(network));
    }

    @Override
    public <V extends IValue> IVariable<V> getVariable(INetwork network, IPartNetwork partNetwork) {
        if(isValid()) {
            // Check if we are entering an infinite recursion (e.g. proxies refering to each other)
            if(this.isGettingVariable) {
                throw new VariableRecursionException("Detected infinite recursion for variable references.");
            }
            this.isGettingVariable = true;
            IVariable<V> variable = getTargetVariable(partNetwork).orElse(null);
            this.isGettingVariable = false;
            return variable;
        }
        return null;
    }

    @Override
    public boolean isValid() {
        return proxyId >= 0;
    }

    protected MutableComponent getProxyNotInNetworkError() {
        return Component.translatable(L10NValues.PROXY_ERROR_PROXYNOTINNETWORK, Integer.toString(proxyId));
    }

    protected MutableComponent getProxyInvalidError() {
        return Component.translatable(L10NValues.PROXY_ERROR_PROXYINVALID, Integer.toString(proxyId));
    }

    protected MutableComponent getProxyInvalidTypeError(IPartNetwork network,
                                                                     IValueType containingValueType,
                                                                     IValueType actualType) {
        return Component.translatable(L10NValues.PROXY_ERROR_PROXYINVALIDTYPE,
                Integer.toString(proxyId),
                Component.translatable(containingValueType.getTranslationKey()),
                Component.translatable(actualType.getTranslationKey()));
    }

    @Override
    public void validate(INetwork network, IPartNetwork partNetwork, IValidator validator, IValueType containingValueType) {
        Optional<IVariable> targetVariable = getTargetVariable(partNetwork);
        if (!isValid()) {
            validator.addError(Component.translatable(L10NValues.VARIABLE_ERROR_INVALIDITEM));
        } else if (partNetwork.getProxy(proxyId) == null) {
            validator.addError(getProxyNotInNetworkError());
        } else if (!targetVariable.isPresent()) {
            validator.addError(getProxyInvalidError());
        } else if (!ValueHelpers.correspondsTo(containingValueType, targetVariable.get().getType())) {
            validator.addError(getProxyInvalidTypeError(partNetwork, containingValueType,
                    targetVariable.get().getType()));
        }

        // Check if we are entering an infinite recursion (e.g. proxies referring to each other)
        if(this.isValidatingVariable) {
            throw new VariableRecursionException("Detected infinite recursion for variable references.");
        }
        this.isValidatingVariable = true;
        getVariable(network, partNetwork);
        this.isValidatingVariable = false;
    }

    @Override
    public IValueType getOutputType() {
        return ValueTypes.CATEGORY_ANY;
    }

    protected Component getProxyTooltip() {
        return Component.translatable(L10NValues.PROXY_TOOLTIP_PROXYID, proxyId);
    }

    @Override
    protected IVariableFacadeClient constructClient() {
        return new ProxyVariableFacadeClient(this);
    }

    @Override
    public void appendHoverText(Consumer<Component> tooltipAdder, Item.TooltipContext context) {
        if(isValid()) {
            tooltipAdder.accept(getProxyTooltip());
        }
        super.appendHoverText(tooltipAdder, context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProxyVariableFacade that = (ProxyVariableFacade) o;
        return proxyId == that.proxyId && isValidatingVariable == that.isValidatingVariable && isGettingVariable == that.isGettingVariable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), proxyId, isValidatingVariable, isGettingVariable);
    }

    public static class VariableRecursionException extends IllegalArgumentException {

        public VariableRecursionException(String msg) {
            super(msg);
        }

    }
}
