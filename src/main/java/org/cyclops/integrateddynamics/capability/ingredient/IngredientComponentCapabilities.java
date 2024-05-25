package org.cyclops.integrateddynamics.capability.ingredient;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.capability.IngredientComponentCapabilityAttacherAdapter;
import org.cyclops.commoncapabilities.api.ingredient.capability.IngredientComponentCapabilityAttacherManager;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.ingredient.capability.IIngredientComponentValueHandler;
import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;

/**
 * Value handlers for ingredient components.
 * @author rubensworks
 */
public class IngredientComponentCapabilities {

    public static final ResourceLocation INGREDIENT_ITEMSTACK_NAME = new ResourceLocation("minecraft", "itemstack");
    public static final ResourceLocation INGREDIENT_FLUIDSTACK_NAME = new ResourceLocation("minecraft", "fluidstack");
    public static final ResourceLocation INGREDIENT_ENERGY_NAME = new ResourceLocation("minecraft", "energy");

    public static void load() {
        IngredientComponentCapabilityAttacherManager attacherManager = new IngredientComponentCapabilityAttacherManager();

        // Value handlers
        attacherManager.addAttacher(new IngredientComponentCapabilityAttacherAdapter<ItemStack, Integer>(INGREDIENT_ITEMSTACK_NAME, Capabilities.IngredientComponentValueHandler.INGREDIENT) {
            @Override
            public ICapabilityProvider<IngredientComponent<?, ?>, Void, IIngredientComponentValueHandler<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack, ItemStack, Integer>>
            createCapabilityProvider(IngredientComponent<ItemStack, Integer> ingredientComponent) {
                return (object, context) -> new IngredientComponentValueHandlerItemStack(ingredientComponent);
            }
        });
        attacherManager.addAttacher(new IngredientComponentCapabilityAttacherAdapter<FluidStack, Integer>(INGREDIENT_FLUIDSTACK_NAME, Capabilities.IngredientComponentValueHandler.INGREDIENT) {
            @Override
            public ICapabilityProvider<IngredientComponent<?, ?>, Void, IIngredientComponentValueHandler<ValueObjectTypeFluidStack, ValueObjectTypeFluidStack.ValueFluidStack, FluidStack, Integer>>
            createCapabilityProvider(IngredientComponent<FluidStack, Integer> ingredientComponent) {
                return (object, context) -> new IngredientComponentValueHandlerFluidStack(ingredientComponent);
            }
        });
        attacherManager.addAttacher(new IngredientComponentCapabilityAttacherAdapter<Integer, Boolean>(INGREDIENT_ENERGY_NAME, Capabilities.IngredientComponentValueHandler.INGREDIENT) {
            @Override
            public ICapabilityProvider<IngredientComponent<?, ?>, Void, IIngredientComponentValueHandler<ValueTypeInteger, ValueTypeInteger.ValueInteger, Integer, Boolean>>
            createCapabilityProvider(IngredientComponent<Integer, Boolean> ingredientComponent) {
                return (object, context) -> new IngredientComponentValueHandlerEnergy(ingredientComponent);
            }
        });

        // Network handler
        attacherManager.addAttacher(new IngredientComponentCapabilityAttacherAdapter<Integer, Boolean>(INGREDIENT_ENERGY_NAME, Capabilities.PositionedAddonsNetworkIngredientsHandler.INGREDIENT) {
            @Override
            public ICapabilityProvider<INetwork, Void, IEnergyNetwork> createCapabilityProvider(IngredientComponent<Integer, Boolean> ingredientComponent) {
                return (network, context) -> network.getCapability(Capabilities.EnergyNetwork.NETWORK).orElse(null);
            }
        });
    }

}
