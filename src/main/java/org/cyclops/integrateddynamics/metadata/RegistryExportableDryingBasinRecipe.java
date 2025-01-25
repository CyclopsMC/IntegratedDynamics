package org.cyclops.integrateddynamics.metadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.metadata.IRegistryExportable;
import org.cyclops.cyclopscore.metadata.RegistryExportableRecipeAbstract;
import org.cyclops.cyclopscore.recipe.type.IInventoryFluid;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeDryingBasin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Drying basin recipe exporter.
 */
public class RegistryExportableDryingBasinRecipe extends RegistryExportableRecipeAbstract<RecipeType<RecipeDryingBasin>, RecipeDryingBasin, IInventoryFluid> {

    protected RegistryExportableDryingBasinRecipe() {
        super(RegistryEntries.RECIPETYPE_DRYING_BASIN::get);
    }

    public static JsonObject serializeRecipeStatic(RecipeDryingBasin recipe) {
        JsonObject object = new JsonObject();

        // Properties
        int duration = recipe.getDuration();
        object.addProperty("duration", duration);

        // Inputs
        JsonObject inputObject = new JsonObject();
        List<Holder<Item>> inputItems = recipe.getInputIngredient().map(i -> i.items().toList()).orElse(Collections.emptyList());
        JsonArray arrayInputs = new JsonArray();
        for (Holder<Item> input : inputItems) {
            arrayInputs.add(IRegistryExportable.serializeItemStack(new ItemStack(input)));
        }
        inputObject.add("item", arrayInputs);
        Optional<FluidStack> inputFluid = recipe.getInputFluid();
        inputFluid.ifPresent(fluidStack -> inputObject.add("fluid", IRegistryExportable.serializeFluidStack(fluidStack)));

        // Outputs
        JsonObject outputObject = new JsonObject();
        Optional<FluidStack> fluidOutput = recipe.getOutputFluid();
        fluidOutput.ifPresent(fluidStack -> outputObject.add("fluid", IRegistryExportable.serializeFluidStack(fluidStack)));
        ItemStack itemOutput = recipe.getOutputItemFirst().orElse(ItemStack.EMPTY);
        outputObject.add("item", IRegistryExportable.serializeItemStack(itemOutput));

        // Recipe object
        object.add("input", inputObject);
        object.add("output", outputObject);

        return object;
    }

    @Override
    public JsonObject serializeRecipe(RecipeHolder<RecipeDryingBasin> recipe) {
        return serializeRecipeStatic(recipe.value());
    }
}
