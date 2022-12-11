package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockMaterializer}.
 * @author rubensworks
 */
public class BlockMaterializerConfig extends BlockConfig {

    public BlockMaterializerConfig() {
        super(
                IntegratedDynamics._instance,
                "materializer",
                eConfig -> new BlockMaterializer(Block.Properties.of(Material.HEAVY_METAL)
                        .strength(2.0F, 5.0F)
                        .sound(SoundType.METAL)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
