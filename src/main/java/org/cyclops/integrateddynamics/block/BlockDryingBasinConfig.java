package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockDryingBasin}.
 * @author rubensworks
 */
public class BlockDryingBasinConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "drying_basin",
                (eConfig, properties) -> new BlockDryingBasin(properties
                        .strength(2.0F, 5.0F)
                        .noOcclusion()
                        .isRedstoneConductor((blockState, world, pos) -> false)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
