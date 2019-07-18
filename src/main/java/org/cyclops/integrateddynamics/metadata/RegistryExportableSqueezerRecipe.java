package org.cyclops.integrateddynamics.metadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.metadata.IRegistryExportable;
import org.cyclops.cyclopscore.metadata.RegistryExportableRecipeAbstract;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DummyPropertiesComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientsAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.block.BlockSqueezer;

import java.util.List;

/**
 * Squeezer recipe exporter.
 */
public class RegistryExportableSqueezerRecipe extends RegistryExportableRecipeAbstract<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent> {

    public RegistryExportableSqueezerRecipe() {
        super(() -> BlockSqueezer.getInstance().getRecipeRegistry(), "squeezer_recipe");
    }

    public static JsonObject serializeRecipeIO(IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, ?> recipe) {
        JsonObject object = new JsonObject();

        // Inputs
        ItemStack[] inputs = recipe.getInput().getIngredient().getMatchingStacks();
        JsonArray arrayInputs = new JsonArray();
        for (ItemStack input : inputs) {
            arrayInputs.add(IRegistryExportable.serializeItemStack(input));
        }

        // Outputs
        JsonObject outputObject = new JsonObject();
        FluidStack fluidOutput = recipe.getOutput().getFluidStack();
        if (fluidOutput != null) {
            outputObject.add("fluid", IRegistryExportable.serializeFluidStack(fluidOutput));
        }
        List<ItemStack> itemOutputs = recipe.getOutput().getIngredients();
        JsonArray arrayItemOutputs = new JsonArray();
        for (ItemStack itemOutput : itemOutputs) {
            arrayItemOutputs.add(IRegistryExportable.serializeItemStack(itemOutput));
        }
        outputObject.add("items", arrayItemOutputs);
        float chance = recipe.getOutput().getChance();
        if (chance > 0) {
            outputObject.addProperty("chance", chance);
        }

        // Recipe object
        object.add("input", arrayInputs);
        object.add("output", outputObject);

        return object;
    }

    public JsonObject serializeRecipe(IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent> recipe) {
        return serializeRecipeIO(recipe);
    }

}
