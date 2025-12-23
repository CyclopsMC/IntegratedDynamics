package org.cyclops.integrateddynamics.core.recipe.type;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RecipeSerializerSqueezerFacade implements RecipeSerializer<RecipeSqueezerFacade> {

    public static final MapCodec<RecipeSqueezerFacade> CODEC = RecordCodecBuilder.mapCodec(b -> b.point(RecipeSqueezerFacade.INSTANCE));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeSqueezerFacade> STREAM_CODEC = StreamCodec.unit(RecipeSqueezerFacade.INSTANCE);

    @Override
    public MapCodec<RecipeSqueezerFacade> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RecipeSqueezerFacade> streamCodec() {
        return STREAM_CODEC;
    }

}
