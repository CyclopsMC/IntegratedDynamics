package org.cyclops.integrateddynamics.metadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.metadata.IRegistryExportable;
import org.cyclops.cyclopscore.metadata.RegistryExportableRecipeAbstract;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DummyPropertiesComponent;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientsAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockDryingBasin;
import org.cyclops.integrateddynamics.block.BlockSqueezer;

import java.util.List;

/**
 * Drying basin recipe exporter.
 */
public class RegistryExportableDryingBasinRecipe extends RegistryExportableRecipeAbstract<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> {

    public RegistryExportableDryingBasinRecipe() {
        super(() -> RegistryEntries.BLOCK_DRYING_BASIN.getRecipeRegistry(), "drying_basin_recipe");
    }

    public static JsonObject serializeRecipeIO(IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> recipe) {
        JsonObject object = new JsonObject();

        // Properties
        int duration = recipe.getProperties().getDuration();
        object.addProperty("duration", duration);

        // Inputs
        JsonObject inputObject = new JsonObject();
        ItemStack[] inputItems = recipe.getInput().getIngredient().getMatchingStacks();
        JsonArray arrayInputs = new JsonArray();
        for (ItemStack input : inputItems) {
            arrayInputs.add(IRegistryExportable.serializeItemStack(input));
        }
        inputObject.add("item", arrayInputs);
        FluidStack inputFluid = recipe.getInput().getFluidStack();
        if (inputFluid != null) {
            inputObject.add("fluid", IRegistryExportable.serializeFluidStack(inputFluid));
        }

        // Outputs
        JsonObject outputObject = new JsonObject();
        FluidStack fluidOutput = recipe.getOutput().getFluidStack();
        if (fluidOutput != null) {
            outputObject.add("fluid", IRegistryExportable.serializeFluidStack(fluidOutput));
        }
        ItemStack itemOutput = recipe.getOutput().getFirstItemStack();
        outputObject.add("item", IRegistryExportable.serializeItemStack(itemOutput));

        // Recipe object
        object.add("input", inputObject);
        object.add("output", outputObject);

        return object;
    }

    public JsonObject serializeRecipe(IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> recipe) {
        return serializeRecipeIO(recipe);
    }

}
