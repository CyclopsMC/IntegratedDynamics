package org.cyclops.integrateddynamics.modcompat.tesla.operator;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraftforge.common.capabilities.Capability;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.operator.IterativeFunction;

/**
 * Collection of rf operator builders.
 * @author rubensworks
 */
public class OperatorBuilders {

    // --------------- Aspect builders ---------------
    public static final IterativeFunction.PrePostBuilder<ITeslaHolder, IValue> FUNCTION_HOLDER =
            org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.getItemCapability(new org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ICapabilityReference<ITeslaHolder>() {
                @Override
                public Capability<ITeslaHolder> getReference() {
                    return Capabilities.TESLA_HOLDER;
                }
            });
    public static final IterativeFunction.PrePostBuilder<ITeslaConsumer, IValue> FUNCTION_CONSUMER =
            org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.getItemCapability(new org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ICapabilityReference<ITeslaConsumer>() {
                @Override
                public Capability<ITeslaConsumer> getReference() {
                    return Capabilities.TESLA_CONSUMER;
                }
            });
    public static final IterativeFunction.PrePostBuilder<ITeslaProducer, IValue> FUNCTION_PRODUCER =
            org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.getItemCapability(new org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ICapabilityReference<ITeslaProducer>() {
                @Override
                public Capability<ITeslaProducer> getReference() {
                    return Capabilities.TESLA_PRODUCER;
                }
            });

    public static final IterativeFunction.PrePostBuilder<ITeslaHolder, Long> FUNCTION_HOLDER_TO_LONG =
            FUNCTION_HOLDER.appendPost(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.PROPAGATOR_LONG_VALUE);
    public static final IterativeFunction.PrePostBuilder<ITeslaHolder, Boolean> FUNCTION_HOLDER_TO_BOOLEAN =
            FUNCTION_HOLDER.appendPost(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.PROPAGATOR_BOOLEAN_VALUE);
    public static final IterativeFunction.PrePostBuilder<ITeslaHolder, Double> FUNCTION_HOLDER_TO_DOUBLE =
            FUNCTION_HOLDER.appendPost(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.PROPAGATOR_DOUBLE_VALUE);
    public static final IterativeFunction.PrePostBuilder<ITeslaConsumer, Boolean> FUNCTION_CONSUMER_TO_BOOLEAN =
            FUNCTION_CONSUMER.appendPost(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.PROPAGATOR_BOOLEAN_VALUE);
    public static final IterativeFunction.PrePostBuilder<ITeslaProducer, Boolean> FUNCTION_PRODUCER_TO_BOOLEAN =
            FUNCTION_PRODUCER.appendPost(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.PROPAGATOR_BOOLEAN_VALUE);

}
