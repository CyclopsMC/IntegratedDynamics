package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.extendedconfig.ItemClientConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the facade.
 * @author rubensworks
 */
public class ItemFacadeConfig extends ItemConfigCommon<IntegratedDynamics> {

    public ItemFacadeConfig() {
        super(
                IntegratedDynamics._instance,
                "facade",
                (eConfig, properties) -> new ItemFacade(properties)
        );
    }

    @Override
    public ItemClientConfig<IntegratedDynamics> constructItemClientConfig() {
        return new ItemFacadeConfigClient(this);
    }

}
