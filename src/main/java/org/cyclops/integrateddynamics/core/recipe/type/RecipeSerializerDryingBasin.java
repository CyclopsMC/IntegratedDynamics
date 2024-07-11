package org.cyclops.integrateddynamics.core.recipe.type;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.helper.RecipeSerializerHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;

/**
 * Recipe serializer for drying basin recipes
 * @author rubensworks
 */
public class RecipeSerializerDryingBasin implements RecipeSerializer<RecipeDryingBasin> {

    public static final MapCodec<RecipeDryingBasin> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            Ingredient.CODEC_NONEMPTY.optionalFieldOf("input_item").forGetter(RecipeDryingBasin::getInputIngredient),
                            FluidStack.CODEC.optionalFieldOf("input_fluid").forGetter(RecipeDryingBasin::getInputFluid),
                            RecipeSerializerHelpers.getCodecItemStackOrTag(() -> GeneralConfig.recipeTagOutputModPriorities).optionalFieldOf("output_item").forGetter(RecipeDryingBasin::getOutputItem),
                            FluidStack.CODEC.optionalFieldOf("output_fluid").forGetter(RecipeDryingBasin::getOutputFluid),
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
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeDryingBasin> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), RecipeDryingBasin::getInputIngredient,
            ByteBufCodecs.optional(FluidStack.STREAM_CODEC), RecipeDryingBasin::getInputFluid,
            ByteBufCodecs.optional(RecipeSerializerHelpers.STREAM_CODEC_ITEMSTACK_OR_TAG), RecipeDryingBasin::getOutputItem,
            ByteBufCodecs.optional(FluidStack.STREAM_CODEC), RecipeDryingBasin::getOutputFluid,
            ByteBufCodecs.INT, RecipeDryingBasin::getDuration,
            RecipeDryingBasin::new
    );

    @Override
    public MapCodec<RecipeDryingBasin> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RecipeDryingBasin> streamCodec() {
        return STREAM_CODEC;
    }
}
