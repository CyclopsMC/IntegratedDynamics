package org.cyclops.integrateddynamics.capability.network;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.integrateddynamics.api.ingredient.capability.IPositionedAddonsNetworkIngredientsHandler;

/**
 * Config for the positioned addons network handler capability.
 * @author rubensworks
 */
public class PositionedAddonsNetworkIngredientsHandlerConfig extends CapabilityConfig<IPositionedAddonsNetworkIngredientsHandler> {

    public static Capability<IPositionedAddonsNetworkIngredientsHandler> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public PositionedAddonsNetworkIngredientsHandlerConfig() {
        super(
                CommonCapabilities._instance,
                "positioned_addons_network_ingredients_handler",
                IPositionedAddonsNetworkIngredientsHandler.class
        );
    }

}
