package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SoundType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockCoalGenerator}.
 * @author rubensworks
 */
public class BlockCoalGeneratorConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockCoalGeneratorConfig() {
        super(
                IntegratedDynamics._instance,
                "coal_generator",
                (eConfig, properties) -> new BlockCoalGenerator(properties
                        .strength(2.0F, 5.0F)
                        .sound(SoundType.METAL)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
