package org.cyclops.integrateddynamics.metadata;

import com.google.gson.JsonObject;
import org.cyclops.cyclopscore.metadata.RegistryExportableRecipeAbstract;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockMechanicalDryingBasin;

/**
 * Drying basin recipe exporter.
 */
public class RegistryExportableMechanicalDryingBasinRecipe extends RegistryExportableRecipeAbstract<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> {

    public RegistryExportableMechanicalDryingBasinRecipe() {
        super(() -> RegistryEntries.BLOCK_MECHANICAL_DRYING_BASIN.getRecipeRegistry(), "mechanical_drying_basin_recipe");
    }

    public JsonObject serializeRecipe(IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> recipe) {
        return RegistryExportableDryingBasinRecipe.serializeRecipeIO(recipe);
    }

}
