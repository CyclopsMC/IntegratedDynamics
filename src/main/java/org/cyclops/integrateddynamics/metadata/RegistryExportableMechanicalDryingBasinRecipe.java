package org.cyclops.integrateddynamics.metadata;

import com.google.gson.JsonObject;
import org.cyclops.cyclopscore.metadata.IRegistryExportable;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientAndFluidStackRecipeComponent;

/**
 * Drying basin recipe exporter.
 */
public class RegistryExportableMechanicalDryingBasinRecipe implements IRegistryExportable {

    public JsonObject serializeRecipe(IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> recipe) {
        return RegistryExportableDryingBasinRecipe.serializeRecipeIO(recipe);
    }

    @Override
    public JsonObject export() {
        return null; // TODO
    }

    @Override
    public String getName() {
        return "mechanical_drying_basin_recipe";
    }
}
