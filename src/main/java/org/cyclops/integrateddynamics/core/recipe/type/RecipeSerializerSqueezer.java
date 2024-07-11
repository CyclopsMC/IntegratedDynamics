package org.cyclops.integrateddynamics.core.recipe.type;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.codec.ListCodecStrict;
import org.cyclops.cyclopscore.helper.RecipeSerializerHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;

import java.util.Optional;

/**
 * Recipe serializer for squeezer recipes
 * @author rubensworks
 */
public class RecipeSerializerSqueezer implements RecipeSerializer<RecipeSqueezer> {

    public static final Codec<RecipeSqueezer.IngredientChance> CODEC_INGREDIENT_CHANCE = RecipeSerializerHelpers
            .getCodecItemStackOrTagChance(() -> GeneralConfig.recipeTagOutputModPriorities)
            .xmap(
                    RecipeSqueezer.IngredientChance::new,
                    RecipeSqueezer.IngredientChance::getIngredientChance
            );
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeSqueezer.IngredientChance> STREAM_CODEC_INGREDIENT_CHANCE = RecipeSerializerHelpers.STREAM_CODEC_ITEMSTACK_OR_ITEMSTACKINGREDIENT_CHANCE
            .map(
                    RecipeSqueezer.IngredientChance::new,
                    RecipeSqueezer.IngredientChance::getIngredientChance
            );

    public static final MapCodec<RecipeSqueezer> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            Ingredient.CODEC_NONEMPTY.fieldOf("input_item").forGetter(RecipeSqueezer::getInputIngredient),
                            new ListCodecStrict<>(RecipeSerializerSqueezer.CODEC_INGREDIENT_CHANCE).optionalFieldOf("output_items").forGetter(r -> r.getOutputItems().isEmpty() ? Optional.empty() : Optional.of(r.getOutputItems().stream().toList())),
                            FluidStack.CODEC.optionalFieldOf("output_fluid").forGetter(RecipeSqueezer::getOutputFluid)
                    )
                    .apply(builder, (inputIngredient, outputItemStacks, outputFluid) -> {
                        // Validation
                        if (outputItemStacks.isEmpty() && outputFluid.isEmpty()) {
                            throw new JsonSyntaxException("An output item or fluid is required");
                        }

                        return new RecipeSqueezer(inputIngredient, outputItemStacks.map(NonNullList::copyOf).orElseGet(NonNullList::create), outputFluid);
                    })
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeSqueezer> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, RecipeSqueezer::getInputIngredient,
            STREAM_CODEC_INGREDIENT_CHANCE.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)), RecipeSqueezer::getOutputItems,
            ByteBufCodecs.optional(FluidStack.STREAM_CODEC), RecipeSqueezer::getOutputFluid,
            RecipeSqueezer::new
    );

    @Override
    public MapCodec<RecipeSqueezer> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RecipeSqueezer> streamCodec() {
        return STREAM_CODEC;
    }
}
