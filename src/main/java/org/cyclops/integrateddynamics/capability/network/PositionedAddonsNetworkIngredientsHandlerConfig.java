package org.cyclops.integrateddynamics.capability.network;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.ingredient.capability.DefaultPositionedAddonsNetworkIngredientsHandler;
import org.cyclops.integrateddynamics.api.ingredient.capability.IPositionedAddonsNetworkIngredientsHandler;

/**
 * Config for the positioned addons network handler capability.
 * @author rubensworks
 */
public class PositionedAddonsNetworkIngredientsHandlerConfig extends CapabilityConfig<IPositionedAddonsNetworkIngredientsHandler> {

    @CapabilityInject(IPositionedAddonsNetworkIngredientsHandler.class)
    public static Capability<IPositionedAddonsNetworkIngredientsHandler> CAPABILITY = null;

    public PositionedAddonsNetworkIngredientsHandlerConfig() {
        super(
                CommonCapabilities._instance,
                "positioned_addons_network_ingredients_handler",
                IPositionedAddonsNetworkIngredientsHandler.class,
                new DefaultCapabilityStorage<IPositionedAddonsNetworkIngredientsHandler>(),
                () -> new DefaultPositionedAddonsNetworkIngredientsHandler(null)
        );
    }

}
