package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockCoalGenerator}.
 * @author rubensworks
 */
public class BlockCoalGeneratorConfig extends BlockConfig {

    public BlockCoalGeneratorConfig() {
        super(
                IntegratedDynamics._instance,
                "coal_generator",
                eConfig -> new BlockCoalGenerator(Block.Properties.of()
                        .strength(2.0F, 5.0F)
                        .sound(SoundType.METAL)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
