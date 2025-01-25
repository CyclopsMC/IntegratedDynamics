package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SoundType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockVariablestore}.
 * @author rubensworks
 */
public class BlockVariablestoreConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockVariablestoreConfig() {
        super(
                IntegratedDynamics._instance,
                "variablestore",
                (eConfig, properties) -> new BlockVariablestore(properties
                        .strength(2.0F, 5.0F)
                        .sound(SoundType.METAL)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
