package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the portable logic programmer.
 * @author rubensworks
 */
public class ItemPortableLogicProgrammerConfig extends ItemConfigCommon<IModBase> {

    public ItemPortableLogicProgrammerConfig() {
        super(
                IntegratedDynamics._instance,
                "portable_logic_programmer",
                (eConfig, properties) -> new ItemPortableLogicProgrammer(properties)
        );
    }

}
