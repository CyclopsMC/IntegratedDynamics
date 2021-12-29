package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockDryingBasin}.
 * @author rubensworks
 */
public class BlockDryingBasinConfig extends BlockConfig {

    public BlockDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "drying_basin",
                eConfig -> new BlockDryingBasin(Block.Properties.of(Material.WOOD)
                        .noOcclusion()
                        .isRedstoneConductor((blockState, world, pos) -> false)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
