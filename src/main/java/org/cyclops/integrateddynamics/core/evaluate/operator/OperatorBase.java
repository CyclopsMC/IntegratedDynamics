package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import javax.annotation.Nullable;
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
    @Nullable
    private final IConfigRenderPattern renderPattern;

    private String translationKey = null;
    private int recursiveInvocations;

    protected OperatorBase(String symbol, String operatorName, IValueType[] inputTypes,
                           IValueType outputType, IFunction function, @Nullable IConfigRenderPattern renderPattern) {
        this.symbol = symbol;
        this.operatorName = operatorName;
        this.inputTypes = inputTypes;
        this.outputType = outputType;
        this.function = function;
        this.renderPattern = renderPattern;
        if(renderPattern != null && renderPattern.getSlotPositions().length != inputTypes.length) {
            throw new IllegalArgumentException(String.format("The given config render pattern with %s slots is not " +
                    "compatible with the number of input types %s for %s",
                    renderPattern.getSlotPositions().length, inputTypes.length, symbol));
        }
    }

    public static IValueType[] constructInputVariables(int length, IValueType defaultType) {
        IValueType[] values = new IValueType[length];
        Arrays.fill(values, defaultType);
        return values;
    }

    protected abstract String getUnlocalizedType();

    protected IFunction getFunction() {
        return this.function;
    }

    @Override
    public ResourceLocation getUniqueName() {
        return new ResourceLocation(getModId(), this.getUnlocalizedType().replaceAll("\\.", "_") + "_" + getOperatorName());
    }

    @Override
    public String getTranslationKey() {
        return translationKey != null ? translationKey : (translationKey = getUnlocalizedPrefix());
    }

    @Override
    public String getUnlocalizedCategoryName() {
        return getUnlocalizedCategoryPrefix();
    }

    @Override
    public MutableComponent getLocalizedNameFull() {
        return Component.translatable(getUnlocalizedCategoryPrefix() + ".basename", Component.translatable(getTranslationKey()));
    }

    protected String getUnlocalizedPrefix() {
        return "operator." + getModId() + "." + getUnlocalizedType() + "." + getOperatorName();
    }

    protected String getUnlocalizedCategoryPrefix() {
        return "operator." + getModId() + "." + getUnlocalizedType();
    }

    protected String getOperatorName() {
        return this.operatorName;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public void loadTooltip(List<Component> lines, boolean appendOptionalInfo) {
        Component operatorName = Component.translatable(getTranslationKey());
        Component categoryName = Component.translatable(getUnlocalizedCategoryName());
        String symbol = getSymbol();
        String outputTypeName = L10NHelpers.localize(getOutputType().getTranslationKey());
        lines.add(Component.translatable(L10NValues.OPERATOR_TOOLTIP_OPERATORNAME, operatorName, symbol));
        lines.add(Component.translatable(L10NValues.OPERATOR_TOOLTIP_OPERATORCATEGORY, categoryName));
        IValueType[] inputTypes = getInputTypes();
        for(int i = 0; i < inputTypes.length; i++) {
            lines.add(Component.translatable(L10NValues.OPERATOR_TOOLTIP_INPUTTYPENAME, i + 1)
            .append(Component.translatable(inputTypes[i].getTranslationKey()))
                    .withStyle(inputTypes[i].getDisplayColorFormat()));
        }
        lines.add(Component.translatable(L10NValues.OPERATOR_TOOLTIP_OUTPUTTYPENAME, getOutputType().getDisplayColorFormat() + outputTypeName));
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
    public IValue evaluate(IVariable... input) throws EvaluationException {
        if (this.recursiveInvocations++ > GeneralConfig.operatorRecursionLimit) {
            this.recursiveInvocations = 0;
            throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_RECURSIONLIMIT,
                    GeneralConfig.operatorRecursionLimit,
                    Component.translatable(this.getTranslationKey())
            ));
        }
        MutableComponent error = validateTypes(ValueHelpers.from(input));
        if(error != null) {
            this.recursiveInvocations--;
            throw new EvaluationException(error);
        }
        IValue res = function.evaluate(new SafeVariablesGetter(input));
        this.recursiveInvocations--;
        return res;
    }

    @Override
    public int getRequiredInputLength() {
        return getInputTypes().length;
    }

    @Override
    public MutableComponent validateTypes(IValueType[] input) {
        // Input size checking
        int requiredInputLength = getRequiredInputLength();
        if(input.length != requiredInputLength) {
            return Component.translatable(L10NValues.OPERATOR_ERROR_WRONGINPUTLENGTH,
                    this.getOperatorName(), input.length, requiredInputLength);
        }
        // Input types checking
        for(int i = 0; i < requiredInputLength; i++) {
            IValueType inputType = input[i];
            if(inputType == null) {
                return Component.translatable(L10NValues.OPERATOR_ERROR_NULLTYPE, this.getOperatorName(), Integer.toString(i));
            }
            if(!ValueHelpers.correspondsTo(getInputTypes()[i], inputType)) {
                return Component.translatable(L10NValues.OPERATOR_ERROR_WRONGTYPE,
                        this.getOperatorName(), Component.translatable(inputType.getTranslationKey()),
                        Integer.toString(i + 1), Component.translatable(getInputTypes()[i].getTranslationKey()));
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
    @Nullable
    public IConfigRenderPattern getRenderPattern() {
        return renderPattern;
    }

    @Override
    public IOperator materialize() throws EvaluationException {
        return this;
    }

    public static class SafeVariablesGetter {

        private final IVariable[] variables;

        public SafeVariablesGetter(IVariable... variables) {
            this.variables = variables;
        }

        public IValue getValue(int i) throws EvaluationException {
            return variables[i].getValue();
        }

        public <V extends IValue> V getValue(int i, IValueType<V> valueType) throws EvaluationException {
            return valueType.cast(getValue(i));
        }

        public IVariable[] getVariables() {
            return this.variables;
        }

        public static class Shifted extends SafeVariablesGetter {

            public Shifted(int start, IVariable... variables) {
                super(Arrays.copyOfRange(variables, start, variables.length));
            }
        }
    }

    public static interface IFunction {

        /**
         * Evaluate this function for the given input.
         * @param variables The input variables holder.
         * @return The output value.
         * @throws EvaluationException If an exception occurs while evaluating
         */
        public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException;

    }

}
