package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the labeller.
 * @author rubensworks
 */
public class ItemLabellerConfig extends ItemConfigCommon<IModBase> {

    public ItemLabellerConfig() {
        super(
                IntegratedDynamics._instance,
                "labeller",
                (eConfig, properties) -> new ItemLabeller(properties)
        );
    }

}
