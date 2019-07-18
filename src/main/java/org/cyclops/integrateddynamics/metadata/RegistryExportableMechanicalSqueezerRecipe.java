package org.cyclops.integrateddynamics.metadata;

import com.google.gson.JsonObject;
import org.cyclops.cyclopscore.metadata.RegistryExportableRecipeAbstract;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientsAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.block.BlockMechanicalSqueezer;

/**
 * Mechanical squeezer recipe exporter.
 */
public class RegistryExportableMechanicalSqueezerRecipe extends RegistryExportableRecipeAbstract<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties> {

    public RegistryExportableMechanicalSqueezerRecipe() {
        super(() -> BlockMechanicalSqueezer.getInstance().getRecipeRegistry(), "mechanical_squeezer_recipe");
    }

    public JsonObject serializeRecipe(IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties> recipe) {
        JsonObject object = RegistryExportableSqueezerRecipe.serializeRecipeIO(recipe);

        // Properties
        int duration = recipe.getProperties().getDuration();
        object.addProperty("duration", duration);

        return object;
    }

}
