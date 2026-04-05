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
public class RecipeSerializerCraftingSpecialShapedOmniDirectional3Config extends RecipeConfigCommon<RecipeCraftingShapedCustomOutput, IntegratedDynamics> {

    public RecipeSerializerCraftingSpecialShapedOmniDirectional3Config() {
        super(IntegratedDynamics._instance,
                "crafting_special_shaped_omni_directional_3",
                eConfig -> new RecipeCraftingShapedCustomOutput.Serializer(
                        () -> new ItemStackTemplate(PartTypes.CONNECTOR_OMNI.getItem(), 3),
                        PartTypeConnectorOmniDirectional::transformCraftingOutput).getRecipeSerializer()
        );
    }

}
