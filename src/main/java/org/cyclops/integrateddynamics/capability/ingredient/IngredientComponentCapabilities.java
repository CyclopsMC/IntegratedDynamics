package org.cyclops.integrateddynamics.capability.ingredient;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.capability.IngredientComponentCapabilityAttacherAdapter;
import org.cyclops.commoncapabilities.api.ingredient.capability.IngredientComponentCapabilityAttacherManager;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.ingredient.capability.IIngredientComponentValueHandler;
import org.cyclops.integrateddynamics.api.ingredient.capability.IPositionedAddonsNetworkIngredientsHandler;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;

import java.util.Optional;

/**
 * Value handlers for ingredient components.
 * @author rubensworks
 */
@Deprecated // TODO: try to rm in next major, as we have IIngredientComponentHandler and IIngredientComponentHandlerRegistry that do the same
public class IngredientComponentCapabilities {

    public static final Identifier INGREDIENT_ITEMSTACK_NAME = Identifier.fromNamespaceAndPath("minecraft", "itemstack");
    public static final Identifier INGREDIENT_FLUIDSTACK_NAME = Identifier.fromNamespaceAndPath("minecraft", "fluidstack");
    public static final Identifier INGREDIENT_ENERGY_NAME = Identifier.fromNamespaceAndPath("minecraft", "energy");

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
            public ICapabilityProvider<IngredientComponent<Integer, Boolean>, Void, IPositionedAddonsNetworkIngredientsHandler<Integer, Boolean>> createCapabilityProvider(IngredientComponent<Integer, Boolean> ingredientComponent) {
                return new DefaultCapabilityProvider<>(network -> (Optional) network.getCapability(Capabilities.EnergyNetwork.NETWORK));
            }
        });
    }

}
