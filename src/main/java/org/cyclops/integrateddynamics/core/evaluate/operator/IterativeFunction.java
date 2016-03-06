package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.IOperatorValuePropagator;
import org.cyclops.integrateddynamics.core.helper.Helpers;

import java.util.Collections;
import java.util.List;

/**
 * A smart function that is made up of a list of value propagators.
 * @author rubensworks
 */
public class IterativeFunction implements OperatorBase.IFunction {

    private final List<IOperatorValuePropagator<?, ?>> valuePropagators;

    public IterativeFunction(List<IOperatorValuePropagator<?, ?>> valuePropagators) {
        this.valuePropagators = valuePropagators;
    }

    @Override
    public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
        Object output = variables;
        for (IOperatorValuePropagator valuePropagator : valuePropagators) {
            output = valuePropagator.getOutput(output);
        }
        return (IValue) output;
    }

    /**
     * A builder for iterative smart functions.
     * The idea is that this builder will buildReader two lists, one for the value propagators before
     * the core propagator, and one for the value propagators after the core propagators.
     * This is so that the pre- and post- list can be shared and only the core propagators
     * is variable.
     * @param <O> The current output of the pre-propagator list.
     * @param <P> The current output of the post-propagator list.
     */
    public static class PrePostBuilder<O, P> {

        private final List<IOperatorValuePropagator<?, ?>> preValuePropagators;
        private final List<IOperatorValuePropagator<?, ?>> postValuePropagators;

        private PrePostBuilder(List<IOperatorValuePropagator<?, ?>> preValuePropagators, List<IOperatorValuePropagator<?, ?>> postValuePropagators) {
            this.preValuePropagators = preValuePropagators;
            this.postValuePropagators = postValuePropagators;
        }

        /**
         * @return The builder instance.
         */
        public static PrePostBuilder<OperatorBase.SafeVariablesGetter, IValue> begin() {
            return new PrePostBuilder<>(Collections.<IOperatorValuePropagator<?, ?>>emptyList(), Collections.<IOperatorValuePropagator<?, ?>>emptyList());
        }

        /**
         * Add a new pre-propagator
         * @param valuePropagator The pre-propagator
         * @param <O2> The new output of the pre-propagator list.
         * @return The builder instance.
         */
        public <O2> PrePostBuilder<O2, P> appendPre(IOperatorValuePropagator<O, O2> valuePropagator) {
            return new PrePostBuilder<>(Helpers.joinList(preValuePropagators, valuePropagator), Lists.newArrayList(postValuePropagators));
        }

        /**
         * Add a new post-propagator
         * @param valuePropagator The post-propagator
         * @param <P2> The new output of the post-propagator list.
         * @return The builder instance.
         */
        public <P2> PrePostBuilder<O, P2> appendPost(IOperatorValuePropagator<P2, P> valuePropagator) {
            return new PrePostBuilder<>(Lists.newArrayList(preValuePropagators), Helpers.joinList(postValuePropagators, valuePropagator));
        }

        /**
         * Finalize the function with the given core propagator.
         * @param valuePropagator The core propagator that will be connected to the pre- and post-list.
         * @return The built function.
         */
        public IterativeFunction build(IOperatorValuePropagator<O, P> valuePropagator) {
            List<IOperatorValuePropagator<?, ?>> valuePropagators = Lists.newArrayListWithExpectedSize(preValuePropagators.size() + postValuePropagators.size());
            valuePropagators.addAll(preValuePropagators);
            valuePropagators.add(valuePropagator);
            valuePropagators.addAll(postValuePropagators);
            return new IterativeFunction(valuePropagators);
        }

    }

}
