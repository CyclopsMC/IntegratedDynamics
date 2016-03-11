package org.cyclops.integrateddynamics.modcompat.rf.evaluate.operator;

import cofh.api.energy.IEnergyContainerItem;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.IOperatorValuePropagator;
import org.cyclops.integrateddynamics.core.evaluate.operator.IterativeFunction;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;

/**
 * Collection of rf operator builders.
 * @author rubensworks
 */
public class OperatorBuilders {

    // --------------- Aspect builders ---------------
    public static final IterativeFunction.PrePostBuilder<Pair<IEnergyContainerItem, ItemStack>, IValue> FUNCTION_CONTAINERITEM = IterativeFunction.PrePostBuilder.begin()
            .appendPre(new IOperatorValuePropagator<OperatorBase.SafeVariablesGetter, Pair<IEnergyContainerItem, ItemStack>>() {
                @Override
                public Pair<IEnergyContainerItem, ItemStack> getOutput(OperatorBase.SafeVariablesGetter input) throws EvaluationException {
                    ValueObjectTypeItemStack.ValueItemStack a = input.getValue(0);
                    if(a.getRawValue().isPresent() && a.getRawValue().get().getItem() instanceof IEnergyContainerItem) {
                        return Pair.of((IEnergyContainerItem) a.getRawValue().get().getItem(), a.getRawValue().get());
                    }
                    return null;
                }
            });
    public static final IterativeFunction.PrePostBuilder<Pair<IEnergyContainerItem, ItemStack>, Integer> FUNCTION_CONTAINERITEM_TO_INT =
            FUNCTION_CONTAINERITEM.appendPost(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.PROPAGATOR_INTEGER_VALUE);
    public static final IterativeFunction.PrePostBuilder<Pair<IEnergyContainerItem, ItemStack>, Boolean> FUNCTION_CONTAINERITEM_TO_BOOLEAN =
            FUNCTION_CONTAINERITEM.appendPost(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.PROPAGATOR_BOOLEAN_VALUE);

}
