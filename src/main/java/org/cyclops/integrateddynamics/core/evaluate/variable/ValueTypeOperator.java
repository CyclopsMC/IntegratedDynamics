package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Lists;
import lombok.ToString;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Value type with operator values.
 * @author rubensworks
 */
public class ValueTypeOperator extends ValueTypeBase<ValueTypeOperator.ValueOperator> {

    private static final String SIGNATURE_LINK = "->";

    public ValueTypeOperator() {
        super("operator", Helpers.RGBToInt(43, 231, 47), TextFormatting.DARK_GREEN.toString());
    }

    @Override
    public ValueOperator getDefault() {
        return ValueOperator.of(Operators.GENERAL_IDENTITY);
    }

    @Override
    public String toCompactString(ValueOperator value) {
        return value.getRawValue().getLocalizedNameFull();
    }

    @Override
    public String serialize(ValueOperator value) {
        return value.getRawValue().getUniqueName();
    }

    @Override
    public ValueOperator deserialize(String value) {
        IOperator operator = Operators.REGISTRY.getOperator(value);
        if (operator != null) {
            return ValueOperator.of(operator);
        }
        throw new IllegalArgumentException(String.format("Value \"%s\" could not be parsed to an operator.", value));
    }

    @Override
    public void loadTooltip(List<String> lines, boolean appendOptionalInfo, @Nullable ValueOperator value) {
        super.loadTooltip(lines, appendOptionalInfo, value);
        if (value != null) {
            lines.add(L10NHelpers.localize(L10NValues.VALUETYPEOPERATOR_TOOLTIP_SIGNATURE, getSignature(value.getRawValue())));
        }
    }

    @Override
    public boolean hasDefaultLogicProgrammerElement() {
        return false;
    }

    /**
     * Pretty formatted signature of an operator.
     * @param operator The operator.
     * @return The signature.
     */
    public static String getSignature(IOperator operator) {
        return StringUtils.join(getSignatureLines(operator, false), " ");
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

    protected static StringBuilder switchSignatureLineContext(List<String> lines, StringBuilder sb) {
        lines.add(sb.toString());
        return new StringBuilder();
    }

    /**
     * Pretty formatted signature of an operator.
     * @param inputTypes The input types.
     * @param outputType The output types.
     * @param indent If the lines should be indented.
     * @return The signature.
     */
    public static List<String> getSignatureLines(IValueType[] inputTypes, IValueType outputType, boolean indent) {
        List<String> lines = Lists.newArrayList();
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        boolean first = true;
        for (IValueType inputType : inputTypes) {
            if (first) {
                first = false;
            } else {
                sb = switchSignatureLineContext(lines, sb);
                sb.append((indent ? "  " : "") + SIGNATURE_LINK + " ");
            }
            sb.append(inputType.getDisplayColorFormat())
                    .append(L10NHelpers.localize(inputType.getUnlocalizedName()))
                    .append(TextFormatting.RESET);
        }
        sb.append(")");

        sb = switchSignatureLineContext(lines, sb);
        sb.append(SIGNATURE_LINK + " ")
                .append(outputType.getDisplayColorFormat())
                .append(L10NHelpers.localize(outputType.getUnlocalizedName()))
                .append(TextFormatting.RESET);
        switchSignatureLineContext(lines, sb);
        return lines;
    }

    /**
     * Pretty formatted signature of an operator.
     * @param operator The operator.
     * @param indent If the lines should be indented.
     * @return The signature.
     */
    public static List<String> getSignatureLines(IOperator operator, boolean indent) {
        return getSignatureLines(operator.getInputTypes(), operator.getOutputType(), indent);
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
            return o instanceof ValueOperator && ((ValueOperator) o).value == this.value;
        }
    }

}
