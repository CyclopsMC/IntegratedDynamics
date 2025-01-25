package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for a wrench.
 * @author rubensworks
 */
public class ItemWrenchConfig extends ItemConfigCommon<IModBase> {

    public ItemWrenchConfig() {
        super(
                IntegratedDynamics._instance,
                "wrench",
                (eConfig, properties) -> new ItemWrench(properties
                        .stacksTo(1))
        );
    }

}
