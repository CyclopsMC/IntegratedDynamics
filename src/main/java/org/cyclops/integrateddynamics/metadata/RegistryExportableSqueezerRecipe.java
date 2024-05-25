package org.cyclops.integrateddynamics.metadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.metadata.IRegistryExportable;
import org.cyclops.cyclopscore.metadata.RegistryExportableRecipeAbstract;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeSqueezer;

import java.util.Optional;

/**
 * Squeezer recipe exporter.
 */
public class RegistryExportableSqueezerRecipe extends RegistryExportableRecipeAbstract<RecipeType<RecipeSqueezer>, RecipeSqueezer, Container> {

    protected RegistryExportableSqueezerRecipe() {
        super(RegistryEntries.RECIPETYPE_SQUEEZER::get);
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
        Optional<FluidStack> fluidOutput = recipe.getOutputFluid();
        fluidOutput.ifPresent(fluidStack -> outputObject.add("fluid", IRegistryExportable.serializeFluidStack(fluidStack)));
        NonNullList<RecipeSqueezer.IngredientChance> itemOutputs = recipe.getOutputItems();
        JsonArray arrayItemOutputs = new JsonArray();
        int i = 0;
        for (RecipeSqueezer.IngredientChance itemOutput : itemOutputs) {
            JsonObject itemOutputObject = IRegistryExportable.serializeItemStack(itemOutput.getIngredientFirst());
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
    public JsonObject serializeRecipe(RecipeHolder<RecipeSqueezer> recipe) {
        return serializeRecipeStatic(recipe.value());
    }
}
