package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
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
                eConfig -> new BlockSqueezer(Block.Properties.of(Material.METAL)
                        .strength(2.0F, 5.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()
                        .isRedstoneConductor((blockState, world, pos) -> false)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
