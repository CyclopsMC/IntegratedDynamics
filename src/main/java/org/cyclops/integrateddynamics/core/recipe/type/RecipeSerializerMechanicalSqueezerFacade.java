package org.cyclops.integrateddynamics.core.recipe.type;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RecipeSerializerMechanicalSqueezerFacade implements RecipeSerializer<RecipeMechanicalSqueezerFacade> {

    public static final MapCodec<RecipeMechanicalSqueezerFacade> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            Codec.INT.fieldOf("duration").forGetter(RecipeMechanicalSqueezerFacade::getDuration)
                    )
                    .apply(builder, (duration) -> {
                        // Validation
                        if (duration <= 0) {
                            throw new JsonSyntaxException("Durations must be higher than one tick");
                        }
                        return new RecipeMechanicalSqueezerFacade(duration);
                    })
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeMechanicalSqueezerFacade> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, RecipeMechanicalSqueezerFacade::getDuration,
            RecipeMechanicalSqueezerFacade::new
    );

    @Override
    public MapCodec<RecipeMechanicalSqueezerFacade> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RecipeMechanicalSqueezerFacade> streamCodec() {
        return STREAM_CODEC;
    }

}
