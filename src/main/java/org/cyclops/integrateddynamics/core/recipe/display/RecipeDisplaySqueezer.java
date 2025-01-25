package org.cyclops.integrateddynamics.core.recipe.display;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.core.helper.Codecs;

import java.util.List;

/**
 * @author rubensworks
 */
public record RecipeDisplaySqueezer(
        SlotDisplay inputIngredient,
        List<Pair<? extends SlotDisplay, Float>> outputItems,
        FluidStack outputFluid,
        SlotDisplay craftingStation,
        int duration
) implements RecipeDisplay {

    public static final MapCodec<RecipeDisplaySqueezer> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            SlotDisplay.CODEC.fieldOf("input_ingredient").forGetter(RecipeDisplaySqueezer::inputIngredient),
                            Codecs.SLOT_DISPLAY_CHANCE.listOf().fieldOf("output_ingredients").forGetter(RecipeDisplaySqueezer::outputItems),
                            FluidStack.CODEC.fieldOf("output_fluid").forGetter(RecipeDisplaySqueezer::outputFluid),
                            SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(RecipeDisplaySqueezer::craftingStation),
                            Codec.INT.fieldOf("duration").forGetter(RecipeDisplaySqueezer::duration)
                    )
                    .apply(instance, RecipeDisplaySqueezer::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeDisplaySqueezer> STREAM_CODEC = StreamCodec.composite(
            SlotDisplay.STREAM_CODEC,
            RecipeDisplaySqueezer::inputIngredient,
            Codecs.STREAM_SLOT_DISPLAY_CHANCE.apply(ByteBufCodecs.list()),
            RecipeDisplaySqueezer::outputItems,
            FluidStack.OPTIONAL_STREAM_CODEC,
            RecipeDisplaySqueezer::outputFluid,
            SlotDisplay.STREAM_CODEC,
            RecipeDisplaySqueezer::craftingStation,
            ByteBufCodecs.VAR_INT,
            RecipeDisplaySqueezer::duration,
            RecipeDisplaySqueezer::new
    );
    public static final Type<RecipeDisplaySqueezer> TYPE = new Type<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public SlotDisplay result() {
        if (outputItems().isEmpty()) {
            FluidStack fluidStack = outputFluid();
            return new SlotDisplay.ItemSlotDisplay(outputFluid().getFluidType().getBucket(fluidStack).getItem());
        }
        return outputItems().getFirst().getLeft();
    }

    @Override
    public Type<? extends RecipeDisplay> type() {
        return TYPE;
    }

    @Override
    public boolean isEnabled(FeatureFlagSet featureFlagSet) {
        return this.inputIngredient.isEnabled(featureFlagSet)
                && this.outputItems.stream().allMatch(pair -> pair.getLeft().isEnabled(featureFlagSet))
                && RecipeDisplay.super.isEnabled(featureFlagSet);
    }
}
