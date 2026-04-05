package org.cyclops.integrateddynamics.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ItemVariableCopyRecipe}.
 * @author rubensworks
 */
public class ItemVariableCopyRecipeConfig extends RecipeConfigCommon<ItemVariableCopyRecipe, IntegratedDynamics> {

    public static final RecipeSerializer<ItemVariableCopyRecipe> SERIALIZER = new RecipeSerializer<>(
            MapCodec.unit(new ItemVariableCopyRecipe()),
            StreamCodec.unit(new ItemVariableCopyRecipe())
    );

    public ItemVariableCopyRecipeConfig() {
        super(IntegratedDynamics._instance,
                "crafting_special_variable_copy",
                eConfig -> SERIALIZER);
    }

}
