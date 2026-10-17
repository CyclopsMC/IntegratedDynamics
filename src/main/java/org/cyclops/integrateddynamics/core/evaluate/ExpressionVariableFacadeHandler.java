package org.cyclops.integrateddynamics.core.evaluate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IExpressionVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTrees;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.item.ExpressionVariableFacade;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for expression variable facades.
 * @author rubensworks
 */
public class ExpressionVariableFacadeHandler implements IVariableFacadeHandler<IExpressionVariableFacade> {

    public static final String KEY_EXPRESSION = "expression";
    public static final String KEY_REFERENCE_TYPES = "referenceTypes";

    private static final IExpressionVariableFacade INVALID_FACADE = new ExpressionVariableFacade(false, null);
    private static ExpressionVariableFacadeHandler _instance;

    private ExpressionVariableFacadeHandler() {

    }

    public static ExpressionVariableFacadeHandler getInstance() {
        if(_instance == null) _instance = new ExpressionVariableFacadeHandler();
        return _instance;
    }

    @Override
    public ResourceLocation getUniqueName() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "expression");
    }

    @Override
    public IExpressionVariableFacade getVariableFacade(ValueDeseralizationContext valueDeseralizationContext, int id, CompoundTag tag) {
        if(!tag.contains(KEY_EXPRESSION, Tag.TAG_COMPOUND)) {
            return INVALID_FACADE;
        }
        Expression expression;
        try {
            expression = ExpressionTrees.deserialize(valueDeseralizationContext, tag.getCompound(KEY_EXPRESSION));
        } catch (EvaluationException e) {
            return INVALID_FACADE;
        }
        Map<Integer, IValueType<?>> referenceTypes = new HashMap<>();
        CompoundTag referenceTypesTag = tag.getCompound(KEY_REFERENCE_TYPES);
        for (String key : referenceTypesTag.getAllKeys()) {
            ResourceLocation typeName = ResourceLocation.tryParse(referenceTypesTag.getString(key));
            IValueType<?> valueType = typeName == null ? null : ValueTypes.REGISTRY.getValueType(typeName);
            if (valueType != null) {
                try {
                    referenceTypes.put(Integer.parseInt(key), valueType);
                } catch (NumberFormatException e) {
                    // Ignore invalid entries
                }
            }
        }
        return new ExpressionVariableFacade(id, expression, referenceTypes);
    }

    @Override
    public void setVariableFacade(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag, IExpressionVariableFacade variableFacade) {
        if (variableFacade.getExpression() != null) {
            tag.put(KEY_EXPRESSION, ExpressionTrees.serialize(valueDeseralizationContext, variableFacade.getExpression()));
        }
        if (!variableFacade.getReferenceTypes().isEmpty()) {
            CompoundTag referenceTypesTag = new CompoundTag();
            variableFacade.getReferenceTypes().forEach((id, type) -> referenceTypesTag.putString(String.valueOf(id), type.getUniqueName().toString()));
            tag.put(KEY_REFERENCE_TYPES, referenceTypesTag);
        }
    }

    @Override
    public boolean isInstance(IVariableFacade variableFacade) {
        return variableFacade instanceof IExpressionVariableFacade;
    }

    @Override
    public boolean isInstance(IVariable<?> variable) {
        return variable instanceof IVariable;
    }
}
