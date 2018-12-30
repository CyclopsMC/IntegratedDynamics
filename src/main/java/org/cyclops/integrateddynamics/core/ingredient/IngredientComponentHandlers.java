package org.cyclops.integrateddynamics.core.ingredient;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentHandler;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentHandlerRegistry;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
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
        MinecraftForge.EVENT_BUS.register(IngredientComponentHandlers.class);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onIngredientComponentsPopulated(RegistryEvent.Register event) {
        if (event.getRegistry() == IngredientComponent.REGISTRY) {
            // Components are still loading here, so grab them by name
            IngredientComponent componentItem = IngredientComponent.REGISTRY.getValue(
                    new ResourceLocation("minecraft:itemstack"));
            IngredientComponent componentFluid = IngredientComponent.REGISTRY.getValue(
                    new ResourceLocation("minecraft:fluidstack"));
            IngredientComponent componentEnergy = IngredientComponent.REGISTRY.getValue(
                    new ResourceLocation("minecraft:energy"));

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
                    return value.getRawValue().orNull();
                }
            });
            REGISTRY.register(new IIngredientComponentHandler<ValueTypeInteger, ValueTypeInteger.ValueInteger, Integer, Boolean>() {
                @Override
                public ValueTypeInteger getValueType() {
                    return ValueTypes.INTEGER;
                }

                @Override
                public IngredientComponent<Integer, Boolean> getComponent() {
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
                public String toCompactString(ValueTypeInteger.ValueInteger ingredientValue) {
                    String value = getValueType().toCompactString(ingredientValue);
                    value += " " + L10NHelpers.localize(L10NValues.GENERAL_ENERGY_UNIT);
                    return value;
                }
            });
        }
    }

}
