package org.cyclops.integrateddynamics.core.evaluate;

import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariableFacadePredicate;
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
import java.util.Optional;

/**
 * Handler for expression variable facades.
 * @author rubensworks
 */
public class ExpressionVariableFacadeHandler implements IVariableFacadeHandler<IExpressionVariableFacade> {

    public static final String KEY_EXPRESSION = "expression";
    public static final String KEY_REFERENCE_TYPES = "referenceTypes";
    public static final String KEY_TEXT = "text";

    private static final IExpressionVariableFacade INVALID_FACADE = new ExpressionVariableFacade(false, null);
    private static ExpressionVariableFacadeHandler _instance;

    private ExpressionVariableFacadeHandler() {

    }

    public static ExpressionVariableFacadeHandler getInstance() {
        if(_instance == null) _instance = new ExpressionVariableFacadeHandler();
        return _instance;
    }

    @Override
    public Identifier getUniqueName() {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "expression");
    }

    @Override
    public IExpressionVariableFacade getVariableFacade(ValueDeseralizationContext valueDeseralizationContext, int id, CompoundTag tag) {
        if(!tag.contains(KEY_EXPRESSION)) {
            return INVALID_FACADE;
        }
        Expression expression;
        try {
            expression = ExpressionTrees.deserialize(valueDeseralizationContext, tag.getCompoundOrEmpty(KEY_EXPRESSION));
        } catch (EvaluationException e) {
            return INVALID_FACADE;
        }
        Map<Integer, IValueType<?>> referenceTypes = new HashMap<>();
        CompoundTag referenceTypesTag = tag.getCompoundOrEmpty(KEY_REFERENCE_TYPES);
        for (String key : referenceTypesTag.keySet()) {
            Identifier typeName = Identifier.tryParse(referenceTypesTag.getStringOr(key, ""));
            IValueType<?> valueType = typeName == null ? null : ValueTypes.REGISTRY.getValueType(typeName);
            if (valueType != null) {
                try {
                    referenceTypes.put(Integer.parseInt(key), valueType);
                } catch (NumberFormatException e) {
                    // Ignore invalid entries
                }
            }
        }
        String text = tag.getString(KEY_TEXT).orElse(null);
        return new ExpressionVariableFacade(id, expression, referenceTypes, text);
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
        if (variableFacade.getText() != null) {
            tag.putString(KEY_TEXT, variableFacade.getText());
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

    /**
     * Matches expression cards, optionally by the number of operators and parameters of their expression.
     */
    public static class ExpressionVariableFacadePredicate extends VariableFacadePredicate<IExpressionVariableFacade> {

        private final Optional<MinMaxBounds.Ints> operators;
        private final Optional<MinMaxBounds.Ints> parameters;

        public ExpressionVariableFacadePredicate(Optional<MinMaxBounds.Ints> operators, Optional<MinMaxBounds.Ints> parameters) {
            super(IExpressionVariableFacade.class);
            this.operators = operators;
            this.parameters = parameters;
        }

        public Optional<MinMaxBounds.Ints> getOperators() {
            return operators;
        }

        public Optional<MinMaxBounds.Ints> getParameters() {
            return parameters;
        }

        @Override
        protected boolean testTyped(IExpressionVariableFacade variableFacade) {
            Expression expression = variableFacade.getExpression();
            return expression != null
                    && (operators.isEmpty() || operators.get().matches(ExpressionTrees.countOperators(expression.root())))
                    && (parameters.isEmpty() || parameters.get().matches(expression.parameters().size()));
        }
    }
}
