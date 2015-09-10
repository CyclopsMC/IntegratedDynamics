package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the facade.
 * @author rubensworks
 */
public class ItemFacadeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemFacadeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemFacadeConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "facade",
                null,
                ItemFacade.class
        );
    }

}
