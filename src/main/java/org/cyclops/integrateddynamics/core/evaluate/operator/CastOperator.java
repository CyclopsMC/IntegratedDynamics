package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueCastRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

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
        super("()", from.getTranslationKey() + "$" + to.getTranslationKey(), from.getTypeName() + "To" + Helpers.capitalizeString(to.getTypeName()), null, false, constructInputVariables(1, from), to, new IFunction() {
            @Override
            public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
                IValue value = variables.getValue(0);
                if(value.getType() != from) {
                    throw new EvaluationException(Component.translatable(
                            L10NValues.OPERATOR_ERROR_CAST_UNEXPECTED, Component.translatable(value.getType().getTranslationKey()), Component.translatable(from.getTranslationKey()), Component.translatable(to.getTranslationKey())));
                }
                return mapping.cast((V1) value);
            }
        }, IConfigRenderPattern.PREFIX_1);
        this.from = from;
        this.to = to;
        this.mapping = mapping;
    }

    @Override
    public ResourceLocation getUniqueName() {
        return new ResourceLocation(Reference.MOD_ID, "operator." + getModId() + ".cast"
                + from.getUniqueName().toString().replaceAll(":", "_") + "__"
                + to.getUniqueName().toString().replaceAll(":", "_"));
    }

    @Override
    public String getUnlocalizedType() {
        return "cast";
    }

    @Override
    protected String getUnlocalizedPrefix() {
        return "operator." + getModId() + "." + getUnlocalizedType();
    }

    @Override
    public void loadTooltip(List<Component> lines, boolean appendOptionalInfo) {
        lines.add(Component.translatable("operator.integrateddynamics.cast.tooltip",
                Component.translatable(from.getTranslationKey()),
                Component.translatable(to.getTranslationKey())));
        super.loadTooltip(lines, appendOptionalInfo);
    }

}
