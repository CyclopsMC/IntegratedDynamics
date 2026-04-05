package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.cyclops.cyclopscore.helper.IModHelpers;
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
        super();
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
        return !assemble(grid).isEmpty();
    }

    protected ItemStack getResultItem() {
        return new ItemStack(this.batteryItem.items().findFirst().get());
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inventory) {
        NonNullList<ItemStack> aitemstack = NonNullList.withSize(inventory.size(), ItemStack.EMPTY);

        for (int i = 0; i < aitemstack.size(); ++i) {
            ItemStack itemstack = inventory.getItem(i);
            net.minecraft.world.item.ItemStackTemplate remainder = itemstack.getItem().getCraftingRemainder(itemstack);
            aitemstack.set(i, remainder != null ? remainder.create() : ItemStack.EMPTY);
        }

        return aitemstack;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_ENERGY_CONTAINER_COMBINATION.get();
    }

    @Override
    public ItemStack assemble(CraftingInput grid) {
        ItemStack output = getResultItem().copy();
        IEnergyStorageCapacity energyStorage = (IEnergyStorageCapacity) output.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(output));

        int totalCapacity = 0;
        int totalEnergy = 0;
        int inputItems = 0;

        // Loop over the grid and count the total contents and capacity + collect energy.
        for(int j = 0; j < grid.size(); j++) {
            ItemStack element = grid.getItem(j).copy().split(1);
            if(!element.isEmpty()) {
                if(this.batteryItem.test(element)) {
                    IEnergyStorageCapacity currentEnergyStorage = (IEnergyStorageCapacity) element.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(element));
                    inputItems++;
                    totalEnergy = IModHelpers.get().getBaseHelpers().addSafe(totalEnergy, currentEnergyStorage.getAmountAsInt());
                    totalCapacity = IModHelpers.get().getBaseHelpers().addSafe(totalCapacity, currentEnergyStorage.getCapacityAsInt());
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

}
