package org.cyclops.integrateddynamics.recipe;

import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfig;
import org.cyclops.cyclopscore.recipe.type.RecipeCraftingShapedCustomOutput;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.PartTypeConnectorOmniDirectional;

/**
 * @author rubensworks
 */
public class RecipeSerializerCraftingSpecialShapedOmniDirectional3Config extends RecipeConfig<RecipeCraftingShapedCustomOutput> {

    public RecipeSerializerCraftingSpecialShapedOmniDirectional3Config() {
        super(IntegratedDynamics._instance,
                "crafting_special_shaped_omni_directional_3",
                eConfig -> new RecipeCraftingShapedCustomOutput.Serializer(
                        () -> new ItemStack(PartTypes.CONNECTOR_OMNI.getItem(), 3),
                        PartTypeConnectorOmniDirectional::transformCraftingOutput)
        );
    }

}
