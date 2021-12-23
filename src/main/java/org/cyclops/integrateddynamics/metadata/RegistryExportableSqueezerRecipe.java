package org.cyclops.integrateddynamics.metadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.metadata.IRegistryExportable;
import org.cyclops.cyclopscore.metadata.RegistryExportableRecipeAbstract;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeSqueezer;

/**
 * Squeezer recipe exporter.
 */
public class RegistryExportableSqueezerRecipe extends RegistryExportableRecipeAbstract<IRecipeType<RecipeSqueezer>, RecipeSqueezer, IInventory> {

    protected RegistryExportableSqueezerRecipe() {
        super(() -> RegistryEntries.RECIPETYPE_SQUEEZER);
    }

    public static JsonObject serializeRecipeStatic(RecipeSqueezer recipe) {
        JsonObject object = new JsonObject();

        // Inputs
        ItemStack[] inputs = recipe.getInputIngredient().getItems();
        JsonArray arrayInputs = new JsonArray();
        for (ItemStack input : inputs) {
            arrayInputs.add(IRegistryExportable.serializeItemStack(input));
        }

        // Outputs
        JsonObject outputObject = new JsonObject();
        FluidStack fluidOutput = recipe.getOutputFluid();
        if (fluidOutput != null) {
            outputObject.add("fluid", IRegistryExportable.serializeFluidStack(fluidOutput));
        }
        NonNullList<RecipeSqueezer.ItemStackChance> itemOutputs = recipe.getOutputItems();
        JsonArray arrayItemOutputs = new JsonArray();
        int i = 0;
        for (RecipeSqueezer.ItemStackChance itemOutput : itemOutputs) {
            JsonObject itemOutputObject = IRegistryExportable.serializeItemStack(itemOutput.getItemStack());
            float chance = itemOutput.getChance();
            if (chance > 0) {
                itemOutputObject.addProperty("chance", chance);
            }
            arrayItemOutputs.add(itemOutputObject);
        }
        outputObject.add("items", arrayItemOutputs);

        // Recipe object
        object.add("input", arrayInputs);
        object.add("output", outputObject);

        return object;
    }

    @Override
    public JsonObject serializeRecipe(RecipeSqueezer recipe) {
        return serializeRecipeStatic(recipe);
    }
}
