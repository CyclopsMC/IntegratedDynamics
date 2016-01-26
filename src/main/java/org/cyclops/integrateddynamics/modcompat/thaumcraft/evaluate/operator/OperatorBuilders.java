package org.cyclops.integrateddynamics.modcompat.thaumcraft.evaluate.operator;

import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.IOperatorValuePropagator;
import org.cyclops.integrateddynamics.core.evaluate.build.OperatorBuilder;
import org.cyclops.integrateddynamics.core.evaluate.operator.IterativeFunction;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorBase;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.ThaumcraftModCompat;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.evaluate.variable.ValueObjectTypeAspect;
import thaumcraft.api.aspects.Aspect;

/**
 * Collection of thaumcraft operator builders.
 * @author rubensworks
 */
public class OperatorBuilders {

    // --------------- Aspect builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ASPECT = OperatorBuilder.forType(ThaumcraftModCompat.OBJECT_ASPECT).appendKind("thaumcraft").appendKind("aspect");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ASPECT_1_SUFFIX_LONG = ASPECT.inputTypes(1, ThaumcraftModCompat.OBJECT_ASPECT).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ASPECT_2 = ASPECT.inputTypes(2, ThaumcraftModCompat.OBJECT_ASPECT).renderPattern(IConfigRenderPattern.INFIX);
    public static final IterativeFunction.PrePostBuilder<Pair<Aspect, Integer>, IValue> FUNCTION_ASPECT = IterativeFunction.PrePostBuilder.begin()
            .appendPre(new IOperatorValuePropagator<OperatorBase.SafeVariablesGetter, Pair<Aspect, Integer>>() {
                @Override
                public Pair<Aspect, Integer> getOutput(OperatorBase.SafeVariablesGetter input) throws EvaluationException {
                    ValueObjectTypeAspect.ValueAspect a = input.getValue(0);
                    return a.getRawValue().isPresent() ? a.getRawValue().get() : null;
                }
            });
    public static final IterativeFunction.PrePostBuilder<Pair<Aspect, Integer>, Integer> FUNCTION_ASPECT_TO_INT =
            FUNCTION_ASPECT.appendPost(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.PROPAGATOR_INTEGER_VALUE);
    public static final IterativeFunction.PrePostBuilder<Pair<Aspect, Integer>, Boolean> FUNCTION_ASPECT_TO_BOOLEAN =
            FUNCTION_ASPECT.appendPost(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.PROPAGATOR_BOOLEAN_VALUE);

}
