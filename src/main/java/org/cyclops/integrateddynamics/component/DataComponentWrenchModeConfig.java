package org.cyclops.integrateddynamics.component;

import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.item.ItemWrench;

/**
 * @author rubensworks
 */
public class DataComponentWrenchModeConfig extends DataComponentConfigCommon<ItemWrench.Mode, IntegratedDynamics> {

    public DataComponentWrenchModeConfig() {
        super(IntegratedDynamics._instance, "wrench_mode", builder -> builder
                .persistent(ItemWrench.Mode.CODEC)
                .networkSynchronized(ItemWrench.Mode.STREAM_CODEC));
    }
}
