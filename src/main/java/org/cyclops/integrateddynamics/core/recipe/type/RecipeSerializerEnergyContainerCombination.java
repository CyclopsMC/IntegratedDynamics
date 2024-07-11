package org.cyclops.integrateddynamics.core.recipe.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Recipe serializer for energy container combinations.
 * @author rubensworks
 */
public class RecipeSerializerEnergyContainerCombination implements RecipeSerializer<RecipeEnergyContainerCombination> {

    public static final MapCodec<RecipeEnergyContainerCombination> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            Ingredient.CODEC_NONEMPTY.fieldOf("item").forGetter(RecipeEnergyContainerCombination::getBatteryItem),
                            Codec.INT.fieldOf("maxCapacity").forGetter(RecipeEnergyContainerCombination::getMaxCapacity)
                    )
                    .apply(builder, RecipeEnergyContainerCombination::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeEnergyContainerCombination> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, RecipeEnergyContainerCombination::getBatteryItem,
            ByteBufCodecs.INT, RecipeEnergyContainerCombination::getMaxCapacity,
            RecipeEnergyContainerCombination::new
    );

    @Override
    public MapCodec<RecipeEnergyContainerCombination> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RecipeEnergyContainerCombination> streamCodec() {
        return STREAM_CODEC;
    }
}
