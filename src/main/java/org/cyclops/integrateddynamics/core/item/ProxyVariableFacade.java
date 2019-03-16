package org.cyclops.integrateddynamics.core.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.L10NHelpers;
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

    protected TileProxy getProxy(IPartNetwork network) {
        DimPos dimPos = network.getProxy(proxyId);
        if(dimPos != null) {
            return TileHelpers.getSafeTile(dimPos, TileProxy.class);
        }
        return null;
    }

    protected IVariable getTargetVariable(IPartNetwork network) {
        TileProxy tile = getProxy(network);
        if(tile != null) {
            IVariable variable = tile.getVariable(network);
            return variable;
        }
        return null;
    }

    @Override
    public <V extends IValue> IVariable<V> getVariable(IPartNetwork network) {
        if(isValid()) {
            // Check if we are entering an infinite recursion (e.g. proxies refering to each other)
            if(this.isGettingVariable) {
                throw new VariableRecursionException("Detected infinite recursion for variable references.");
            }
            this.isGettingVariable = true;
            IVariable<V> variable = getTargetVariable(network);
            this.isGettingVariable = false;
            return variable;
        }
        return null;
    }

    @Override
    public boolean isValid() {
        return proxyId >= 0;
    }

    protected L10NHelpers.UnlocalizedString getProxyNotInNetworkError() {
        return new L10NHelpers.UnlocalizedString(L10NValues.PROXY_ERROR_PROXYNOTINNETWORK, Integer.toString(proxyId));
    }

    protected L10NHelpers.UnlocalizedString getProxyInvalidError() {
        return new L10NHelpers.UnlocalizedString(L10NValues.PROXY_ERROR_PROXYINVALID, Integer.toString(proxyId));
    }

    protected L10NHelpers.UnlocalizedString getProxyInvalidTypeError(IPartNetwork network,
                                                                     IValueType containingValueType,
                                                                     IValueType actualType) {
        return new L10NHelpers.UnlocalizedString(L10NValues.PROXY_ERROR_PROXYINVALIDTYPE,
                Integer.toString(proxyId),
                new L10NHelpers.UnlocalizedString(containingValueType.getTranslationKey()),
                new L10NHelpers.UnlocalizedString(actualType.getTranslationKey()));
    }

    @Override
    public void validate(IPartNetwork network, IValidator validator, IValueType containingValueType) {
        if (!isValid()) {
            validator.addError(new L10NHelpers.UnlocalizedString(L10NValues.VARIABLE_ERROR_INVALIDITEM));
        } else if (network.getProxy(proxyId) == null) {
            validator.addError(getProxyNotInNetworkError());
        } else if (getTargetVariable(network) == null) {
            validator.addError(getProxyInvalidError());
        } else if (!ValueHelpers.correspondsTo(containingValueType, getTargetVariable(network).getType())) {
            validator.addError(getProxyInvalidTypeError(network, containingValueType,
                    getTargetVariable(network).getType()));
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

    protected String getProxyTooltip() {
        return L10NHelpers.localize(L10NValues.PROXY_TOOLTIP_PROXYID, proxyId);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(List<String> list, World world) {
        if(isValid()) {
            list.add(getProxyTooltip());
        }
        super.addInformation(list, world);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addModelOverlay(IVariableModelBaked variableModelBaked, List<BakedQuad> quads) {
        if(isValid()) {
            quads.addAll(variableModelBaked.getSubModels(VariableModelProviders.PROXY).getBakedModel().getQuads(null, null, 0L));
        }
    }

    public static class VariableRecursionException extends IllegalArgumentException {

        public VariableRecursionException(String msg) {
            super(msg);
        }

    }
}
