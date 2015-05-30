package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for a variable item.
 * @author rubensworks
 */
public class ItemVariableConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemVariableConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemVariableConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "variable",
                null,
                ItemVariable.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }
}
