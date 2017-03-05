package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the infobook.
 * @author rubensworks
 */
public class ItemOnTheDynamicsOfIntegrationConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemOnTheDynamicsOfIntegrationConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemOnTheDynamicsOfIntegrationConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "on_the_dynamics_of_integration",
                null,
                ItemOnTheDynamicsOfIntegration.class
        );
    }

}
