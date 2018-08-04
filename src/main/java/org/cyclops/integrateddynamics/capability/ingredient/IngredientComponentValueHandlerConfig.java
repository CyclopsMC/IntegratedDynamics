package org.cyclops.integrateddynamics.capability.ingredient;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.ingredient.capability.IIngredientComponentValueHandler;

/**
 * Config for the ingredient component value handler capability.
 * @author rubensworks
 *
 */
public class IngredientComponentValueHandlerConfig extends CapabilityConfig<IIngredientComponentValueHandler> {

    /**
     * The unique instance.
     */
    public static IngredientComponentValueHandlerConfig _instance;

    @CapabilityInject(IIngredientComponentValueHandler.class)
    public static Capability<IIngredientComponentValueHandler> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public IngredientComponentValueHandlerConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "ingredientComponentHandler",
                "Handles the translation between IngredientComponent instances and IValue",
                IIngredientComponentValueHandler.class,
                new DefaultCapabilityStorage<IIngredientComponentValueHandler>(),
                IngredientComponentValueHandlerItemStack.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
