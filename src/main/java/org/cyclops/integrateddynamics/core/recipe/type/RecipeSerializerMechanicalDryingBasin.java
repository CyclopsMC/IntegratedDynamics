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
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import org.cyclops.cyclopscore.helper.RecipeSerializerHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;

/**
 * Recipe serializer for mechanical drying basin recipes
 * @author rubensworks
 */
public class RecipeSerializerMechanicalDryingBasin {

    public static final MapCodec<RecipeMechanicalDryingBasin> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            Ingredient.CODEC.optionalFieldOf("input_item").forGetter(RecipeDryingBasin::getInputIngredient),
                            FluidStackTemplate.CODEC.optionalFieldOf("input_fluid").forGetter(RecipeDryingBasin::getInputFluidTemplate),
                            RecipeSerializerHelpers.getCodecItemStackTemplateOrTag(() -> GeneralConfig.recipeTagOutputModPriorities).optionalFieldOf("output_item").forGetter(RecipeDryingBasin::getOutputItemTemplate),
                            FluidStackTemplate.CODEC.optionalFieldOf("output_fluid").forGetter(RecipeDryingBasin::getOutputFluidTemplate),
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

                        return new RecipeMechanicalDryingBasin(inputItem, inputFluid, outputItem, outputFluid, duration);
                    })
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeMechanicalDryingBasin> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), RecipeDryingBasin::getInputIngredient,
            ByteBufCodecs.optional(FluidStackTemplate.STREAM_CODEC), RecipeDryingBasin::getInputFluidTemplate,
            ByteBufCodecs.optional(RecipeSerializerHelpers.STREAM_CODEC_ITEMSTACKTEMPLATE_OR_TAG), RecipeDryingBasin::getOutputItemTemplate,
            ByteBufCodecs.optional(FluidStackTemplate.STREAM_CODEC), RecipeDryingBasin::getOutputFluidTemplate,
            ByteBufCodecs.INT, RecipeDryingBasin::getDuration,
            RecipeMechanicalDryingBasin::new
    );
    public static final RecipeSerializer<RecipeMechanicalDryingBasin> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

}
