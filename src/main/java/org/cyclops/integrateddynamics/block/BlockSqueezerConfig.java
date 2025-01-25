package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SoundType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockSqueezer}.
 * @author rubensworks
 */
public class BlockSqueezerConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockSqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "squeezer",
                (eConfig, properties) -> new BlockSqueezer(properties
                        .strength(2.0F, 5.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()
                        .isRedstoneConductor((blockState, world, pos) -> false)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
