package org.cyclops.integrateddynamics.core.recipe.type;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Recipe serializer for NBT clear recipes.
 * @author rubensworks
 */
public class RecipeSerializerNbtClear implements RecipeSerializer<RecipeNbtClear> {

    public static final MapCodec<RecipeNbtClear> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            Ingredient.CODEC_NONEMPTY.fieldOf("item").forGetter(RecipeNbtClear::getInputIngredient)
                    )
                    .apply(builder, RecipeNbtClear::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeNbtClear> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, RecipeNbtClear::getInputIngredient,
            RecipeNbtClear::new
    );

    @Override
    public MapCodec<RecipeNbtClear> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RecipeNbtClear> streamCodec() {
        return STREAM_CODEC;
    }
}
