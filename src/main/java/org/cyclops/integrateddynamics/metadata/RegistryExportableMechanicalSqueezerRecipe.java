package org.cyclops.integrateddynamics.metadata;

import com.google.gson.JsonObject;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.cyclops.cyclopscore.metadata.RegistryExportableRecipeAbstract;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeMechanicalSqueezer;

/**
 * Mechanical squeezer recipe exporter.
 */
public class RegistryExportableMechanicalSqueezerRecipe extends RegistryExportableRecipeAbstract<RecipeType<RecipeMechanicalSqueezer>, RecipeMechanicalSqueezer, CraftingInput> {

    protected RegistryExportableMechanicalSqueezerRecipe() {
        super(RegistryEntries.RECIPETYPE_MECHANICAL_SQUEEZER::get);
    }

    @Override
    public JsonObject serializeRecipe(RecipeHolder<RecipeMechanicalSqueezer> recipe) {
        JsonObject object = RegistryExportableSqueezerRecipe.serializeRecipeStatic(recipe.value());

        // Properties
        int duration = recipe.value().getDuration();
        object.addProperty("duration", duration);

        return object;
    }
}
