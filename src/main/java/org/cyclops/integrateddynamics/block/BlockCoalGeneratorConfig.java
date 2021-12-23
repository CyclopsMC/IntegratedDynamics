package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
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
                eConfig -> new BlockCoalGenerator(Block.Properties.of(Material.HEAVY_METAL)
                        .strength(5.0F)
                        .sound(SoundType.METAL)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
