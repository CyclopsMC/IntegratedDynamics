package org.cyclops.integrateddynamics.recipe;

import net.minecraft.world.item.ItemStackTemplate;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfigCommon;
import org.cyclops.cyclopscore.init.ModBaseNeoForge;
import org.cyclops.cyclopscore.recipe.type.RecipeCraftingShapelessCustomOutput;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.PartTypeConnectorOmniDirectional;

/**
 * @author rubensworks
 */
public class RecipeSerializerCraftingSpecialShapelessOmniDirectionalConfig extends RecipeConfigCommon<RecipeCraftingShapelessCustomOutput, ModBaseNeoForge<?>> {

    public RecipeSerializerCraftingSpecialShapelessOmniDirectionalConfig() {
        super(IntegratedDynamics._instance,
                "crafting_special_shapeless_omni_directional",
                eConfig -> new RecipeCraftingShapelessCustomOutput.Serializer(
                        () -> new ItemStackTemplate(PartTypes.CONNECTOR_OMNI.getItem(), 2),
                        PartTypeConnectorOmniDirectional::transformCraftingOutput).getRecipeSerializer()
        );
    }

}
