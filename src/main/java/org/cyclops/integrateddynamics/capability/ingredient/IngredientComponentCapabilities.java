package org.cyclops.integrateddynamics.capability.ingredient;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.capability.IngredientComponentCapabilityAttacherAdapter;
import org.cyclops.commoncapabilities.api.ingredient.capability.IngredientComponentCapabilityAttacherManager;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.capability.network.EnergyNetworkConfig;
import org.cyclops.integrateddynamics.capability.network.PositionedAddonsNetworkIngredientsHandlerConfig;

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
        ResourceLocation capabilityIngredientComponentValueHandler = new ResourceLocation(Reference.MOD_ID, "ingredientComponentValueHandler");
        attacherManager.addAttacher(new IngredientComponentCapabilityAttacherAdapter<ItemStack, Integer>(INGREDIENT_ITEMSTACK_NAME, capabilityIngredientComponentValueHandler) {
            @Override
            public ICapabilityProvider createCapabilityProvider(IngredientComponent<ItemStack, Integer> ingredientComponent) {
                return new DefaultCapabilityProvider<>(() -> IngredientComponentValueHandlerConfig.CAPABILITY,
                        new IngredientComponentValueHandlerItemStack(ingredientComponent));
            }
        });
        attacherManager.addAttacher(new IngredientComponentCapabilityAttacherAdapter<FluidStack, Integer>(INGREDIENT_FLUIDSTACK_NAME, capabilityIngredientComponentValueHandler) {
            @Override
            public ICapabilityProvider createCapabilityProvider(IngredientComponent<FluidStack, Integer> ingredientComponent) {
                return new DefaultCapabilityProvider<>(() -> IngredientComponentValueHandlerConfig.CAPABILITY,
                        new IngredientComponentValueHandlerFluidStack(ingredientComponent));
            }
        });
        attacherManager.addAttacher(new IngredientComponentCapabilityAttacherAdapter<Integer, Boolean>(INGREDIENT_ENERGY_NAME, capabilityIngredientComponentValueHandler) {
            @Override
            public ICapabilityProvider createCapabilityProvider(IngredientComponent<Integer, Boolean> ingredientComponent) {
                return new DefaultCapabilityProvider<>(() -> IngredientComponentValueHandlerConfig.CAPABILITY,
                        new IngredientComponentValueHandlerEnergy(ingredientComponent));
            }
        });

        // Network handler
        ResourceLocation networkHandler = new ResourceLocation(Reference.MOD_ID, "networkHandler");
        attacherManager.addAttacher(new IngredientComponentCapabilityAttacherAdapter<Integer, Boolean>(INGREDIENT_ENERGY_NAME, networkHandler) {
            @Override
            public ICapabilityProvider createCapabilityProvider(IngredientComponent<Integer, Boolean> ingredientComponent) {
                return new DefaultCapabilityProvider<>(() -> PositionedAddonsNetworkIngredientsHandlerConfig.CAPABILITY,
                        (network) -> network.getCapability(EnergyNetworkConfig.CAPABILITY));
            }
        });
    }

}
