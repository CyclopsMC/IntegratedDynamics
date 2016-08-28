package org.cyclops.integrateddynamics.modcompat.tesla;

import com.google.common.collect.Sets;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.evaluate.IOperatorValuePropagator;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.modcompat.tesla.aspect.TeslaAspects;
import org.cyclops.integrateddynamics.modcompat.tesla.operator.OperatorBuilders;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * Mod compat for the Tesla API.
 * @author rubensworks
 *
 */
public class TeslaApiCompat implements IModCompat {

	@Override
	public void onInit(final Step initStep) {
		if(initStep == Step.PREINIT) {
			// Aspects
			Aspects.REGISTRY.register(PartTypes.MACHINE_READER, Sets.<IAspect>newHashSet(
					TeslaAspects.Read.Energy.BOOLEAN_ISAPPLICABLE,
					TeslaAspects.Read.Energy.BOOLEAN_ISRECEIVER,
					TeslaAspects.Read.Energy.BOOLEAN_ISPROVIDER,
					TeslaAspects.Read.Energy.BOOLEAN_CANEXTRACT,
					TeslaAspects.Read.Energy.BOOLEAN_CANINSERT,
					TeslaAspects.Read.Energy.BOOLEAN_ISFULL,
					TeslaAspects.Read.Energy.BOOLEAN_ISEMPTY,
					TeslaAspects.Read.Energy.BOOLEAN_ISNONEMPTY,
					TeslaAspects.Read.Energy.LONG_STORED,
					TeslaAspects.Read.Energy.LONG_CAPACITY,
					TeslaAspects.Read.Energy.DOUBLE_FILLRATIO,
					TeslaAspects.Read.Energy.STRING_STORED,
					TeslaAspects.Read.Energy.STRING_CAPACITY
			));

			// Operators
			/* Check if the item is a Tesla container item */
			Operators.REGISTRY.register(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
					.output(ValueTypes.BOOLEAN).symbolOperator("isteslacontainer")
					.function(OperatorBuilders.FUNCTION_HOLDER_TO_BOOLEAN.build(new IOperatorValuePropagator<ITeslaHolder, Boolean>() {
						@Override
						public Boolean getOutput(ITeslaHolder input) throws EvaluationException {
							return input != null;
						}
					})).build());

			/* Get the storage energy */
			Operators.REGISTRY.register(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
					.output(ValueTypes.LONG).symbolOperator("teslastored")
					.function(OperatorBuilders.FUNCTION_HOLDER_TO_LONG.build(new IOperatorValuePropagator<ITeslaHolder, Long>() {
						@Override
						public Long getOutput(ITeslaHolder input) throws EvaluationException {
							return input != null ? input.getStoredPower() : 0;
						}
					})).build());
			/* Get the energy capacity */
			Operators.REGISTRY.register(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
					.output(ValueTypes.LONG).symbolOperator("teslacapacity")
					.function(OperatorBuilders.FUNCTION_HOLDER_TO_LONG.build(new IOperatorValuePropagator<ITeslaHolder, Long>() {
						@Override
						public Long getOutput(ITeslaHolder input) throws EvaluationException {
							return input != null ? input.getCapacity() : 0;
						}
					})).build());
			/* If the item is an energy receiver */
			Operators.REGISTRY.register(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
					.output(ValueTypes.BOOLEAN).symbolOperator("isteslareceiver")
					.function(OperatorBuilders.FUNCTION_CONSUMER_TO_BOOLEAN.build(new IOperatorValuePropagator<ITeslaConsumer, Boolean>() {
						@Override
						public Boolean getOutput(ITeslaConsumer input) throws EvaluationException {
							return input != null;
						}
					})).build());
			/* If the item is an energy provider */
			Operators.REGISTRY.register(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
					.output(ValueTypes.BOOLEAN).symbolOperator("isteslaprovider")
					.function(OperatorBuilders.FUNCTION_PRODUCER_TO_BOOLEAN.build(new IOperatorValuePropagator<ITeslaProducer, Boolean>() {
						@Override
						public Boolean getOutput(ITeslaProducer input) throws EvaluationException {
							return input != null;
						}
					})).build());
			/* If the item is full */
			Operators.REGISTRY.register(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
					.output(ValueTypes.BOOLEAN).symbolOperator("isteslafull")
					.function(OperatorBuilders.FUNCTION_HOLDER_TO_BOOLEAN.build(new IOperatorValuePropagator<ITeslaHolder, Boolean>() {
						@Override
						public Boolean getOutput(ITeslaHolder input) throws EvaluationException {
							return input != null && input.getStoredPower() == input.getCapacity();
						}
					})).build());
			/* If the item is empty */
			Operators.REGISTRY.register(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
					.output(ValueTypes.BOOLEAN).symbolOperator("isteslaempty")
					.function(OperatorBuilders.FUNCTION_HOLDER_TO_BOOLEAN.build(new IOperatorValuePropagator<ITeslaHolder, Boolean>() {
						@Override
						public Boolean getOutput(ITeslaHolder input) throws EvaluationException {
							return input != null && input.getStoredPower() == 0;
						}
					})).build());
			/* If the item is not empty */
			Operators.REGISTRY.register(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
					.output(ValueTypes.BOOLEAN).symbolOperator("isteslanonempty")
					.function(OperatorBuilders.FUNCTION_HOLDER_TO_BOOLEAN.build(new IOperatorValuePropagator<ITeslaHolder, Boolean>() {
						@Override
						public Boolean getOutput(ITeslaHolder input) throws EvaluationException {
							return input != null && input.getStoredPower() != 0;
						}
					})).build());
			/* The fill ratio */
			Operators.REGISTRY.register(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
					.output(ValueTypes.DOUBLE).symbolOperator("teslafillratio")
					.function(OperatorBuilders.FUNCTION_HOLDER_TO_DOUBLE.build(new IOperatorValuePropagator<ITeslaHolder, Double>() {
						@Override
						public Double getOutput(ITeslaHolder input) throws EvaluationException {
							return input != null ? (((double) input.getStoredPower()) / input.getCapacity()) : 0.0D;
						}
					})).build());
		}
	}

	@Override
	public String getModID() {
		return Reference.MOD_TESLA;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getComment() {
		return "Tesla readers aspects and operators.";
	}

}
