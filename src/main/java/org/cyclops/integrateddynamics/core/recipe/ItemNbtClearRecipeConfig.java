package org.cyclops.integrateddynamics.core.recipe;

import net.minecraft.item.Item;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfig;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.integrateddynamics.IntegratedDynamics;

import java.util.function.Supplier;

/**
 * Config for {@link ItemNbtClearRecipe}.
 * @author rubensworks
 */
public class ItemNbtClearRecipeConfig extends RecipeConfig<ItemNbtClearRecipe> {

    public ItemNbtClearRecipeConfig(Class<? extends Item> clazz, Supplier<Item> dummyInstance, String name) {
        super(IntegratedDynamics._instance,
                "crafting_special_" + name,
                eConfig -> {
                    Wrapper<SpecialRecipeSerializer<ItemNbtClearRecipe>> serializer = new Wrapper<>();
                    serializer.set(new SpecialRecipeSerializer<>((id) -> new ItemNbtClearRecipe(serializer.get(), id, clazz, dummyInstance)));
                    return serializer.get();
                });
    }

}
