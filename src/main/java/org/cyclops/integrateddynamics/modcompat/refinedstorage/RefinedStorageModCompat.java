package org.cyclops.integrateddynamics.modcompat.refinedstorage;

import com.google.common.collect.Sets;
import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyFactories;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyNBTFactory;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.modcompat.refinedstorage.aspect.RefinedStorageAspects;
import org.cyclops.integrateddynamics.modcompat.refinedstorage.aspect.ValueTypeListProxyPositionedNetworkMasterFluidInventory;
import org.cyclops.integrateddynamics.modcompat.refinedstorage.aspect.ValueTypeListProxyPositionedNetworkMasterItemInventory;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * Mod compat for the Refined Storage mod.
 * @author rubensworks
 *
 */
public class RefinedStorageModCompat implements IModCompat {

	public static ValueTypeListProxyNBTFactory<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack,
			ValueTypeListProxyPositionedNetworkMasterItemInventory> POSITIONED_MASTERITEMINVENTORY;
	public static ValueTypeListProxyNBTFactory<ValueObjectTypeFluidStack, ValueObjectTypeFluidStack.ValueFluidStack,
			ValueTypeListProxyPositionedNetworkMasterFluidInventory> POSITIONED_MASTERFLUIDINVENTORY;

	@Override
	public void onInit(Step initStep) {
		if(initStep == Step.PREINIT) {
			Aspects.REGISTRY.register(PartTypes.MACHINE_READER, Sets.<IAspect>newHashSet(
					RefinedStorageAspects.Read.Network.BOOLEAN_APPLICABLE
			));
			Aspects.REGISTRY.register(PartTypes.INVENTORY_READER, Sets.<IAspect>newHashSet(
					RefinedStorageAspects.Read.Inventory.LIST_ITEMSTACKS,
					RefinedStorageAspects.Read.Inventory.LIST_CRAFTABLEITEMS,
					RefinedStorageAspects.Read.Inventory.LIST_CRAFTINGITEMS,
					RefinedStorageAspects.Read.Inventory.LIST_MISSINGCRAFTINGITEMS
			));
			Aspects.REGISTRY.register(PartTypes.FLUID_READER, Sets.<IAspect>newHashSet(
					RefinedStorageAspects.Read.Fluid.LIST_FLUIDSTACKS
			));

			Aspects.REGISTRY.register(PartTypes.INVENTORY_WRITER, Sets.<IAspect>newHashSet(
					RefinedStorageAspects.Write.ITEMSTACK_CRAFT,
					RefinedStorageAspects.Write.LIST_CRAFT
			));

			POSITIONED_MASTERITEMINVENTORY = ValueTypeListProxyFactories.REGISTRY.register(
					new ValueTypeListProxyNBTFactory<>(getModID() + ":positionedItemInventory",
							ValueTypeListProxyPositionedNetworkMasterItemInventory.class));
			POSITIONED_MASTERFLUIDINVENTORY = ValueTypeListProxyFactories.REGISTRY.register(
					new ValueTypeListProxyNBTFactory<>(getModID() + ":positionedFluidInventory",
							ValueTypeListProxyPositionedNetworkMasterFluidInventory.class));
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
