package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.integrateddynamics.core.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;

import java.util.Arrays;

/**
 * A basic abstract implementation of an operator.
 * @author rubensworks
 */
public abstract class BaseOperator implements IOperator {

    private final String operatorName;
    private final IValueType[] inputTypes;
    private final IValueType outputType;
    private final IFunction function;

    protected BaseOperator(String operatorName, IValueType[] inputTypes, IValueType outputType,
                           IFunction function) {
        this.operatorName = operatorName;
        this.inputTypes = inputTypes;
        this.outputType = outputType;
        this.function = function;
    }

    protected static IValueType[] constructInputVariables(int length, IValueType defaultType) {
        IValueType[] values = new IValueType[length];
        Arrays.fill(values, defaultType);
        return values;
    }

    @Override
    public String getOperatorName() {
        return operatorName;
    }

    @Override
    public IValueType[] getInputTypes() {
        return inputTypes;
    }

    @Override
    public IValueType getOutputType() {
        return outputType;
    }

    @Override
    public IValue evaluate(IVariable[] input) throws EvaluationException {
        // Input size checking
        if(input.length != getInputTypes().length) {
            throw new EvaluationException(String.format("The operator %s received an input of length %s while it " +
                    "needs a length of %s.", this, input.length, getInputTypes().length));
        }
        // Input types checking
        for(int i = 0; i < input.length; i++) {
            IVariable inputVar = input[i];
            if(inputVar == null) {
                throw new EvaluationException(String.format("The operator %s received an input with a null variable " +
                        "at position %s.", this, i));
            }
            if(getInputTypes()[i] != inputVar.getType()) {
                throw new EvaluationException(String.format("The operator %s received an input with type %s " +
                        "at position %s while the type %s was expected.", this, inputVar.getType(), i,
                        getInputTypes()[i]));
            }
            i++;
        }
        return function.evaluate(input);
    }

    @Override
    public String toString() {
        return "[Operator: " + getOperatorName() + "]";
    }

    public static interface IFunction {

        /**
         * Evaluate this function for the given input.
         * @param variables The input variables. They can be considered type-safe.
         * @return The output value.
         * @throws EvaluationException If an exception occurs while evaluating
         */
        public IValue evaluate(IVariable... variables) throws EvaluationException;

    }

}
