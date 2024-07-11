package org.cyclops.integrateddynamics.core.ingredient;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentHandler;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentHandlerRegistry;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeLong;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import javax.annotation.Nullable;

/**
 * Value handlers for ingredient components.
 * @author rubensworks
 */
public class IngredientComponentHandlers {

    public static final IIngredientComponentHandlerRegistry REGISTRY = constructRegistry();

    private static IIngredientComponentHandlerRegistry constructRegistry() {
        // This also allows this registry to be used outside of a minecraft environment.
        if(MinecraftHelpers.isModdedEnvironment()) {
            return IntegratedDynamics._instance.getRegistryManager().getRegistry(IIngredientComponentHandlerRegistry.class);
        } else {
            return IngredientComponentHandlerRegistry.getInstance();
        }
    }

    public static void load() {

    }

    public static void onIngredientComponentsPopulated(RegisterEvent event) {
        if (event.getRegistryKey().equals(IngredientComponent.REGISTRY.key())) {
            // Components are still loading here, so grab them by name
            IngredientComponent componentItem = IngredientComponent.REGISTRY.get(
                    ResourceLocation.parse("minecraft:itemstack"));
            IngredientComponent componentFluid = IngredientComponent.REGISTRY.get(
                    ResourceLocation.parse("minecraft:fluidstack"));
            IngredientComponent componentEnergy = IngredientComponent.REGISTRY.get(
                    ResourceLocation.parse("minecraft:energy"));

            REGISTRY.register(new IIngredientComponentHandler<ValueObjectTypeItemStack,
                    ValueObjectTypeItemStack.ValueItemStack, ItemStack, Integer>() {
                @Override
                public ValueObjectTypeItemStack getValueType() {
                    return ValueTypes.OBJECT_ITEMSTACK;
                }

                @Override
                public IngredientComponent<ItemStack, Integer> getComponent() {
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
            REGISTRY.register(new IIngredientComponentHandler<ValueObjectTypeFluidStack,
                    ValueObjectTypeFluidStack.ValueFluidStack, FluidStack, Integer>() {

                @Override
                public ValueObjectTypeFluidStack getValueType() {
                    return ValueTypes.OBJECT_FLUIDSTACK;
                }

                @Override
                public IngredientComponent<FluidStack, Integer> getComponent() {
                    return componentFluid;
                }

                @Override
                public ValueObjectTypeFluidStack.ValueFluidStack toValue(@Nullable FluidStack instance) {
                    return ValueObjectTypeFluidStack.ValueFluidStack.of(instance);
                }

                @Override
                @Nullable
                public FluidStack toInstance(ValueObjectTypeFluidStack.ValueFluidStack value) {
                    return value.getRawValue();
                }
            });
            REGISTRY.register(new IIngredientComponentHandler<ValueTypeLong, ValueTypeLong.ValueLong, Long, Boolean>() {

                @Override
                public ValueTypeLong getValueType() {
                    return ValueTypes.LONG;
                }

                @Override
                public IngredientComponent<Long, Boolean> getComponent() {
                    return componentEnergy;
                }

                @Override
                public ValueTypeLong.ValueLong toValue(@Nullable Long instance) {
                    return ValueTypeLong.ValueLong.of(instance);
                }

                @Nullable
                @Override
                public Long toInstance(ValueTypeLong.ValueLong value) {
                    return value.getRawValue();
                }

                @Override
                public Component toCompactString(ValueTypeLong.ValueLong ingredientValue) {
                    return getValueType().toCompactString(ingredientValue)
                            .append(" ")
                            .append(Component.translatable(L10NValues.GENERAL_ENERGY_UNIT));
                }
            });
        }
    }

}
