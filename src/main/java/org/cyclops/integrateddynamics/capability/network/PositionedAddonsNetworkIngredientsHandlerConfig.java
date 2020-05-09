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

    /**
     * The unique instance.
     */
    public static PositionedAddonsNetworkIngredientsHandlerConfig _instance;

    @CapabilityInject(IPositionedAddonsNetworkIngredientsHandler.class)
    public static Capability<IPositionedAddonsNetworkIngredientsHandler> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public PositionedAddonsNetworkIngredientsHandlerConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "energy_network",
                "A capability for networks that can hold energy.",
                IPositionedAddonsNetworkIngredientsHandler.class,
                new DefaultCapabilityStorage<IPositionedAddonsNetworkIngredientsHandler>(),
                DefaultPositionedAddonsNetworkIngredientsHandler.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
