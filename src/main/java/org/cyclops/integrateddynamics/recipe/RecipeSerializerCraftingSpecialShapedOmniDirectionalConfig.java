package org.cyclops.integrateddynamics.recipe;

import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfig;
import org.cyclops.cyclopscore.recipe.type.RecipeCraftingShapedCustomOutput;
import org.cyclops.cyclopscore.recipe.type.RecipeSerializerCraftingShapedCustomOutput;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.PartTypeConnectorOmniDirectional;

/**
 * @author rubensworks
 */
public class RecipeSerializerCraftingSpecialShapedOmniDirectionalConfig extends RecipeConfig<RecipeCraftingShapedCustomOutput> {

    public RecipeSerializerCraftingSpecialShapedOmniDirectionalConfig() {
        super(IntegratedDynamics._instance,
                "crafting_special_shaped_omni_directional",
                eConfig -> new RecipeSerializerCraftingShapedCustomOutput(
                        () -> new ItemStack(PartTypes.CONNECTOR_OMNI.getItem(), 2),
                        PartTypeConnectorOmniDirectional::transformCraftingOutput)
        );
    }

}
