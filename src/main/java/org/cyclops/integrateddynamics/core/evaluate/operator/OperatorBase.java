package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.Arrays;
import java.util.List;

/**
 * A basic abstract implementation of an operator.
 * @author rubensworks
 */
public abstract class OperatorBase implements IOperator {

    private final String symbol;
    private final String operatorName;
    private final IValueType[] inputTypes;
    private final IValueType outputType;
    private final IFunction function;
    private final IConfigRenderPattern renderPattern;

    protected OperatorBase(String symbol, String operatorName, IValueType[] inputTypes, IValueType outputType,
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
    public String getUniqueName() {
        return getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedName() {
        return getUnlocalizedPrefix() + ".name";
    }

    @Override
    public String getUnlocalizedCategoryName() {
        return getUnlocalizedCategoryPrefix() + ".name";
    }

    @Override
    public String getLocalizedNameFull() {
        return L10NHelpers.localize(getUnlocalizedCategoryPrefix() + ".basename", L10NHelpers.localize(getUnlocalizedName()));
    }

    protected String getUnlocalizedPrefix() {
        return "operator.operators." + getModId() + "." + getUnlocalizedType() + "." + getOperatorName();
    }

    protected String getUnlocalizedCategoryPrefix() {
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
        String categoryName = L10NHelpers.localize(getUnlocalizedCategoryName());
        String symbol = getSymbol();
        String outputTypeName = L10NHelpers.localize(getOutputType().getUnlocalizedName());
        lines.add(L10NHelpers.localize(L10NValues.OPERATOR_TOOLTIP_OPERATORNAME, operatorName, symbol));
        lines.add(L10NHelpers.localize(L10NValues.OPERATOR_TOOLTIP_OPERATORCATEGORY, categoryName));
        IValueType[] inputTypes = getInputTypes();
        for(int i = 0; i < inputTypes.length; i++) {
            lines.add(L10NHelpers.localize(L10NValues.OPERATOR_TOOLTIP_INPUTTYPENAME,
                    i + 1, inputTypes[i].getDisplayColorFormat() + L10NHelpers.localize(inputTypes[i].getUnlocalizedName())));
        }
        lines.add(L10NHelpers.localize(L10NValues.OPERATOR_TOOLTIP_OUTPUTTYPENAME, getOutputType().getDisplayColorFormat() + outputTypeName));
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

    @Override
    public IValue evaluate(IVariable[] input) throws EvaluationException {
        L10NHelpers.UnlocalizedString error = validateTypes(ValueHelpers.from(input));
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
            return new L10NHelpers.UnlocalizedString(L10NValues.OPERATOR_ERROR_WRONGINPUTLENGTH,
                    this.getOperatorName(), input.length, requiredInputLength);
        }
        // Input types checking
        for(int i = 0; i < requiredInputLength; i++) {
            IValueType inputType = input[i];
            if(inputType == null) {
                return new L10NHelpers.UnlocalizedString(L10NValues.OPERATOR_ERROR_NULLTYPE, this.getOperatorName(), Integer.toString(i));
            }
            if(!ValueHelpers.correspondsTo(getInputTypes()[i], inputType)) {
                return new L10NHelpers.UnlocalizedString(L10NValues.OPERATOR_ERROR_WRONGTYPE,
                        this.getOperatorName(), new L10NHelpers.UnlocalizedString(inputType.getUnlocalizedName()),
                        Integer.toString(i + 1), new L10NHelpers.UnlocalizedString(getInputTypes()[i].getUnlocalizedName()));
            }
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
