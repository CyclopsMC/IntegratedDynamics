package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueCastRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;

import java.util.List;

/**
 * Base class for cast operators.
 * @author rubensworks
 */
public class CastOperator<T1 extends IValueType<V1>, T2 extends IValueType<V2>, V1 extends IValue, V2 extends IValue> extends OperatorBase {

    private final T1 from;
    private final T2 to;
    private final IValueCastRegistry.IMapping<T1, T2, V1, V2> mapping;

    public CastOperator(final T1 from, final T2 to, final IValueCastRegistry.IMapping<T1, T2, V1, V2> mapping) {
        super("()", from.getUnlocalizedName() + "$" + to.getUnlocalizedName(), constructInputVariables(1, from), to, new IFunction() {
            @Override
            public IValue evaluate(IVariable... variables) throws EvaluationException {
                IValue value = variables[0].getValue();
                if(value.getType() != from) {
                    throw new EvaluationException(String.format("The value of type %s does not correspond to the " +
                            "expected type %s to cast to %s", value.getType(), from, to));
                }
                return mapping.cast((V1) value);
            }
        }, IConfigRenderPattern.PREFIX_1);
        this.from = from;
        this.to = to;
        this.mapping = mapping;
    }

    @Override
    public String getUniqueName() {
        return "operator.operators." + getModId() + ".cast" + from.getUnlocalizedName() + "$" + to.getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedType() {
        return "cast";
    }

    @Override
    protected String getUnlocalizedPrefix() {
        return "operator.operators." + getModId() + "." + getUnlocalizedType();
    }

    @Override
    public void loadTooltip(List<String> lines, boolean appendOptionalInfo) {
        lines.add(L10NHelpers.localize("operator.operators.integrateddynamics.cast.tooltip",
                  L10NHelpers.localize(from.getUnlocalizedName()), L10NHelpers.localize(to.getUnlocalizedName()))
        );
        super.loadTooltip(lines, appendOptionalInfo);
    }

}
