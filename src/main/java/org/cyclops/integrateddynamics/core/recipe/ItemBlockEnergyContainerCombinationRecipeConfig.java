package org.cyclops.integrateddynamics.core.recipe;

import net.minecraft.item.crafting.SpecialRecipeSerializer;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfig;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;

import java.util.function.Supplier;

/**
 * Config for {@link ItemBlockEnergyContainerCombinationRecipe}.
 * @author rubensworks
 */
public class ItemBlockEnergyContainerCombinationRecipeConfig extends RecipeConfig<ItemBlockEnergyContainerCombinationRecipe> {

    public ItemBlockEnergyContainerCombinationRecipeConfig(int size, Supplier<ItemBlockEnergyContainer> batteryItem, String batteryName, int maxCapacity) {
        super(IntegratedDynamics._instance,
                String.format("crafting_special_energycontainer_combination_%s_%s_%s", size, batteryName, maxCapacity),
                eConfig -> {
                    Wrapper<SpecialRecipeSerializer<ItemBlockEnergyContainerCombinationRecipe>> serializer = new Wrapper<>();
                    serializer.set(new SpecialRecipeSerializer<>((id) -> new ItemBlockEnergyContainerCombinationRecipe(serializer.get(), id, size, batteryItem, maxCapacity)));
                    return serializer.get();
                });
    }

}
