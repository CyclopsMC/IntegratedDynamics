package org.cyclops.integrateddynamics.capability.ingredient;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.capability.IngredientComponentCapabilityAttacherAdapter;
import org.cyclops.commoncapabilities.api.ingredient.capability.IngredientComponentCapabilityAttacherManager;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.ingredient.capability.IPositionedAddonsNetworkIngredientsHandler;

import java.util.Optional;

/**
 * Value handlers for ingredient components.
 * @author rubensworks
 */
public class IngredientComponentCapabilities {

    public static final Identifier INGREDIENT_ITEMSTACK_NAME = Identifier.fromNamespaceAndPath("minecraft", "itemstack");
    public static final Identifier INGREDIENT_FLUIDSTACK_NAME = Identifier.fromNamespaceAndPath("minecraft", "fluidstack");
    public static final Identifier INGREDIENT_ENERGY_NAME = Identifier.fromNamespaceAndPath("minecraft", "energy");

    public static void load() {
        IngredientComponentCapabilityAttacherManager attacherManager = new IngredientComponentCapabilityAttacherManager();

        // Network handler
        attacherManager.addAttacher(new IngredientComponentCapabilityAttacherAdapter<Integer, Boolean>(INGREDIENT_ENERGY_NAME, Capabilities.PositionedAddonsNetworkIngredientsHandler.INGREDIENT) {
            @Override
            public ICapabilityProvider<IngredientComponent<Integer, Boolean>, Void, IPositionedAddonsNetworkIngredientsHandler<Integer, Boolean>> createCapabilityProvider(IngredientComponent<Integer, Boolean> ingredientComponent) {
                return new DefaultCapabilityProvider<>(network -> (Optional) network.getCapability(Capabilities.EnergyNetwork.NETWORK));
            }
        });
    }

}
