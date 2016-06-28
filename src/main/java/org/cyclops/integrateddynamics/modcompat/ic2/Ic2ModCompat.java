package org.cyclops.integrateddynamics.modcompat.ic2;

import com.google.common.collect.Sets;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.evaluate.IOperatorValuePropagator;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.modcompat.ic2.aspect.Ic2Aspects;
import org.cyclops.integrateddynamics.modcompat.ic2.evaluate.operator.OperatorBuilders;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * Compatibility plugin for Forestry.
 * @author rubensworks
 *
 */
public class Ic2ModCompat implements IModCompat {

    @Override
    public String getModID() {
       return Reference.MOD_IC2;
    }

    @Override
    public void onInit(Step step) {
    	if(step == Step.PREINIT) {
			// Aspects
			Aspects.REGISTRY.register(PartTypes.MACHINE_READER, Sets.<IAspect>newHashSet(
					Ic2Aspects.Read.Energy.BOOLEAN_ISAPPLICABLE,
					Ic2Aspects.Read.Energy.BOOLEAN_CANEXTRACT,
					Ic2Aspects.Read.Energy.BOOLEAN_CANINSERT,
					Ic2Aspects.Read.Energy.BOOLEAN_ISFULL,
					Ic2Aspects.Read.Energy.BOOLEAN_ISEMPTY,
					Ic2Aspects.Read.Energy.BOOLEAN_ISNONEMPTY,
					Ic2Aspects.Read.Energy.INTEGER_STORED,
					Ic2Aspects.Read.Energy.INTEGER_CAPACITY,
					Ic2Aspects.Read.Energy.DOUBLE_FILLRATIO
			));

			// Operators
			/* Check if the item is an RF container item */
			Operators.REGISTRY.register(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
					.output(ValueTypes.BOOLEAN).symbolOperator("isic2eucontainer")
					.function(OperatorBuilders.FUNCTION_CONTAINERITEM_TO_BOOLEAN.build(new IOperatorValuePropagator<Pair<IElectricItem, ItemStack>, Boolean>() {
						@Override
						public Boolean getOutput(Pair<IElectricItem, ItemStack> input) throws EvaluationException {
							return input != null;
						}
					})).build());

			/* Get the storage energy */
			Operators.REGISTRY.register(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
					.output(ValueTypes.INTEGER).symbolOperator("storedic2eu")
					.function(OperatorBuilders.FUNCTION_CONTAINERITEM_TO_INT.build(new IOperatorValuePropagator<Pair<IElectricItem, ItemStack>, Integer>() {
						@Override
						public Integer getOutput(Pair<IElectricItem, ItemStack> input) throws EvaluationException {
							return input != null ? (int) ElectricItem.manager.getCharge(input.getRight()) : 0;
						}
					})).build());
			/* Get the energy capacity */
			Operators.REGISTRY.register(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
					.output(ValueTypes.INTEGER).symbolOperator("ic2eucapacity")
					.function(OperatorBuilders.FUNCTION_CONTAINERITEM_TO_INT.build(new IOperatorValuePropagator<Pair<IElectricItem, ItemStack>, Integer>() {
						@Override
						public Integer getOutput(Pair<IElectricItem, ItemStack> input) throws EvaluationException {
							return input != null ? (int) ElectricItem.manager.getMaxCharge(input.getRight()) : 0;
						}
					})).build());
    	}
    }
    
    @Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getComment() {
		return "IC2 EU reader aspects.";
	}

}
