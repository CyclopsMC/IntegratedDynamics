package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SoundType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockLogicProgrammer}.
 * @author rubensworks
 */
public class BlockLogicProgrammerConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockLogicProgrammerConfig() {
        super(
                IntegratedDynamics._instance,
                "logic_programmer",
                (eConfig, properties) -> new BlockLogicProgrammer(properties
                        .strength(2.0F, 5.0F)
                        .sound(SoundType.METAL)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
