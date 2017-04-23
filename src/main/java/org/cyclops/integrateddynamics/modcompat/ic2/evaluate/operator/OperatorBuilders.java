package org.cyclops.integrateddynamics.modcompat.ic2.evaluate.operator;

import ic2.api.item.IElectricItem;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.IOperatorValuePropagator;
import org.cyclops.integrateddynamics.core.evaluate.operator.IterativeFunction;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;

/**
 * Collection of IC2 EU operator builders.
 * @author rubensworks
 */
public class OperatorBuilders {

    // --------------- Aspect builders ---------------
    public static final IterativeFunction.PrePostBuilder<Pair<IElectricItem, ItemStack>, IValue> FUNCTION_CONTAINERITEM = IterativeFunction.PrePostBuilder.begin()
            .appendPre(new IOperatorValuePropagator<OperatorBase.SafeVariablesGetter, Pair<IElectricItem, ItemStack>>() {
                @Override
                public Pair<IElectricItem, ItemStack> getOutput(OperatorBase.SafeVariablesGetter input) throws EvaluationException {
                    ValueObjectTypeItemStack.ValueItemStack a = input.getValue(0);
                    if(!a.getRawValue().isEmpty() && a.getRawValue().getItem() instanceof IElectricItem) {
                        return Pair.of((IElectricItem) a.getRawValue().getItem(), a.getRawValue());
                    }
                    return null;
                }
            });
    public static final IterativeFunction.PrePostBuilder<Pair<IElectricItem, ItemStack>, Integer> FUNCTION_CONTAINERITEM_TO_INT =
            FUNCTION_CONTAINERITEM.appendPost(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.PROPAGATOR_INTEGER_VALUE);
    public static final IterativeFunction.PrePostBuilder<Pair<IElectricItem, ItemStack>, Boolean> FUNCTION_CONTAINERITEM_TO_BOOLEAN =
            FUNCTION_CONTAINERITEM.appendPost(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.PROPAGATOR_BOOLEAN_VALUE);

}
