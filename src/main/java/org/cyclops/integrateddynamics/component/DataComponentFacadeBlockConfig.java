package org.cyclops.integrateddynamics.component;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class DataComponentFacadeBlockConfig extends DataComponentConfig<BlockState> {

    public DataComponentFacadeBlockConfig() {
        super(IntegratedDynamics._instance, "facade_block", builder -> builder
                .persistent(BlockState.CODEC)
                .networkSynchronized(ByteBufCodecs.fromCodec(BlockState.CODEC)));
    }
}
