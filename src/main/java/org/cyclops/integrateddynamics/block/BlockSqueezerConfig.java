package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockSqueezer}.
 * @author rubensworks
 */
public class BlockSqueezerConfig extends BlockConfig {

    public BlockSqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "squeezer",
                eConfig -> new BlockSqueezer(Block.Properties.create(Material.IRON)
                        .hardnessAndResistance(5.0F)
                        .sound(SoundType.METAL)
                        .notSolid()),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
