package org.cyclops.integrateddynamics.core.recipe.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Recipe serializer for NBT clear recipes.
 * @author rubensworks
 */
public class RecipeSerializerNbtClear implements RecipeSerializer<RecipeNbtClear> {

    public static final Codec<RecipeNbtClear> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(
                            Ingredient.CODEC_NONEMPTY.fieldOf("item").forGetter(RecipeNbtClear::getInputIngredient)
                    )
                    .apply(builder, RecipeNbtClear::new)
    );

    @Override
    public Codec<RecipeNbtClear> codec() {
        return CODEC;
    }

    @Override
    public RecipeNbtClear fromNetwork(FriendlyByteBuf buffer) {
        Ingredient inputIngredient = Ingredient.fromNetwork(buffer);
        return new RecipeNbtClear(inputIngredient);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RecipeNbtClear recipe) {
        recipe.getInputIngredient().toNetwork(buffer);
    }
}
