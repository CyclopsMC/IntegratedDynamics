package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IStringConversionRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;

import java.util.List;

/**
 * Base class for string_conversion operators.
 * @author rubensworks / LostOfThought
 */
public class StringConversionOperator<T1 extends IValueType<V1>, T2 extends IValueType<V2>, V1 extends IValue, V2 extends IValue> extends OperatorBase {

    private final T1 from;
    private final T2 to;
    private final IStringConversionRegistry.IMapping<T1, T2, V1, V2> mapping;

    public StringConversionOperator(final T1 from, final T2 to, final IStringConversionRegistry.IMapping<T1, T2, V1, V2> mapping) {
        super(
                to.getTypeName().equals("string")
                        ? from.getTypeName().substring(0, 1).toUpperCase() + from.getTypeName().substring(1) + "_as_String"
                        : "parse_" + to.getTypeName().substring(0, 1).toUpperCase() + to.getTypeName().substring(1)
                , "string_conversion" + from.getUnlocalizedName() + "$" + to.getUnlocalizedName(), constructInputVariables(1, from), to, new IFunction() {
            @Override
            public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
                IValue value = variables.getValue(0);
                if(value.getType() != from) {
                    throw new EvaluationException(String.format("The value of type %s does not correspond to the " +
                            "expected type %s to string_conversion to %s", value.getType(), from, to));
                }
                return mapping.convert((V1) value);
            }
        }, IConfigRenderPattern.PREFIX_1);
        this.from = from;
        this.to = to;
        this.mapping = mapping;
    }

    @Override
    public String getUniqueName() {
        return "operator.operators." + getModId() + ".string_conversion" + from.getUnlocalizedName() + "$" + to.getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedType() {
        return "string_conversion";
    }

    @Override
    protected String getUnlocalizedPrefix() {
        return "operator.operators." + getModId() + "." + getUnlocalizedType();
    }

    @Override
    public void loadTooltip(List<String> lines, boolean appendOptionalInfo) {
        lines.add(L10NHelpers.localize("operator.operators.integrateddynamics.string_conversion.tooltip",
                  L10NHelpers.localize(from.getUnlocalizedName()), L10NHelpers.localize(to.getUnlocalizedName()))
        );
        super.loadTooltip(lines, appendOptionalInfo);
    }

}
