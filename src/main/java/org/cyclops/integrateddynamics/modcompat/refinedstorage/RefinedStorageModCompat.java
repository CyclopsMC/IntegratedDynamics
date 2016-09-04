package org.cyclops.integrateddynamics.modcompat.refinedstorage;

import com.google.common.collect.Sets;
import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyFactories;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyNBTFactory;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.modcompat.refinedstorage.aspect.RefinedStorageAspects;
import org.cyclops.integrateddynamics.modcompat.refinedstorage.aspect.ValueTypeListProxyPositionedNetworkMasterInventory;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * Mod compat for the Refined Storage mod.
 * @author rubensworks
 *
 */
public class RefinedStorageModCompat implements IModCompat {

	public static ValueTypeListProxyNBTFactory<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack,
			ValueTypeListProxyPositionedNetworkMasterInventory> POSITIONED_MASTERINVENTORY;

	@Override
	public void onInit(Step initStep) {
		if(initStep == Step.PREINIT) {
			Aspects.REGISTRY.register(PartTypes.INVENTORY_READER, Sets.<IAspect>newHashSet(
					RefinedStorageAspects.Read.Network.BOOLEAN_APPLICABLE,
					RefinedStorageAspects.Read.Inventory.LIST_ITEMSTACKS
			));

			POSITIONED_MASTERINVENTORY = ValueTypeListProxyFactories.REGISTRY.register(
					new ValueTypeListProxyNBTFactory<>(getModID() + ":positionedInventory",
							ValueTypeListProxyPositionedNetworkMasterInventory.class));
		}
	}

	@Override
	public String getModID() {
		return Reference.MOD_REFINEDSTORAGE;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getComment() {
		return "Refined Storage aspects.";
	}

}
