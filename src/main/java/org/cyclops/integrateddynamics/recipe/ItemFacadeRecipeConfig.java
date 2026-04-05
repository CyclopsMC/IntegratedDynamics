package org.cyclops.integrateddynamics.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ItemFacadeRecipe}.
 * @author rubensworks
 */
public class ItemFacadeRecipeConfig extends RecipeConfigCommon<ItemFacadeRecipe, IntegratedDynamics> {

    public static final RecipeSerializer<ItemFacadeRecipe> SERIALIZER = new RecipeSerializer<>(
            MapCodec.unit(new ItemFacadeRecipe()),
            StreamCodec.unit(new ItemFacadeRecipe())
    );

    public ItemFacadeRecipeConfig() {
        super(IntegratedDynamics._instance,
                "crafting_special_facade",
                eConfig -> SERIALIZER);
    }

}
