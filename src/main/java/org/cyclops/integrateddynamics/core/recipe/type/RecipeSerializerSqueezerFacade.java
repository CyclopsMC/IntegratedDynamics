package org.cyclops.integrateddynamics.core.recipe.type;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RecipeSerializerSqueezerFacade {

    public static final MapCodec<RecipeSqueezerFacade> CODEC = RecordCodecBuilder.mapCodec(b -> b.point(RecipeSqueezerFacade.INSTANCE));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeSqueezerFacade> STREAM_CODEC = StreamCodec.unit(RecipeSqueezerFacade.INSTANCE);
    public static final RecipeSerializer<RecipeSqueezerFacade> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

}
