package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Lists;
import lombok.ToString;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.StringUtils;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeUniquelyNamed;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeOperatorLPElement;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Value type with operator values.
 * @author rubensworks
 */
public class ValueTypeOperator extends ValueTypeBase<ValueTypeOperator.ValueOperator> implements
        IValueTypeNamed<ValueTypeOperator.ValueOperator>, IValueTypeUniquelyNamed<ValueTypeOperator.ValueOperator> {

    private static final String SIGNATURE_LINK = "->";

    public ValueTypeOperator() {
        super("operator", Helpers.RGBToInt(43, 231, 47), TextFormatting.DARK_GREEN);
    }

    @Override
    public ValueOperator getDefault() {
        return ValueOperator.of(Operators.GENERAL_IDENTITY);
    }

    @Override
    public ITextComponent toCompactString(ValueOperator value) {
        return value.getRawValue().getLocalizedNameFull();
    }

    @Override
    public INBT serialize(ValueOperator value) {
        return Operators.REGISTRY.serialize(value.getRawValue());
    }

    @Override
    public ValueOperator deserialize(INBT value) {
        IOperator operator;
        try {
            operator = Operators.REGISTRY.deserialize(value);
        } catch (EvaluationException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        if (operator != null) {
            return ValueOperator.of(operator);
        }
        throw new IllegalArgumentException(String.format("Value \"%s\" could not be parsed to an operator.", value));
    }

    @Override
    public void loadTooltip(List<ITextComponent> lines, boolean appendOptionalInfo, @Nullable ValueOperator value) {
        super.loadTooltip(lines, appendOptionalInfo, value);
        if (value != null) {
            lines.add(new TranslationTextComponent(L10NValues.VALUETYPEOPERATOR_TOOLTIP_SIGNATURE)
                    .appendSibling(getSignature(value.getRawValue())));
        }
    }

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return new ValueTypeOperatorLPElement();
    }

    @Override
    public ValueOperator materialize(ValueOperator value) throws EvaluationException {
        return ValueOperator.of(value.getRawValue().materialize());
    }

    /**
     * Pretty formatted signature of an operator.
     * @param operator The operator.
     * @return The signature.
     */
    public static ITextComponent getSignature(IOperator operator) {
        return getSignatureLines(operator, false)
                .stream()
                .reduce(new StringTextComponent(""), (a, b) -> a.appendText(" ").appendSibling(b));
    }

    /**
     * Pretty formatted signature of an operator.
     * @param inputTypes The input types.
     * @param outputType The output types.
     * @return The signature.
     */
    public static String getSignature(IValueType[] inputTypes, IValueType outputType) {
        return StringUtils.join(getSignatureLines(inputTypes, outputType, false), " ");
    }

    protected static ITextComponent switchSignatureLineContext(List<ITextComponent> lines, ITextComponent sb) {
        lines.add(sb);
        return new StringTextComponent("");
    }

    /**
     * Pretty formatted signature of an operator.
     * @param inputTypes The input types.
     * @param outputType The output types.
     * @param indent If the lines should be indented.
     * @return The signature.
     */
    public static List<ITextComponent> getSignatureLines(IValueType[] inputTypes, IValueType outputType, boolean indent) {
        List<ITextComponent> lines = Lists.newArrayList();
        ITextComponent sb = new StringTextComponent("");
        boolean first = true;
        for (IValueType inputType : inputTypes) {
            if (first) {
                first = false;
            } else {
                sb = switchSignatureLineContext(lines, sb);
                sb.appendText((indent ? "  " : "") + SIGNATURE_LINK + " ");
            }
            sb.applyTextStyle(inputType.getDisplayColorFormat())
                    .appendSibling(new TranslationTextComponent(inputType.getTranslationKey()))
                    .applyTextStyle(TextFormatting.RESET);
        }

        sb = switchSignatureLineContext(lines, sb);
        sb.appendText((indent ? "  " : "") + SIGNATURE_LINK + " ")
                .applyTextStyle(outputType.getDisplayColorFormat())
                .appendSibling(new TranslationTextComponent(outputType.getTranslationKey()))
                .applyTextStyle(TextFormatting.RESET);
        switchSignatureLineContext(lines, sb);
        return lines;
    }

    /**
     * Pretty formatted signature of an operator.
     * @param operator The operator.
     * @param indent If the lines should be indented.
     * @return The signature.
     */
    public static List<ITextComponent> getSignatureLines(IOperator operator, boolean indent) {
        return getSignatureLines(operator.getInputTypes(), operator.getOutputType(), indent);
    }

    @Override
    public String getName(ValueTypeOperator.ValueOperator a) {
        return a.getRawValue().getLocalizedNameFull().getString();
    }

    @Override
    public String getUniqueName(ValueOperator a) {
        return a.getRawValue().getUniqueName().toString();
    }

    @ToString
    public static class ValueOperator extends ValueBase {

        private final IOperator value;

        private ValueOperator(IOperator value) {
            super(ValueTypes.OPERATOR);
            this.value = value;
        }

        public static ValueOperator of(IOperator value) {
            return new ValueOperator(value);
        }

        public IOperator getRawValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            return o == this || (o instanceof ValueOperator && value.equals(((ValueOperator) o).value));
        }

        @Override
        public int hashCode() {
            return 37 + value.hashCode();
        }
    }

}
