package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SoundType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockMaterializer}.
 * @author rubensworks
 */
public class BlockMaterializerConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockMaterializerConfig() {
        super(
                IntegratedDynamics._instance,
                "materializer",
                (eConfig, properties) -> new BlockMaterializer(properties
                        .strength(2.0F, 5.0F)
                        .sound(SoundType.METAL)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
