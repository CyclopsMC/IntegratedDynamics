package org.cyclops.integrateddynamics.capability.ingredient;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.ingredient.capability.IIngredientComponentValueHandler;

/**
 * Config for the ingredient component value handler capability.
 * @author rubensworks
 *
 */
public class IngredientComponentValueHandlerConfig extends CapabilityConfig<IIngredientComponentValueHandler> {

    @CapabilityInject(IIngredientComponentValueHandler.class)
    public static Capability<IIngredientComponentValueHandler> CAPABILITY = null;

    public IngredientComponentValueHandlerConfig() {
        super(
                CommonCapabilities._instance,
                "ingredientComponentHandler",
                IIngredientComponentValueHandler.class,
                new DefaultCapabilityStorage<IIngredientComponentValueHandler>(),
                () -> new IngredientComponentValueHandlerItemStack(IngredientComponent.ITEMSTACK)
        );
    }

}
