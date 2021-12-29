package org.cyclops.integrateddynamics.capability.ingredient;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.integrateddynamics.api.ingredient.capability.IIngredientComponentValueHandler;

/**
 * Config for the ingredient component value handler capability.
 * @author rubensworks
 *
 */
public class IngredientComponentValueHandlerConfig extends CapabilityConfig<IIngredientComponentValueHandler> {

    public static Capability<IIngredientComponentValueHandler> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public IngredientComponentValueHandlerConfig() {
        super(
                CommonCapabilities._instance,
                "ingredientComponentHandler",
                IIngredientComponentValueHandler.class
        );
    }

}
