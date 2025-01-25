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

/**
 * @author rubensworks
 */
public record RecipeDisplayDryingBasin(
        SlotDisplay inputIngredient,
        FluidStack inputFluid,
        SlotDisplay outputItem,
        FluidStack outputFluid,
        SlotDisplay craftingStation,
        int duration
) implements RecipeDisplay {

    public static final MapCodec<RecipeDisplayDryingBasin> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            SlotDisplay.CODEC.fieldOf("input_ingredient").forGetter(RecipeDisplayDryingBasin::inputIngredient),
                            FluidStack.CODEC.fieldOf("input_fluid").forGetter(RecipeDisplayDryingBasin::inputFluid),
                            SlotDisplay.CODEC.fieldOf("output_ingredient").forGetter(RecipeDisplayDryingBasin::outputItem),
                            FluidStack.CODEC.fieldOf("output_fluid").forGetter(RecipeDisplayDryingBasin::outputFluid),
                            SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(RecipeDisplayDryingBasin::craftingStation),
                            Codec.INT.fieldOf("duration").forGetter(RecipeDisplayDryingBasin::duration)
                    )
                    .apply(instance, RecipeDisplayDryingBasin::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeDisplayDryingBasin> STREAM_CODEC = StreamCodec.composite(
            SlotDisplay.STREAM_CODEC,
            RecipeDisplayDryingBasin::inputIngredient,
            FluidStack.OPTIONAL_STREAM_CODEC,
            RecipeDisplayDryingBasin::inputFluid,
            SlotDisplay.STREAM_CODEC,
            RecipeDisplayDryingBasin::outputItem,
            FluidStack.OPTIONAL_STREAM_CODEC,
            RecipeDisplayDryingBasin::outputFluid,
            SlotDisplay.STREAM_CODEC,
            RecipeDisplayDryingBasin::craftingStation,
            ByteBufCodecs.VAR_INT,
            RecipeDisplayDryingBasin::duration,
            RecipeDisplayDryingBasin::new
    );
    public static final RecipeDisplay.Type<RecipeDisplayDryingBasin> TYPE = new RecipeDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public SlotDisplay result() {
        if (outputItem() == SlotDisplay.Empty.INSTANCE) {
            FluidStack fluidStack = outputFluid();
            return new SlotDisplay.ItemSlotDisplay(outputFluid().getFluidType().getBucket(fluidStack).getItem());
        }
        return outputItem();
    }

    @Override
    public Type<? extends RecipeDisplay> type() {
        return TYPE;
    }

    @Override
    public boolean isEnabled(FeatureFlagSet featureFlagSet) {
        return this.inputIngredient.isEnabled(featureFlagSet)
                && this.outputItem().isEnabled(featureFlagSet)
                && RecipeDisplay.super.isEnabled(featureFlagSet);
    }
}
