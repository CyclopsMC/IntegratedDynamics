package org.cyclops.integrateddynamics.metadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.metadata.IRegistryExportable;
import org.cyclops.cyclopscore.metadata.RegistryExportableRecipeAbstract;
import org.cyclops.cyclopscore.recipe.type.IInventoryFluid;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeDryingBasin;

/**
 * Drying basin recipe exporter.
 */
public class RegistryExportableDryingBasinRecipe extends RegistryExportableRecipeAbstract<RecipeType<RecipeDryingBasin>, RecipeDryingBasin, IInventoryFluid> {

    protected RegistryExportableDryingBasinRecipe() {
        super(() -> RegistryEntries.RECIPETYPE_DRYING_BASIN);
    }

    public static JsonObject serializeRecipeStatic(RecipeDryingBasin recipe) {
        JsonObject object = new JsonObject();

        // Properties
        int duration = recipe.getDuration();
        object.addProperty("duration", duration);

        // Inputs
        JsonObject inputObject = new JsonObject();
        ItemStack[] inputItems = recipe.getInputIngredient().getItems();
        JsonArray arrayInputs = new JsonArray();
        for (ItemStack input : inputItems) {
            arrayInputs.add(IRegistryExportable.serializeItemStack(input));
        }
        inputObject.add("item", arrayInputs);
        FluidStack inputFluid = recipe.getInputFluid();
        if (inputFluid != null) {
            inputObject.add("fluid", IRegistryExportable.serializeFluidStack(inputFluid));
        }

        // Outputs
        JsonObject outputObject = new JsonObject();
        FluidStack fluidOutput = recipe.getOutputFluid();
        if (fluidOutput != null) {
            outputObject.add("fluid", IRegistryExportable.serializeFluidStack(fluidOutput));
        }
        ItemStack itemOutput = recipe.getOutputItem();
        outputObject.add("item", IRegistryExportable.serializeItemStack(itemOutput));

        // Recipe object
        object.add("input", inputObject);
        object.add("output", outputObject);

        return object;
    }

    @Override
    public JsonObject serializeRecipe(RecipeDryingBasin recipe) {
        return serializeRecipeStatic(recipe);
    }
}
