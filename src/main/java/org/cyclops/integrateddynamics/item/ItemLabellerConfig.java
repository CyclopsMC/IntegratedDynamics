package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the labeller.
 * @author rubensworks
 */
public class ItemLabellerConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemLabellerConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemLabellerConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "labeller",
                null,
                ItemLabeller.class
        );
    }

}
