package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueParseRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.List;

/**
 * Base class for parse operators.
 * @author rubensworks/lostofthought
 */
public class ParseOperator<T2 extends IValueType<V2>, V2 extends IValue> extends OperatorBase {

    private final T2 to;
    private final IValueParseRegistry.IMapping<T2, V2> mapping;

    public ParseOperator(final T2 to, final IValueParseRegistry.IMapping<T2, V2> mapping) {
        // Also: under_scores, rantogether, or I.amNBT?
        super("parse_" + L10NHelpers.localize(to.getUnlocalizedName())
                , "parse_" + to.getUnlocalizedName()
                , constructInputVariables(1, ValueTypes.STRING)
                , to
                , new IFunction() {
            @Override
            public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
                IValue value = variables.getValue(0);
                return mapping.parse((ValueTypeString.ValueString) value);
            }
        }, IConfigRenderPattern.PREFIX_1);
        this.to = to;
        this.mapping = mapping;
    }

    @Override
    public String getUniqueName() {
        return "operator.operators." + getModId() + ".parse." + to.getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedType() {
        return "parse";
    }

    @Override
    protected String getUnlocalizedPrefix() {
        return "operator.operators." + getModId() + "." + getUnlocalizedType();
    }

    @Override
    public void loadTooltip(List<String> lines, boolean appendOptionalInfo) {
        lines.add(L10NHelpers.localize("operator.operators.integrateddynamics.parse.tooltip"
                , L10NHelpers.localize(ValueTypes.STRING.getUnlocalizedName())
                , L10NHelpers.localize(to.getUnlocalizedName()))
        );
        super.loadTooltip(lines, appendOptionalInfo);
    }

}
