package org.cyclops.integrateddynamics.recipe;

import net.minecraft.world.item.ItemStackTemplate;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfigCommon;
import org.cyclops.cyclopscore.recipe.type.RecipeCraftingShapedCustomOutput;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.PartTypeConnectorOmniDirectional;

/**
 * @author rubensworks
 */
public class RecipeSerializerCraftingSpecialShapedOmniDirectionalConfig extends RecipeConfigCommon<RecipeCraftingShapedCustomOutput, IntegratedDynamics> {

    public RecipeSerializerCraftingSpecialShapedOmniDirectionalConfig() {
        super(IntegratedDynamics._instance,
                "crafting_special_shaped_omni_directional",
                eConfig -> new RecipeCraftingShapedCustomOutput.Serializer(
                        () -> new ItemStackTemplate(PartTypes.CONNECTOR_OMNI.getItem(), 2),
                        PartTypeConnectorOmniDirectional::transformCraftingOutput).getRecipeSerializer()
        );
    }

}
