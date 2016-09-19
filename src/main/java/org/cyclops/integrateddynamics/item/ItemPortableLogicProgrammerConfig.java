package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the portable logic programmer.
 * @author rubensworks
 */
public class ItemPortableLogicProgrammerConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemPortableLogicProgrammerConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemPortableLogicProgrammerConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "portableLogicProgrammer",
                null,
                ItemPortableLogicProgrammer.class
        );
    }

}
