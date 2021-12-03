package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageMutable;

/**
 * Recipe for combining energy batteries in a shapeless manner.
 * @author rubensworks
 */
public class RecipeEnergyContainerCombination extends SpecialRecipe {

	private final Ingredient batteryItem;
	private final int maxCapacity;

	public RecipeEnergyContainerCombination(ResourceLocation id, Ingredient batteryItem, int maxCapacity) {
		super(id);
		this.batteryItem = batteryItem;
		this.maxCapacity = maxCapacity;
	}

	public Ingredient getBatteryItem() {
		return batteryItem;
	}

	public int getMaxCapacity() {
		return maxCapacity;
	}

	@Override
	public boolean matches(CraftingInventory grid, World world) {
		return !getCraftingResult(grid).isEmpty();
	}
	
	@Override
	public ItemStack getRecipeOutput() {
		return this.batteryItem.getMatchingStacks()[0];
	}

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inventory) {
		NonNullList<ItemStack> aitemstack = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);

        for (int i = 0; i < aitemstack.size(); ++i) {
            ItemStack itemstack = inventory.getStackInSlot(i);
            aitemstack.set(i, net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack));
        }

        return aitemstack;
    }

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return RegistryEntries.RECIPESERIALIZER_ENERGY_CONTAINER_COMBINATION;
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory grid) {
		ItemStack output = getRecipeOutput().copy();
		IEnergyStorageCapacity energyStorage = (IEnergyStorageCapacity) output.getCapability(CapabilityEnergy.ENERGY).orElse(null);

		int totalCapacity = 0;
		int totalEnergy = 0;
		int inputItems = 0;
		
		// Loop over the grid and count the total contents and capacity + collect energy.
		for(int j = 0; j < grid.getSizeInventory(); j++) {
			ItemStack element = grid.getStackInSlot(j).copy().split(1);
			if(!element.isEmpty()) {
				if(this.batteryItem.test(element)) {
					IEnergyStorageCapacity currentEnergyStorage = (IEnergyStorageCapacity) element.getCapability(CapabilityEnergy.ENERGY).orElse(null);
					inputItems++;
					totalEnergy = Helpers.addSafe(totalEnergy, currentEnergyStorage.getEnergyStored());
					totalCapacity = Helpers.addSafe(totalCapacity, currentEnergyStorage.getMaxEnergyStored());
				} else {
					return ItemStack.EMPTY;
				}
			}
		}
		
		if(inputItems < 2
				|| totalCapacity > this.maxCapacity) {
			return ItemStack.EMPTY;
		}
		
		// Set capacity and fill fluid into output.
		energyStorage.setCapacity(totalCapacity);
		((IEnergyStorageMutable) energyStorage).setEnergy(totalEnergy);
		
		return output;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 9;
	}

}
