package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;

import java.util.Arrays;
import java.util.List;

/**
 * A basic abstract implementation of an operator.
 * @author rubensworks
 */
public abstract class BaseOperator implements IOperator {

    private final String symbol;
    private final String operatorName;
    private final IValueType[] inputTypes;
    private final IValueType outputType;
    private final IFunction function;

    protected BaseOperator(String symbol, String operatorName, IValueType[] inputTypes, IValueType outputType,
                           IFunction function) {
        this.symbol = symbol;
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

    protected abstract String getUnlocalizedType();

    @Override
    public String getUnlocalizedName() {
        return getUnlocalizedPrefix() + ".name";
    }

    protected String getUnlocalizedPrefix() {
        return "operator.operators." + getModId() + "." + getUnlocalizedType();
    }

    protected String getOperatorName() {
        return this.operatorName;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public void loadTooltip(List<String> lines, boolean appendOptionalInfo) {
        String operatorName = L10NHelpers.localize(getUnlocalizedName());
        String symbol = getSymbol();
        String outputTypeName = L10NHelpers.localize(getOutputType().getUnlocalizedName());
        lines.add(L10NHelpers.localize("operator.tooltip.operatorName", operatorName, symbol));
        IValueType[] inputTypes = getInputTypes();
        for(int i = 0; i < inputTypes.length; i++) {
            lines.add(L10NHelpers.localize("operator.tooltip.inputTypeName",
                    i + 1, L10NHelpers.localize(inputTypes[i].getUnlocalizedName())));
        }
        lines.add(L10NHelpers.localize("operator.tooltip.outputTypeName", outputTypeName));
        if(appendOptionalInfo) {
            L10NHelpers.addOptionalInfo(lines, getUnlocalizedPrefix());
        }
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
    public L10NHelpers.UnlocalizedString validateTypes(IValueType[] input) {
        // Input size checking
        if(input.length != getInputTypes().length) {
            return new L10NHelpers.UnlocalizedString("operator.error.wrongInputLength",
                    this.getOperatorName(), input.length, getInputTypes().length);
        }
        // Input types checking
        for(int i = 0; i < input.length; i++) {
            IValueType inputType = input[i];
            if(inputType == null) {
                return new L10NHelpers.UnlocalizedString("operator.error.nullType", this.getOperatorName(), Integer.toString(i));
            }
            if(getInputTypes()[i] != inputType) {
                return new L10NHelpers.UnlocalizedString("operator.error.wrongType",
                        this.getOperatorName(), new L10NHelpers.UnlocalizedString(inputType.getUnlocalizedName()),
                        i, new L10NHelpers.UnlocalizedString(getInputTypes()[i].getUnlocalizedName()));
            }
            i++;
        }
        return null;
    }

    @Override
    public String toString() {
        return "[Operator: " + getOperatorName() + "]";
    }

    protected String getModId() {
        return Reference.MOD_ID;
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
