package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An operator defined by an expression tree with parameters.
 * The parameters of the expression are the inputs of this operator.
 * @author rubensworks
 */
public class ExpressionOperator extends OperatorBase {

    private final Expression expression;
    private final Int2ObjectMap<IVariable<?>> refs;

    /**
     * @param expression The expression, its parameters define the operator inputs.
     * @param refs Bound variables for references in the expression.
     * @param outputType The output type.
     */
    public ExpressionOperator(Expression expression, Int2ObjectMap<IVariable<?>> refs, IValueType<?> outputType) {
        super(ExpressionPrinter.print(expression), "expression", "expression", null, false,
                ExpressionTypeChecker.getInputTypes(expression), outputType, new Function(), null);
        ((Function) getFunction()).operator = this;
        this.expression = expression;
        this.refs = refs;
    }

    /**
     * Create an operator for the given expression, inferring its output type.
     * @param expression The expression.
     * @param refs Bound variables for references in the expression.
     * @return The operator.
     */
    public static ExpressionOperator of(Expression expression, Int2ObjectMap<IVariable<?>> refs) {
        IValueType<?> outputType = ExpressionTypeChecker.check(expression, id -> {
            IVariable<?> variable = refs.get(id);
            return variable != null ? variable.getType() : null;
        }).outputType();
        return new ExpressionOperator(expression, refs, outputType);
    }

    public static ExpressionOperator of(Expression expression) {
        return of(expression, Int2ObjectMaps.emptyMap());
    }

    @Override
    protected String getUnlocalizedType() {
        return "virtual";
    }

    public Expression getExpression() {
        return expression;
    }

    public Int2ObjectMap<IVariable<?>> getRefs() {
        return refs;
    }

    @Override
    public MutableComponent getLocalizedNameFull() {
        return Component.translatable(L10NValues.OPERATOR_EXPRESSION_NAME, getSymbol());
    }

    @Override
    public void loadTooltip(List<Component> lines, boolean appendOptionalInfo) {
        lines.add(getLocalizedNameFull());
        IValueType[] inputTypes = getInputTypes();
        for (int i = 0; i < inputTypes.length; i++) {
            lines.add(Component.translatable(L10NValues.OPERATOR_TOOLTIP_INPUTTYPENAME, i + 1)
                    .append(Component.translatable(inputTypes[i].getTranslationKey()))
                    .withStyle(inputTypes[i].getDisplayColorFormat()));
        }
        lines.add(Component.translatable(L10NValues.OPERATOR_TOOLTIP_OUTPUTTYPENAME,
                getOutputType().getDisplayColorFormat() + Component.translatable(getOutputType().getTranslationKey()).getString()));
    }

    /**
     * @return The expression where all references are replaced by their current values.
     * @throws EvaluationException If a referenced value could not be evaluated.
     */
    public Expression getMaterializedExpression() throws EvaluationException {
        return expression.withRoot(materializeRefs(expression.root(), true));
    }

    private ExpressionNode materializeRefs(ExpressionNode node, boolean strict) throws EvaluationException {
        if (node instanceof ExpressionNode.Ref ref) {
            IVariable<?> variable = refs.get(ref.variableId());
            if (variable == null) {
                if (strict) {
                    throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_VARIABLENOTINNETWORK,
                            Integer.toString(ref.variableId())));
                }
                return node;
            }
            IValue value;
            try {
                value = variable.getValue();
            } catch (EvaluationException e) {
                if (strict) {
                    throw e;
                }
                value = variable.getType().getDefault();
            }
            IValueType type = value.getType();
            return new ExpressionNode.Const(strict ? type.materialize(value) : value);
        }
        if (node instanceof ExpressionNode.Const constant && strict) {
            IValueType type = constant.value().getType();
            return new ExpressionNode.Const(type.materialize(constant.value()));
        }
        if (node instanceof ExpressionNode.Operator op) {
            List<ExpressionNode> children = new ArrayList<>(op.children().size());
            for (ExpressionNode child : op.children()) {
                children.add(materializeRefs(child, strict));
            }
            return new ExpressionNode.Operator(op.operator(), children);
        }
        return node;
    }

    @Override
    public IOperator materialize() throws EvaluationException {
        return new ExpressionOperator(getMaterializedExpression(), Int2ObjectMaps.emptyMap(), getOutputType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpressionOperator that)) return false;
        return expression.equals(that.expression) && refs.equals(that.refs)
                && getOutputType() == that.getOutputType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, refs);
    }

    @Override
    public String toString() {
        return "[Expression: " + getSymbol() + "]";
    }

    public static class Function implements IFunction {

        private ExpressionOperator operator;

        @Override
        public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
            return ExpressionEvaluator.buildBound(operator.expression.root(), variables.getVariables(), operator.refs).getValue();
        }
    }

    public static class Serializer implements IOperatorSerializer<ExpressionOperator> {

        @Override
        public boolean canHandle(IOperator operator) {
            return operator instanceof ExpressionOperator;
        }

        @Override
        public ResourceLocation getUniqueName() {
            return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "expression");
        }

        @Override
        public Tag serialize(ValueDeseralizationContext valueDeseralizationContext, ExpressionOperator operator) {
            Expression expression;
            try {
                expression = operator.expression.withRoot(operator.materializeRefs(operator.expression.root(), false));
            } catch (EvaluationException e) {
                // Can not happen in non-strict mode
                expression = operator.expression;
            }
            CompoundTag tag = ExpressionTrees.serialize(valueDeseralizationContext, expression);
            tag.putString("output", operator.getOutputType().getUniqueName().toString());
            return tag;
        }

        @Override
        public ExpressionOperator deserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value) throws EvaluationException {
            if (!(value instanceof CompoundTag tag)) {
                throw new EvaluationException(Component.translatable(L10NValues.VALUETYPE_ERROR_DESERIALIZE,
                        value.toString(), "not a compound"));
            }
            Expression expression = ExpressionTrees.deserialize(valueDeseralizationContext, tag);
            IValueType<?> outputType = ValueTypes.REGISTRY.getValueType(ResourceLocation.parse(tag.getString("output")));
            if (outputType == null) {
                return ExpressionOperator.of(expression);
            }
            return new ExpressionOperator(expression, Int2ObjectMaps.emptyMap(), outputType);
        }
    }
}
