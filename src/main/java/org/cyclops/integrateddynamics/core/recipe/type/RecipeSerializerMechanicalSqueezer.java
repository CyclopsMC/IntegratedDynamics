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

import java.util.Optional;

/**
 * Recipe serializer for mechanical squeezer recipes
 * @author rubensworks
 */
public class RecipeSerializerMechanicalSqueezer implements RecipeSerializer<RecipeMechanicalSqueezer> {

    public static final MapCodec<RecipeMechanicalSqueezer> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            Ingredient.CODEC_NONEMPTY.fieldOf("input_item").forGetter(RecipeMechanicalSqueezer::getInputIngredient),
                            new ListCodecStrict<>(RecipeSerializerSqueezer.CODEC_INGREDIENT_CHANCE).optionalFieldOf("output_items").forGetter(r -> r.getOutputItems().isEmpty() ? Optional.empty() : Optional.of(r.getOutputItems().stream().toList())),
                            FluidStack.CODEC.optionalFieldOf("output_fluid").forGetter(RecipeMechanicalSqueezer::getOutputFluid),
                            Codec.INT.fieldOf("duration").forGetter(RecipeMechanicalSqueezer::getDuration)
                    )
                    .apply(builder, (inputIngredient, outputItemStacks, outputFluid, duration) -> {
                        // Validation
                        if (outputItemStacks.isEmpty() && outputFluid.isEmpty()) {
                            throw new JsonSyntaxException("An output item or fluid is required");
                        }
                        if (duration <= 0) {
                            throw new JsonSyntaxException("Durations must be higher than one tick");
                        }

                        return new RecipeMechanicalSqueezer(inputIngredient, outputItemStacks.map(NonNullList::copyOf).orElseGet(NonNullList::create), outputFluid, duration);
                    })
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeMechanicalSqueezer> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, RecipeMechanicalSqueezer::getInputIngredient,
            RecipeSerializerSqueezer.STREAM_CODEC_INGREDIENT_CHANCE.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)), RecipeMechanicalSqueezer::getOutputItems,
            ByteBufCodecs.optional(FluidStack.STREAM_CODEC), RecipeMechanicalSqueezer::getOutputFluid,
            ByteBufCodecs.INT, RecipeMechanicalSqueezer::getDuration,
            RecipeMechanicalSqueezer::new
    );

    @Override
    public MapCodec<RecipeMechanicalSqueezer> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RecipeMechanicalSqueezer> streamCodec() {
        return STREAM_CODEC;
    }
}
