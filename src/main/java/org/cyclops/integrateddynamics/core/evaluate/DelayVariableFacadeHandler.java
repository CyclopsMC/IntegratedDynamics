package org.cyclops.integrateddynamics.core.evaluate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IDelayVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.item.DelayVariableFacade;

/**
 * Handler for delay variable facades.
 * @author rubensworks
 */
public class DelayVariableFacadeHandler implements IVariableFacadeHandler<IDelayVariableFacade> {

    private static final IDelayVariableFacade INVALID_FACADE = new DelayVariableFacade(false, -1);
    private static DelayVariableFacadeHandler _instance;

    private DelayVariableFacadeHandler() {

    }

    public static DelayVariableFacadeHandler getInstance() {
        if(_instance == null) _instance = new DelayVariableFacadeHandler();
        return _instance;
    }

    @Override
    public ResourceLocation getUniqueName() {
        return new ResourceLocation(Reference.MOD_ID, "delay");
    }

    @Override
    public IDelayVariableFacade getVariableFacade(ValueDeseralizationContext valueDeseralizationContext, int id, CompoundTag tag) {
        if(!tag.contains("partId", Tag.TAG_INT)) {
            return INVALID_FACADE;
        }
        return new DelayVariableFacade(id, tag.getInt("partId"));
    }

    @Override
    public void setVariableFacade(CompoundTag tag, IDelayVariableFacade variableFacade) {
        tag.putInt("partId", variableFacade.getProxyId());
    }
}
