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
import org.cyclops.integrateddynamics.GeneralConfig;

import javax.annotation.Nullable;
import java.util.List;
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
    public static final Codec<List<RecipeSqueezer.IngredientChance>> CODEC_INGREDIENT_CHANCE_LIST = Codec.list(CODEC_INGREDIENT_CHANCE);

    public static final Codec<RecipeSqueezer> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(
                            Ingredient.CODEC_NONEMPTY.fieldOf("input_item").forGetter(RecipeSqueezer::getInputIngredient),
                            RecipeSerializerSqueezer.CODEC_INGREDIENT_CHANCE_LIST.fieldOf("output_items").forGetter(r -> r.getOutputItems().stream().toList()),
                            ExtraCodecs.strictOptionalField(FluidStack.CODEC, "output_fluid").forGetter(RecipeSqueezer::getOutputFluid)
                    )
                    .apply(builder, (inputIngredient, outputItemStacks, outputFluid) -> {
                        // Validation
                        if (inputIngredient.isEmpty()) {
                            throw new JsonSyntaxException("An input item is required");
                        }
                        if (outputItemStacks.isEmpty() && outputFluid.isEmpty()) {
                            throw new JsonSyntaxException("An output item or fluid is required");
                        }

                        return new RecipeSqueezer(inputIngredient, NonNullList.copyOf(outputItemStacks), outputFluid);
                    })
    );

    @Override
    public Codec<RecipeSqueezer> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public RecipeSqueezer fromNetwork(FriendlyByteBuf buffer) {
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

        return new RecipeSqueezer(inputIngredient, outputItemStacks, outputFluid);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RecipeSqueezer recipe) {
        // Input
        recipe.getInputIngredient().toNetwork(buffer);

        // Output
        buffer.writeInt(recipe.getOutputItems().size());
        for (RecipeSqueezer.IngredientChance outputItem : recipe.getOutputItems()) {
            RecipeSerializerHelpers.writeItemStackOrItemStackIngredientChance(buffer, outputItem.getIngredientChance());
        }
        RecipeSerializerHelpers.writeOptionalToNetwork(buffer, recipe.getOutputFluid(), (b, value) -> value.writeToPacket(b));
    }
}
