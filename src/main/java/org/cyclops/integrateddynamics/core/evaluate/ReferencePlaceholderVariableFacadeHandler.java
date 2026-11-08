package org.cyclops.integrateddynamics.core.evaluate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.item.ReferencePlaceholderVariableFacade;

/**
 * Handler for reference placeholder variable facades, used in the slots of the logic programmer.
 * @author rubensworks
 */
public class ReferencePlaceholderVariableFacadeHandler implements IVariableFacadeHandler<ReferencePlaceholderVariableFacade> {

    private static final String KEY_TYPE = "valueType";
    private static ReferencePlaceholderVariableFacadeHandler _instance;

    private ReferencePlaceholderVariableFacadeHandler() {

    }

    public static ReferencePlaceholderVariableFacadeHandler getInstance() {
        if(_instance == null) _instance = new ReferencePlaceholderVariableFacadeHandler();
        return _instance;
    }

    @Override
    public ResourceLocation getUniqueName() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "reference_placeholder");
    }

    @Override
    public ReferencePlaceholderVariableFacade getVariableFacade(ValueDeseralizationContext valueDeseralizationContext, int id, CompoundTag tag) {
        ResourceLocation typeName = ResourceLocation.tryParse(tag.getString(KEY_TYPE));
        IValueType<?> valueType = typeName == null ? null : ValueTypes.REGISTRY.getValueType(typeName);
        return new ReferencePlaceholderVariableFacade(id, valueType);
    }

    @Override
    public void setVariableFacade(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag, ReferencePlaceholderVariableFacade variableFacade) {
        if (variableFacade.getValueType() != null) {
            tag.putString(KEY_TYPE, variableFacade.getValueType().getUniqueName().toString());
        }
    }

    @Override
    public boolean isInstance(IVariableFacade variableFacade) {
        return variableFacade instanceof ReferencePlaceholderVariableFacade;
    }

    @Override
    public boolean isInstance(IVariable<?> variable) {
        return false;
    }
}
