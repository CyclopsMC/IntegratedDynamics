package org.cyclops.integrateddynamics.component;

import net.minecraft.core.BlockPos;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class DataComponentWrenchTargetBlockPosConfig extends DataComponentConfig<BlockPos> {

    public DataComponentWrenchTargetBlockPosConfig() {
        super(IntegratedDynamics._instance, "wrench_target_blockpos", builder -> builder
                .persistent(BlockPos.CODEC)
                .networkSynchronized(BlockPos.STREAM_CODEC));
    }
}
