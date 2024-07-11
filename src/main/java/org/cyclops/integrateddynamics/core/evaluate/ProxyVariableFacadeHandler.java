package org.cyclops.integrateddynamics.core.evaluate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IProxyVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.item.ProxyVariableFacade;

/**
 * Handler for proxy variable facades.
 * @author rubensworks
 */
public class ProxyVariableFacadeHandler implements IVariableFacadeHandler<IProxyVariableFacade> {

    private static final IProxyVariableFacade INVALID_FACADE = new ProxyVariableFacade(false, -1);
    private static ProxyVariableFacadeHandler _instance;

    private ProxyVariableFacadeHandler() {

    }

    public static ProxyVariableFacadeHandler getInstance() {
        if(_instance == null) _instance = new ProxyVariableFacadeHandler();
        return _instance;
    }

    @Override
    public ResourceLocation getUniqueName() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "proxy");
    }

    @Override
    public IProxyVariableFacade getVariableFacade(ValueDeseralizationContext valueDeseralizationContext, int id, CompoundTag tag) {
        if(!tag.contains("partId", Tag.TAG_INT)) {
            return INVALID_FACADE;
        }
        return new ProxyVariableFacade(id, tag.getInt("partId"));
    }

    @Override
    public void setVariableFacade(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag, IProxyVariableFacade variableFacade) {
        tag.putInt("partId", variableFacade.getProxyId());
    }

    @Override
    public boolean isInstance(IVariableFacade variableFacade) {
        return variableFacade instanceof IProxyVariableFacade;
    }

    @Override
    public boolean isInstance(IVariable<?> variable) {
        return variable instanceof IVariable;
    }
}
