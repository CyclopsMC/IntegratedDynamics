package org.cyclops.integrateddynamics.core.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.block.BlockEnergyBattery;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;

import java.util.function.Supplier;

/**
 * Recipe for combining energy batteries in a shapeless manner.
 * @author rubensworks
 */
public class ItemBlockEnergyContainerCombinationRecipe extends SpecialRecipe {

	private final IRecipeSerializer<?> serializer;
	private final int size;
	private final Supplier<ItemBlockEnergyContainer> batteryItem;
	private final int maxCapacity;

	/**
	 * Make a new instance.
	 * @param serializer The serializer;
	 * @param id The recipe id.
	 * @param size The recipe size (should be called multiple times (1 to 9) to allow for all shapeless crafting types.
	 * @param batteryItem The battery item that is combinable.
	 * @param maxCapacity The maximum allowed capacity.
	 */
	public ItemBlockEnergyContainerCombinationRecipe(IRecipeSerializer<?> serializer, ResourceLocation id, int size, Supplier<ItemBlockEnergyContainer> batteryItem, int maxCapacity) {
		super(id);
		this.serializer = serializer;
		this.size = size;
		this.batteryItem = batteryItem;
		this.maxCapacity = maxCapacity;
	}

	@Override
	public boolean matches(CraftingInventory grid, World world) {
		return !getCraftingResult(grid).isEmpty();
	}
	
	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(this.batteryItem.get());
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
		return this.serializer;
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory grid) {
		ItemStack output = getRecipeOutput().copy();
		IEnergyStorageCapacity energyStorage = (IEnergyStorageCapacity) output.getCapability(CapabilityEnergy.ENERGY, null);

		int totalCapacity = 0;
		int totalEnergy = 0;
		int inputItems = 0;
		
		// Loop over the grid and count the total contents and capacity + collect energy.
		for(int j = 0; j < grid.getSizeInventory(); j++) {
			ItemStack element = grid.getStackInSlot(j);
			if(!element.isEmpty()) {
				if(element.getItem() == batteryItem.get()) {
					IEnergyStorageCapacity currentEnergyStorage = (IEnergyStorageCapacity) element.getCapability(CapabilityEnergy.ENERGY, null);
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
		energyStorage.receiveEnergy(totalEnergy, false);
		
		return output;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= size;
	}

}
