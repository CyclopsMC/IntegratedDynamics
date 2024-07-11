package org.cyclops.integrateddynamics.component;

import net.minecraft.core.Direction;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class DataComponentWrenchTargetDirectionConfig extends DataComponentConfig<Direction> {

    public DataComponentWrenchTargetDirectionConfig() {
        super(IntegratedDynamics._instance, "wrench_target_direction", builder -> builder
                .persistent(Direction.CODEC)
                .networkSynchronized(Direction.STREAM_CODEC));
    }
}
