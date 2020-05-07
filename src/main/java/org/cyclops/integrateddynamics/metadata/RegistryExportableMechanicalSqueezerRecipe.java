package org.cyclops.integrateddynamics.metadata;

import com.google.gson.JsonObject;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipeType;
import org.cyclops.cyclopscore.metadata.RegistryExportableRecipeAbstract;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeMechanicalSqueezer;

/**
 * Mechanical squeezer recipe exporter.
 */
public class RegistryExportableMechanicalSqueezerRecipe extends RegistryExportableRecipeAbstract<IRecipeType<RecipeMechanicalSqueezer>, RecipeMechanicalSqueezer, IInventory> {

    protected RegistryExportableMechanicalSqueezerRecipe() {
        super(() -> RegistryEntries.RECIPETYPE_MECHANICAL_SQUEEZER);
    }

    @Override
    public JsonObject serializeRecipe(RecipeMechanicalSqueezer recipe) {
        JsonObject object = RegistryExportableSqueezerRecipe.serializeRecipeStatic(recipe);

        // Properties
        int duration = recipe.getDuration();
        object.addProperty("duration", duration);

        return object;
    }
}
