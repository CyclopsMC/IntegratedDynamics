package org.cyclops.integrateddynamics.core.recipe.type;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.helper.RecipeSerializerHelpers;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Recipe serializer for mechanical squeezer recipes
 * @author rubensworks
 */
public class RecipeSerializerMechanicalSqueezer implements RecipeSerializer<RecipeMechanicalSqueezer> {

    public static final Codec<RecipeMechanicalSqueezer> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(
                            Ingredient.CODEC_NONEMPTY.fieldOf("input_item").forGetter(RecipeMechanicalSqueezer::getInputIngredient),
                            RecipeSerializerSqueezer.CODEC_INGREDIENT_CHANCE_LIST.fieldOf("output_items").forGetter(r -> r.getOutputItems().stream().toList()),
                            ExtraCodecs.strictOptionalField(FluidStack.CODEC, "output_fluid").forGetter(RecipeMechanicalSqueezer::getOutputFluid),
                            Codec.INT.fieldOf("duration").forGetter(RecipeMechanicalSqueezer::getDuration)
                    )
                    .apply(builder, (inputIngredient, outputItemStacks, outputFluid, duration) -> {
                        // Validation
                        if (inputIngredient.isEmpty()) {
                            throw new JsonSyntaxException("An input item is required");
                        }
                        if (outputItemStacks.isEmpty() && outputFluid.isEmpty()) {
                            throw new JsonSyntaxException("An output item or fluid is required");
                        }
                        if (duration <= 0) {
                            throw new JsonSyntaxException("Durations must be higher than one tick");
                        }

                        return new RecipeMechanicalSqueezer(inputIngredient, NonNullList.copyOf(outputItemStacks), outputFluid, duration);
                    })
    );

    @Override
    public Codec<RecipeMechanicalSqueezer> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public RecipeMechanicalSqueezer fromNetwork(FriendlyByteBuf buffer) {
        // Input
        Ingredient inputIngredient = Ingredient.fromNetwork(buffer);

        // Output
        NonNullList<RecipeSqueezer.IngredientChance> outputItemStacks = NonNullList.create();
        int outputItemStacksCount = buffer.readInt();
        for (int i = 0; i < outputItemStacksCount; i++) {
            outputItemStacks.add(new RecipeSqueezer.IngredientChance(
                    RecipeSerializerHelpers.readItemStackOrItemStackIngredientChance(buffer)
            ));
        }
        Optional<FluidStack> outputFluid = RecipeSerializerHelpers.readOptionalFromNetwork(buffer, FluidStack::readFromPacket);

        // Other stuff
        int duration = buffer.readVarInt();

        return new RecipeMechanicalSqueezer(inputIngredient, outputItemStacks, outputFluid, duration);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RecipeMechanicalSqueezer recipe) {
        // Input
        recipe.getInputIngredient().toNetwork(buffer);

        // Output
        buffer.writeInt(recipe.getOutputItems().size());
        for (RecipeSqueezer.IngredientChance outputItem : recipe.getOutputItems()) {
            RecipeSerializerHelpers.writeItemStackOrItemStackIngredientChance(buffer, outputItem.getIngredientChance());
        }
        RecipeSerializerHelpers.writeOptionalToNetwork(buffer, recipe.getOutputFluid(), (b, value) -> value.writeToPacket(b));

        // Other stuff
        buffer.writeVarInt(recipe.getDuration());
    }
}
