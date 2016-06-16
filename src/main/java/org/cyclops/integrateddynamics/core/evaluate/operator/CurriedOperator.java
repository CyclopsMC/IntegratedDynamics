package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.Arrays;
import java.util.List;

/**
 * An operator that is partially being applied.
 * @author rubensworks
 */
public class CurriedOperator implements IOperator {

    private final IOperator baseOperator;
    private final IVariable appliedVariable;

    public CurriedOperator(IOperator baseOperator, IVariable appliedVariable) {
        this.baseOperator = baseOperator;
        this.appliedVariable = appliedVariable;
    }

    protected String getAppliedSymbol() {
        return appliedVariable.getType().getTypeName();
    }

    @Override
    public String getSymbol() {
        StringBuilder sb = new StringBuilder();
        sb.append(baseOperator.getSymbol());
        sb.append(" [");
        sb.append(getAppliedSymbol());
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String getUniqueName() {
        StringBuilder sb = new StringBuilder();
        sb.append(baseOperator.getUniqueName());
        sb.append("[");
        sb.append(getAppliedSymbol());
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String getUnlocalizedName() {
        return baseOperator.getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedCategoryName() {
        return baseOperator.getUnlocalizedCategoryName();
    }

    @Override
    public String getLocalizedNameFull() {
        return L10NHelpers.localize(L10NValues.OPERATOR_APPLIED_OPERATORNAME,
                baseOperator.getLocalizedNameFull(), getAppliedSymbol());
    }

    @Override
    public void loadTooltip(List<String> lines, boolean appendOptionalInfo) {
        baseOperator.loadTooltip(lines, appendOptionalInfo);
        lines.add(L10NHelpers.localize(L10NValues.OPERATOR_APPLIED_TYPE, getAppliedSymbol()));
    }

    @Override
    public IValueType[] getInputTypes() {
        IValueType[] baseInputTypes = baseOperator.getInputTypes();
        return Arrays.copyOfRange(baseInputTypes, 1, baseInputTypes.length);
    }

    @Override
    public IValueType getOutputType() {
        if (baseOperator.getRequiredInputLength() == 1) {
            return baseOperator.getOutputType();
        } else {
            return ValueTypes.OPERATOR;
        }
    }

    protected IVariable[] deriveFullInputVariables(IVariable[] partialInput) {
        IVariable[] fullInput = new IVariable[Math.min(baseOperator.getRequiredInputLength(), partialInput.length + 1)];
        fullInput[0] = appliedVariable;
        System.arraycopy(partialInput, 0, fullInput, 1, fullInput.length - 1);
        return fullInput;
    }

    protected IValueType[] deriveFullInputTypes(IValueType[] partialInput) {
        IValueType[] fullInput = new IValueType[Math.min(baseOperator.getRequiredInputLength(), partialInput.length + 1)];
        fullInput[0] = appliedVariable.getType();
        System.arraycopy(partialInput, 0, fullInput, 1, fullInput.length - 1);
        return fullInput;
    }

    @Override
    public IValueType getConditionalOutputType(IVariable[] input) {
        return baseOperator.getConditionalOutputType(deriveFullInputVariables(input));
    }

    @Override
    public IValue evaluate(IVariable[] input) throws EvaluationException {
        return baseOperator.evaluate(deriveFullInputVariables(input));
    }

    @Override
    public int getRequiredInputLength() {
        return baseOperator.getRequiredInputLength() - 1;
    }

    @Override
    public L10NHelpers.UnlocalizedString validateTypes(IValueType[] input) {
        return baseOperator.validateTypes(deriveFullInputTypes(input));
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return IConfigRenderPattern.NONE;
    }
}
