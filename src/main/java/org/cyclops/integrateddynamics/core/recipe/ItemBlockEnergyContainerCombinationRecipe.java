package org.cyclops.integrateddynamics.core.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.block.BlockEnergyBattery;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;

/**
 * recipe for combining energy batteries in a shapeless manner.
 * @author rubensworks
 *
 */
public class ItemBlockEnergyContainerCombinationRecipe implements IRecipe {

	private final int size;
	private final ItemBlockEnergyContainer batteryItem;
	private final int maxCapacity;

	/**
	 * Make a new instance.
	 * @param size The recipe size (should be called multiple times (1 to 9) to allow for all shapeless crafting types.
	 * @param batteryItem The battery item that is combinable.
	 */
	public ItemBlockEnergyContainerCombinationRecipe(int size, ItemBlockEnergyContainer batteryItem, int maxCapacity) {
		this.size = size;
		this.batteryItem = batteryItem;
		this.maxCapacity = maxCapacity;
	}

	@Override
	public boolean matches(InventoryCrafting grid, World world) {
		return getCraftingResult(grid) != null;
	}
	
	@Override
	public int getRecipeSize() {
		return size;
	}
	
	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(BlockEnergyBattery.getInstance());
	}

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inventory) {
		ItemStack[] aitemstack = new ItemStack[inventory.getSizeInventory()];

        for (int i = 0; i < aitemstack.length; ++i) {
            ItemStack itemstack = inventory.getStackInSlot(i);
            aitemstack[i] =  net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack);
        }

        return aitemstack;
    }

    @Override
	public ItemStack getCraftingResult(InventoryCrafting grid) {
		ItemStack output = getRecipeOutput().copy();
		IEnergyStorageCapacity energyStorage = (IEnergyStorageCapacity) output.getCapability(CapabilityEnergy.ENERGY, null);

		int totalCapacity = 0;
		int totalEnergy = 0;
		int inputItems = 0;
		
		// Loop over the grid and count the total contents and capacity + collect energy.
		for(int j = 0; j < grid.getSizeInventory(); j++) {
			ItemStack element = grid.getStackInSlot(j);
			if(element != null) {
				if(element.getItem() == batteryItem) {
					IEnergyStorageCapacity currentEnergyStorage = (IEnergyStorageCapacity) element.getCapability(CapabilityEnergy.ENERGY, null);
					inputItems++;
					totalEnergy = Helpers.addSafe(totalEnergy, currentEnergyStorage.getEnergyStored());
					totalCapacity = Helpers.addSafe(totalCapacity, currentEnergyStorage.getMaxEnergyStored());
				} else {
					return null;
				}
			}
		}
		
		if(inputItems < 2
				|| totalCapacity > this.maxCapacity) {
			return null;
		}
		
		// Set capacity and fill fluid into output.
		energyStorage.setCapacity(totalCapacity);
		energyStorage.receiveEnergy(totalEnergy, false);
		
		return output;
	}
	
}
