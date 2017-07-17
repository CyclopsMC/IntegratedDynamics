package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.configurable.ConfigurableItem;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Logic Director.
 * @author rubensworks
 *
 */
public class ItemLogicDirectorConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemLogicDirectorConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemLogicDirectorConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "logic_director",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new ConfigurableItem(this);
    }
    
}
