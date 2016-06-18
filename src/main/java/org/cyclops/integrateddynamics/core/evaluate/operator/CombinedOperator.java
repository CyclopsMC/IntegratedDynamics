package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * An operator that somehow combines one or more operators.
 * @author rubensworks
 */
public class CombinedOperator extends OperatorBase {

    private final String unlocalizedType;

    public CombinedOperator(String symbol, String operatorName, OperatorsFunction function, IValueType outputType) {
        this(symbol, operatorName, function, new IValueType[]{ValueTypes.CATEGORY_ANY}, outputType, IConfigRenderPattern.PREFIX_1);
    }

    public CombinedOperator(String symbol, String operatorName, OperatorsFunction function, IValueType[] inputTypes,
                            IValueType outputType, IConfigRenderPattern configRenderPattern) {
        super(symbol, operatorName, inputTypes,
                outputType, function, configRenderPattern);
        this.unlocalizedType = "virtual";
    }

    @Override
    protected String getUnlocalizedType() {
        return unlocalizedType;
    }

    public static abstract class OperatorsFunction implements IFunction {

        private final IOperator[] operators;

        public OperatorsFunction(IOperator... operators) {
            this.operators = operators;
        }

        public IOperator[] getOperators() {
            return operators;
        }

        public int getInputOperatorCount() {
            return getOperators().length;
        }
    }

    public static class Conjunction extends OperatorsFunction {

        public Conjunction(IOperator... operators) {
            super(operators);
        }

        @Override
        public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
            IValue value = variables.getValue(0);
            for (IOperator operator : getOperators()) {
                IValue result = ValueHelpers.evaluateOperator(operator, value);
                if (!((ValueTypeBoolean.ValueBoolean) result).getRawValue()) {
                    return ValueTypeBoolean.ValueBoolean.of(false);
                }
            }
            return ValueTypeBoolean.ValueBoolean.of(true);
        }
    }

    public static class Disjunction extends OperatorsFunction {

        public Disjunction(IOperator... operators) {
            super(operators);
        }

        @Override
        public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
            IValue value = variables.getValue(0);
            for (IOperator operator : getOperators()) {
                IValue result = ValueHelpers.evaluateOperator(operator, value);
                if (((ValueTypeBoolean.ValueBoolean) result).getRawValue()) {
                    return ValueTypeBoolean.ValueBoolean.of(true);
                }
            }
            return ValueTypeBoolean.ValueBoolean.of(false);
        }
    }

    public static class Negation extends OperatorsFunction {

        public Negation(IOperator operator) {
            super(new IOperator[]{operator});
        }

        @Override
        public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
            IValue value = variables.getValue(0);
            IValue result = ValueHelpers.evaluateOperator(getOperators()[0], value);
            return ValueTypeBoolean.ValueBoolean.of(!((ValueTypeBoolean.ValueBoolean) result).getRawValue());
        }
    }

    public static class Pipe extends OperatorsFunction {

        public Pipe(IOperator... operators) {
            super(operators);
        }

        @Override
        public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
            IValue value = variables.getValue(0);
            for (IOperator operator : getOperators()) {
                value = ValueHelpers.evaluateOperator(operator, value);
            }
            return value;
        }
    }

    public static class Flip extends OperatorsFunction {

        public Flip(IOperator operator) {
            super(new IOperator[]{operator});
        }

        @Override
        public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
            int size = variables.getVariables().length;
            IValue[] values = new IValue[size];
            for (int i = 0; i < size; i++) {
                values[size - i - 1] = variables.getValue(i);
            }
            return ValueHelpers.evaluateOperator(getOperators()[0], values);
        }
    }
}
