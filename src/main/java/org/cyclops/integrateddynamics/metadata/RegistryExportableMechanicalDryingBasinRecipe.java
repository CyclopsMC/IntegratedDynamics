package org.cyclops.integrateddynamics.metadata;

import com.google.gson.JsonObject;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.cyclops.cyclopscore.metadata.RegistryExportableRecipeAbstract;
import org.cyclops.cyclopscore.recipe.type.IInventoryFluid;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeMechanicalDryingBasin;

/**
 * Drying basin recipe exporter.
 */
public class RegistryExportableMechanicalDryingBasinRecipe extends RegistryExportableRecipeAbstract<RecipeType<RecipeMechanicalDryingBasin>, RecipeMechanicalDryingBasin, IInventoryFluid> {

    protected RegistryExportableMechanicalDryingBasinRecipe() {
        super(RegistryEntries.RECIPETYPE_MECHANICAL_DRYING_BASIN::get);
    }

    @Override
    public JsonObject serializeRecipe(RecipeHolder<RecipeMechanicalDryingBasin> recipe) {
        return RegistryExportableDryingBasinRecipe.serializeRecipeStatic(recipe.value());
    }
}
