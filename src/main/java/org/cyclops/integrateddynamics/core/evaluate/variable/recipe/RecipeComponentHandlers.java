package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import com.google.common.collect.Iterables;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.cyclops.commoncapabilities.api.capability.recipehandler.FluidHandlerRecipeTarget;
import org.cyclops.commoncapabilities.api.capability.recipehandler.ItemHandlerRecipeTarget;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.recipe.IRecipeComponentHandler;
import org.cyclops.integrateddynamics.api.evaluate.variable.recipe.IRecipeComponentHandlerRegistry;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Collection of light level calculators for value types..
 * @author rubensworks
 */
public class RecipeComponentHandlers {

    public static final IRecipeComponentHandlerRegistry REGISTRY = constructRegistry();

    private static IRecipeComponentHandlerRegistry constructRegistry() {
        // This also allows this registry to be used outside of a minecraft environment.
        if(MinecraftHelpers.isModdedEnvironment()) {
            return IntegratedDynamics._instance.getRegistryManager().getRegistry(IRecipeComponentHandlerRegistry.class);
        } else {
            return RecipeComponentHandlerRegistry.getInstance();
        }
    }

    public static void load() {
        MinecraftForge.EVENT_BUS.register(RecipeComponentHandlers.class);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRecipeComponentsPopulated(RegistryEvent.Register event) {
        if (event.getRegistry() == RecipeComponent.REGISTRY) {
            // Components are still loading here, so grab them by name
            RecipeComponent componentItem = RecipeComponent.REGISTRY.getValue(
                    new ResourceLocation("minecraft:itemstack"));
            RecipeComponent componentFluid = RecipeComponent.REGISTRY.getValue(
                    new ResourceLocation("minecraft:fluidstack"));
            RecipeComponent componentEnergy = RecipeComponent.REGISTRY.getValue(
                    new ResourceLocation("minecraft:energy"));

            REGISTRY.register(new IRecipeComponentHandler<ValueObjectTypeItemStack,
                    ValueObjectTypeItemStack.ValueItemStack, ItemStack, ItemHandlerRecipeTarget,
                    RecipeComponent<ItemStack, ItemHandlerRecipeTarget>>() {
                @Override
                public ValueObjectTypeItemStack getValueType() {
                    return ValueTypes.OBJECT_ITEMSTACK;
                }

                @Override
                public RecipeComponent<ItemStack, ItemHandlerRecipeTarget> getComponent() {
                    return componentItem;
                }

                @Override
                public ValueObjectTypeItemStack.ValueItemStack toValue(ItemStack instance) {
                    return ValueObjectTypeItemStack.ValueItemStack.of(instance);
                }

                @Override
                public ItemStack toInstance(ValueObjectTypeItemStack.ValueItemStack value) {
                    return value.getRawValue();
                }
            });
            REGISTRY.register(new IRecipeComponentHandler<ValueObjectTypeFluidStack,
                    ValueObjectTypeFluidStack.ValueFluidStack, FluidStack, FluidHandlerRecipeTarget,
                    RecipeComponent<FluidStack, FluidHandlerRecipeTarget>>() {

                @Override
                public ValueObjectTypeFluidStack getValueType() {
                    return ValueTypes.OBJECT_FLUIDSTACK;
                }

                @Override
                public RecipeComponent<FluidStack, FluidHandlerRecipeTarget> getComponent() {
                    return componentFluid;
                }

                @Override
                public ValueObjectTypeFluidStack.ValueFluidStack toValue(@Nullable FluidStack instance) {
                    return ValueObjectTypeFluidStack.ValueFluidStack.of(instance);
                }

                @Override
                @Nullable
                public FluidStack toInstance(ValueObjectTypeFluidStack.ValueFluidStack value) {
                    return value.getRawValue().orNull();
                }
            });
            REGISTRY.register(new IRecipeComponentHandler<ValueTypeInteger, ValueTypeInteger.ValueInteger, Integer, IEnergyStorage, RecipeComponent<Integer, IEnergyStorage>>() {
                @Override
                public ValueTypeInteger getValueType() {
                    return ValueTypes.INTEGER;
                }

                @Override
                public RecipeComponent<Integer, IEnergyStorage> getComponent() {
                    return componentEnergy;
                }

                @Override
                public ValueTypeInteger.ValueInteger toValue(@Nullable Integer instance) {
                    return ValueTypeInteger.ValueInteger.of(instance);
                }

                @Nullable
                @Override
                public Integer toInstance(ValueTypeInteger.ValueInteger value) {
                    return value.getRawValue();
                }

                @Override
                public String toCompactString(List<ValueTypeInteger.ValueInteger> ingredientValue) {
                    String value = getValueType().toCompactString(Iterables.getFirst(ingredientValue,
                            getValueType().getDefault()));
                    value += " " + L10NHelpers.localize(L10NValues.GENERAL_ENERGY_UNIT);
                    if (ingredientValue.size() > 1) value += "+";
                    return value;
                }
            });
        }
    }

}
