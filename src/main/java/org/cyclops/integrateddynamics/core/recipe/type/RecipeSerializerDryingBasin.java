package org.cyclops.integrateddynamics.core.recipe.type;

import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.helper.RecipeSerializerHelpers;
import org.cyclops.cyclopscore.recipe.ItemStackFromIngredient;
import org.cyclops.integrateddynamics.GeneralConfig;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Recipe serializer for drying basin recipes
 * @author rubensworks
 */
public class RecipeSerializerDryingBasin implements RecipeSerializer<RecipeDryingBasin> {

    public static final Codec<RecipeDryingBasin> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(
                            ExtraCodecs.strictOptionalField(Ingredient.CODEC_NONEMPTY, "input_item").forGetter(RecipeDryingBasin::getInputIngredient),
                            ExtraCodecs.strictOptionalField(FluidStack.CODEC, "input_fluid").forGetter(RecipeDryingBasin::getInputFluid),
                            ExtraCodecs.strictOptionalField(RecipeSerializerHelpers.getCodecItemStackOrTag(() -> GeneralConfig.recipeTagOutputModPriorities), "output_item").forGetter(RecipeDryingBasin::getOutputItem),
                            ExtraCodecs.strictOptionalField(FluidStack.CODEC, "output_fluid").forGetter(RecipeDryingBasin::getOutputFluid),
                            Codec.INT.fieldOf("duration").forGetter(RecipeDryingBasin::getDuration)
                    )
                    .apply(builder, (inputItem, inputFluid, outputItem, outputFluid, duration) -> {
                        // Validation
                        if (inputItem.isEmpty() && inputFluid.isEmpty()) {
                            throw new JsonSyntaxException("An input item or fluid is required");
                        }
                        if (outputItem.isEmpty() && outputFluid.isEmpty()) {
                            throw new JsonSyntaxException("An output item or fluid is required");
                        }
                        if (inputFluid.isPresent() && outputFluid.isPresent()) {
                            throw new JsonSyntaxException("Can't have both an input and output fluid");
                        }
                        if (duration <= 0) {
                            throw new JsonSyntaxException("Durations must be higher than one tick");
                        }

                        return new RecipeDryingBasin(inputItem, inputFluid, outputItem, outputFluid, duration);
                    })
    );

    @Override
    public Codec<RecipeDryingBasin> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public RecipeDryingBasin fromNetwork(FriendlyByteBuf buffer) {
        // Input
        Optional<Ingredient> inputIngredient = RecipeSerializerHelpers.readOptionalFromNetwork(buffer, Ingredient::fromNetwork);
        Optional<FluidStack> inputFluid = RecipeSerializerHelpers.readOptionalFromNetwork(buffer, FluidStack::readFromPacket);

        // Output
        Optional<Either<ItemStack, ItemStackFromIngredient>> outputItem = RecipeSerializerHelpers.readOptionalFromNetwork(buffer, RecipeSerializerHelpers::readItemStackOrItemStackIngredient);
        Optional<FluidStack> outputFluid = RecipeSerializerHelpers.readOptionalFromNetwork(buffer, FluidStack::readFromPacket);

        // Other stuff
        int duration = buffer.readVarInt();

        return new RecipeDryingBasin(inputIngredient, inputFluid, outputItem, outputFluid, duration);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RecipeDryingBasin recipe) {
        // Input
        RecipeSerializerHelpers.writeOptionalToNetwork(buffer, recipe.getInputIngredient(), (b, value) -> value.toNetwork(b));
        RecipeSerializerHelpers.writeOptionalToNetwork(buffer, recipe.getInputFluid(), (b, value) -> value.writeToPacket(b));

        // Output
        RecipeSerializerHelpers.writeOptionalToNetwork(buffer, recipe.getOutputItem(), RecipeSerializerHelpers::writeItemStackOrItemStackIngredient);
        RecipeSerializerHelpers.writeOptionalToNetwork(buffer, recipe.getOutputFluid(), (b, value) -> value.writeToPacket(b));

        // Other stuff
        buffer.writeVarInt(recipe.getDuration());
    }
}
