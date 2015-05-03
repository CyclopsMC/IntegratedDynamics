package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for a wrench.
 * @author rubensworks
 */
public class ItemWrenchConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemWrenchConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemWrenchConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "wrench",
                null,
                ItemWrench.class
        );
    }

}
