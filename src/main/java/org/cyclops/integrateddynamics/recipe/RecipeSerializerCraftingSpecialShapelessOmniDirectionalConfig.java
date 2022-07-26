package org.cyclops.integrateddynamics.recipe;

import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfig;
import org.cyclops.cyclopscore.recipe.type.RecipeCraftingShapelessCustomOutput;
import org.cyclops.cyclopscore.recipe.type.RecipeSerializerCraftingShapelessCustomOutput;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.PartTypeConnectorOmniDirectional;

/**
 * @author rubensworks
 */
public class RecipeSerializerCraftingSpecialShapelessOmniDirectionalConfig extends RecipeConfig<RecipeCraftingShapelessCustomOutput> {

    public RecipeSerializerCraftingSpecialShapelessOmniDirectionalConfig() {
        super(IntegratedDynamics._instance,
                "crafting_special_shapeless_omni_directional",
                eConfig -> new RecipeSerializerCraftingShapelessCustomOutput(
                        () -> new ItemStack(PartTypes.CONNECTOR_OMNI.getItem(), 2),
                        PartTypeConnectorOmniDirectional::transformCraftingOutput)
        );
    }

}
