package org.cyclops.integrateddynamics.core.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IProxyVariableFacade;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.tileentity.TileProxy;

import java.util.List;
import java.util.Optional;
import java.util.Random;

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

    protected Optional<TileProxy> getProxy(IPartNetwork network) {
        DimPos dimPos = network.getProxy(proxyId);
        if(dimPos != null) {
            return TileHelpers.getSafeTile(dimPos, TileProxy.class);
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

    protected ITextComponent getProxyNotInNetworkError() {
        return new TranslationTextComponent(L10NValues.PROXY_ERROR_PROXYNOTINNETWORK, Integer.toString(proxyId));
    }

    protected ITextComponent getProxyInvalidError() {
        return new TranslationTextComponent(L10NValues.PROXY_ERROR_PROXYINVALID, Integer.toString(proxyId));
    }

    protected ITextComponent getProxyInvalidTypeError(IPartNetwork network,
                                                                     IValueType containingValueType,
                                                                     IValueType actualType) {
        return new TranslationTextComponent(L10NValues.PROXY_ERROR_PROXYINVALIDTYPE,
                Integer.toString(proxyId),
                new TranslationTextComponent(containingValueType.getTranslationKey()),
                new TranslationTextComponent(actualType.getTranslationKey()));
    }

    @Override
    public void validate(IPartNetwork network, IValidator validator, IValueType containingValueType) {
        Optional<IVariable> targetVariable = getTargetVariable(network);
        if (!isValid()) {
            validator.addError(new TranslationTextComponent(L10NValues.VARIABLE_ERROR_INVALIDITEM));
        } else if (network.getProxy(proxyId) == null) {
            validator.addError(getProxyNotInNetworkError());
        } else if (!targetVariable.isPresent()) {
            validator.addError(getProxyInvalidError());
        } else if (!ValueHelpers.correspondsTo(containingValueType, targetVariable.get().getType())) {
            validator.addError(getProxyInvalidTypeError(network, containingValueType,
                    targetVariable.get().getType()));
        }

        // Check if we are entering an infinite recursion (e.g. proxies refering to each other)
        if(this.isValidatingVariable) {
            throw new VariableRecursionException("Detected infinite recursion for variable references.");
        }
        this.isValidatingVariable = true;
        getVariable(network);
        this.isValidatingVariable = false;
    }

    @Override
    public IValueType getOutputType() {
        return ValueTypes.CATEGORY_ANY;
    }

    protected ITextComponent getProxyTooltip() {
        return new TranslationTextComponent(L10NValues.PROXY_TOOLTIP_PROXYID, proxyId);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(List<ITextComponent> list, World world) {
        if(isValid()) {
            list.add(getProxyTooltip());
        }
        super.addInformation(list, world);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addModelOverlay(IVariableModelBaked variableModelBaked, List<BakedQuad> quads, Random random, IModelData modelData) {
        if(isValid()) {
            quads.addAll(variableModelBaked.getSubModels(VariableModelProviders.PROXY).getBakedModel().getQuads(null, null, random, modelData));
        }
    }

    public static class VariableRecursionException extends IllegalArgumentException {

        public VariableRecursionException(String msg) {
            super(msg);
        }

    }
}
