package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.energy.CapabilityEnergy;
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
    public boolean matches(CraftingContainer grid, Level world) {
        return !assemble(grid).isEmpty();
    }

    @Override
    public ItemStack getResultItem() {
        return this.batteryItem.getItems()[0];
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inventory) {
        NonNullList<ItemStack> aitemstack = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < aitemstack.size(); ++i) {
            ItemStack itemstack = inventory.getItem(i);
            aitemstack.set(i, net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack));
        }

        return aitemstack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_ENERGY_CONTAINER_COMBINATION;
    }

    @Override
    public ItemStack assemble(CraftingContainer grid) {
        ItemStack output = getResultItem().copy();
        IEnergyStorageCapacity energyStorage = (IEnergyStorageCapacity) output.getCapability(CapabilityEnergy.ENERGY).orElse(null);

        int totalCapacity = 0;
        int totalEnergy = 0;
        int inputItems = 0;

        // Loop over the grid and count the total contents and capacity + collect energy.
        for(int j = 0; j < grid.getContainerSize(); j++) {
            ItemStack element = grid.getItem(j).copy().split(1);
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
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 9;
    }

}
