package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
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
                eConfig -> new BlockMaterializer(Block.Properties.of()
                        .strength(2.0F, 5.0F)
                        .sound(SoundType.METAL)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
