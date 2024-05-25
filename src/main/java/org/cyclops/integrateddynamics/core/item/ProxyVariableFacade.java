package org.cyclops.integrateddynamics.core.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IProxyVariableFacade;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.blockentity.BlockEntityProxy;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.List;
import java.util.Optional;

/**
 * Variable facade for variables determined by proxies.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = true)
@Data
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

    protected Optional<BlockEntityProxy> getProxy(IPartNetwork network) {
        DimPos dimPos = network.getProxy(proxyId);
        if(dimPos != null) {
            return BlockEntityHelpers.get(dimPos, BlockEntityProxy.class);
        }
        return Optional.empty();
    }

    protected Optional<IVariable> getTargetVariable(IPartNetwork network) {
        return getProxy(network)
                .map(tile -> tile.getVariable(network));
    }

    @Override
    public <V extends IValue> IVariable<V> getVariable(IPartNetwork network) {
        if(isValid()) {
            // Check if we are entering an infinite recursion (e.g. proxies refering to each other)
            if(this.isGettingVariable) {
                throw new VariableRecursionException("Detected infinite recursion for variable references.");
            }
            this.isGettingVariable = true;
            IVariable<V> variable = getTargetVariable(network).orElse(null);
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
    public void validate(IPartNetwork partNetwork, IValidator validator, IValueType containingValueType) {
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

        // Check if we are entering an infinite recursion (e.g. proxies refering to each other)
        if(this.isValidatingVariable) {
            throw new VariableRecursionException("Detected infinite recursion for variable references.");
        }
        this.isValidatingVariable = true;
        getVariable(partNetwork);
        this.isValidatingVariable = false;
    }

    @Override
    public IValueType getOutputType() {
        return ValueTypes.CATEGORY_ANY;
    }

    protected Component getProxyTooltip() {
        return Component.translatable(L10NValues.PROXY_TOOLTIP_PROXYID, proxyId);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(List<Component> list, Level world) {
        if(isValid()) {
            list.add(getProxyTooltip());
        }
        super.appendHoverText(list, world);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addModelOverlay(IVariableModelBaked variableModelBaked, List<BakedQuad> quads, RandomSource random, ModelData modelData) {
        if(isValid()) {
            quads.addAll(variableModelBaked.getSubModels(VariableModelProviders.PROXY).getBakedModel().getQuads(null, null, random, modelData, null));
        }
    }

    public static class VariableRecursionException extends IllegalArgumentException {

        public VariableRecursionException(String msg) {
            super(msg);
        }

    }
}
