package org.cyclops.integrateddynamics.modcompat.rf;

import cofh.api.energy.IEnergyContainerItem;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.energy.IEnergyStorage;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.modcompat.IApiCompat;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.evaluate.IOperatorValuePropagator;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.EnergyHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.modcompat.rf.aspect.RfAspects;
import org.cyclops.integrateddynamics.modcompat.rf.evaluate.operator.OperatorBuilders;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import javax.annotation.Nullable;

/**
 * Mod compat for the RF API.
 * TODO: remove in next major MC update.
 * @author rubensworks
 *
 */
public class RfApiCompat implements IApiCompat {

	@Override
	public void onInit(final Step initStep) {
		if(initStep == Step.PREINIT) {
			// Aspects
			Aspects.REGISTRY.register(PartTypes.MACHINE_READER, Lists.<IAspect>newArrayList(
					RfAspects.Read.Energy.BOOLEAN_ISAPPLICABLE,
					RfAspects.Read.Energy.BOOLEAN_ISRECEIVER,
					RfAspects.Read.Energy.BOOLEAN_ISPROVIDER,
					RfAspects.Read.Energy.BOOLEAN_CANEXTRACT,
					RfAspects.Read.Energy.BOOLEAN_CANINSERT,
					RfAspects.Read.Energy.BOOLEAN_ISFULL,
					RfAspects.Read.Energy.BOOLEAN_ISEMPTY,
					RfAspects.Read.Energy.BOOLEAN_ISNONEMPTY,
					RfAspects.Read.Energy.INTEGER_STORED,
					RfAspects.Read.Energy.INTEGER_CAPACITY,
					RfAspects.Read.Energy.DOUBLE_FILLRATIO
			));

			// Operators
			/* Check if the item is an RF container item */
			Operators.REGISTRY.register(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
					.output(ValueTypes.BOOLEAN).symbolOperator("isrfcontainer")
					.function(OperatorBuilders.FUNCTION_CONTAINERITEM_TO_BOOLEAN.build(new IOperatorValuePropagator<Pair<IEnergyContainerItem, ItemStack>, Boolean>() {
						@Override
						public Boolean getOutput(Pair<IEnergyContainerItem, ItemStack> input) throws EvaluationException {
							return input != null;
						}
					})).build());

			/* Get the storage energy */
			Operators.REGISTRY.register(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
					.output(ValueTypes.INTEGER).symbolOperator("storedrf")
					.function(OperatorBuilders.FUNCTION_CONTAINERITEM_TO_INT.build(new IOperatorValuePropagator<Pair<IEnergyContainerItem, ItemStack>, Integer>() {
						@Override
						public Integer getOutput(Pair<IEnergyContainerItem, ItemStack> input) throws EvaluationException {
							return input != null ? input.getLeft().getEnergyStored(input.getRight()) : 0;
						}
					})).build());
			/* Get the energy capacity */
			Operators.REGISTRY.register(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
					.output(ValueTypes.INTEGER).symbolOperator("rfcapacity")
					.function(OperatorBuilders.FUNCTION_CONTAINERITEM_TO_INT.build(new IOperatorValuePropagator<Pair<IEnergyContainerItem, ItemStack>, Integer>() {
						@Override
						public Integer getOutput(Pair<IEnergyContainerItem, ItemStack> input) throws EvaluationException {
							return input != null ? input.getLeft().getMaxEnergyStored(input.getRight()) : 0;
						}
					})).build());

			EnergyHelpers.addEnergyStorageProxy(new EnergyHelpers.IEnergyStorageProxy() {
				@Nullable
				@Override
				public IEnergyStorage getEnergyStorageProxy(IBlockAccess world, BlockPos pos, EnumFacing facing) {
					return new EnergyStorageRf(world, pos, facing);
				}
			});
		}
	}

	@Override
	public String getApiID() {
		return Reference.MOD_RF_API;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public String getComment() {
		return "RF readers aspects and operators.";
	}

}
