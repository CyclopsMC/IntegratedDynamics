package org.cyclops.integrateddynamics.component;

import net.minecraft.core.BlockPos;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class DataComponentWrenchTargetBlockPosConfig extends DataComponentConfigCommon<BlockPos, IntegratedDynamics> {

    public DataComponentWrenchTargetBlockPosConfig() {
        super(IntegratedDynamics._instance, "wrench_target_blockpos", builder -> builder
                .persistent(BlockPos.CODEC)
                .networkSynchronized(BlockPos.STREAM_CODEC));
    }
}
