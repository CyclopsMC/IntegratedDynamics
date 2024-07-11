package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.CommonHooks;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageMutable;

/**
 * Recipe for combining energy batteries in a shapeless manner.
 * @author rubensworks
 */
public class RecipeEnergyContainerCombination extends CustomRecipe {

    private final Ingredient batteryItem;
    private final int maxCapacity;

    public RecipeEnergyContainerCombination(Ingredient batteryItem, int maxCapacity) {
        super(CraftingBookCategory.MISC);
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
    public boolean matches(CraftingInput grid, Level world) {
        return !assemble(grid, world.registryAccess()).isEmpty();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        return this.batteryItem.getItems()[0];
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inventory) {
        NonNullList<ItemStack> aitemstack = NonNullList.withSize(inventory.size(), ItemStack.EMPTY);

        for (int i = 0; i < aitemstack.size(); ++i) {
            ItemStack itemstack = inventory.getItem(i);
            aitemstack.set(i, CommonHooks.getCraftingRemainingItem(itemstack));
        }

        return aitemstack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_ENERGY_CONTAINER_COMBINATION.get();
    }

    @Override
    public ItemStack assemble(CraftingInput grid, HolderLookup.Provider registryAccess) {
        ItemStack output = getResultItem(registryAccess).copy();
        IEnergyStorageCapacity energyStorage = (IEnergyStorageCapacity) output.getCapability(Capabilities.EnergyStorage.ITEM);

        int totalCapacity = 0;
        int totalEnergy = 0;
        int inputItems = 0;

        // Loop over the grid and count the total contents and capacity + collect energy.
        for(int j = 0; j < grid.size(); j++) {
            ItemStack element = grid.getItem(j).copy().split(1);
            if(!element.isEmpty()) {
                if(this.batteryItem.test(element)) {
                    IEnergyStorageCapacity currentEnergyStorage = (IEnergyStorageCapacity) element.getCapability(Capabilities.EnergyStorage.ITEM);
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
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 9;
    }

}
