package org.cyclops.integrateddynamics.core.recipe.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;

/**
 * Recipe serializer for energy container combinations.
 * @author rubensworks
 */
public class RecipeSerializerEnergyContainerCombination implements RecipeSerializer<RecipeEnergyContainerCombination> {

    public static final Codec<RecipeEnergyContainerCombination> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(
                            Ingredient.CODEC_NONEMPTY.fieldOf("item").forGetter(RecipeEnergyContainerCombination::getBatteryItem),
                            Codec.INT.fieldOf("maxCapacity").forGetter(RecipeEnergyContainerCombination::getMaxCapacity)
                    )
                    .apply(builder, RecipeEnergyContainerCombination::new)
    );

    @Override
    public Codec<RecipeEnergyContainerCombination> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public RecipeEnergyContainerCombination fromNetwork(FriendlyByteBuf buffer) {
        Ingredient inputIngredient = Ingredient.fromNetwork(buffer);
        int maxCapacity = buffer.readInt();
        return new RecipeEnergyContainerCombination(inputIngredient, maxCapacity);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RecipeEnergyContainerCombination recipe) {
        recipe.getBatteryItem().toNetwork(buffer);
        buffer.writeInt(recipe.getMaxCapacity());
    }
}
