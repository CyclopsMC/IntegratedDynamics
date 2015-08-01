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
    private final IConfigRenderPattern renderPattern;

    protected BaseOperator(String symbol, String operatorName, IValueType[] inputTypes, IValueType outputType,
                           IFunction function, IConfigRenderPattern renderPattern) {
        this.symbol = symbol;
        this.operatorName = operatorName;
        this.inputTypes = inputTypes;
        this.outputType = outputType;
        this.function = function;
        this.renderPattern = renderPattern;
        if(renderPattern.getSlotPositions().length != inputTypes.length) {
            throw new IllegalArgumentException(String.format("The given config render pattern with %s slots is not " +
                    "compatible with the number of input types %s for %s",
                    renderPattern.getSlotPositions().length, inputTypes.length, symbol));
        }
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
                    i + 1, inputTypes[i].getDisplayColorFormat() + L10NHelpers.localize(inputTypes[i].getUnlocalizedName())));
        }
        lines.add(L10NHelpers.localize("operator.tooltip.outputTypeName", getOutputType().getDisplayColorFormat() + outputTypeName));
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
    public IValueType getConditionalOutputType(IVariable[] input) {
        return outputType;
    }

    protected IValueType[] getValueTypes(IVariable[] variables) {
        IValueType[] valueTypes = new IValueType[variables.length];
        for(int i = 0; i < valueTypes.length; i++) {
            IVariable variable = variables[i];
            valueTypes[i] = variable == null ? null : variable.getType();
        }
        return valueTypes;
    }

    @Override
    public IValue evaluate(IVariable[] input) throws EvaluationException {
        L10NHelpers.UnlocalizedString error = validateTypes(getValueTypes(input));
        if(error != null) {
            throw new EvaluationException(error.localize());
        }
        return function.evaluate(input);
    }

    @Override
    public int getRequiredInputLength() {
        return getInputTypes().length;
    }

    @Override
    public L10NHelpers.UnlocalizedString validateTypes(IValueType[] input) {
        // Input size checking
        int requiredInputLength = getRequiredInputLength();
        if(input.length != requiredInputLength) {
            return new L10NHelpers.UnlocalizedString("operator.error.wrongInputLength",
                    this.getOperatorName(), input.length, requiredInputLength);
        }
        // Input types checking
        for(int i = 0; i < requiredInputLength; i++) {
            IValueType inputType = input[i];
            if(inputType == null) {
                return new L10NHelpers.UnlocalizedString("operator.error.nullType", this.getOperatorName(), Integer.toString(i));
            }
            if(getInputTypes()[i] != inputType) {
                return new L10NHelpers.UnlocalizedString("operator.error.wrongType",
                        this.getOperatorName(), new L10NHelpers.UnlocalizedString(inputType.getUnlocalizedName()),
                        Integer.toString(i), new L10NHelpers.UnlocalizedString(getInputTypes()[i].getUnlocalizedName()));
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

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return renderPattern;
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
