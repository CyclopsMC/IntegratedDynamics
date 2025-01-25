package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SoundType;
import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.item.ItemBlockDelay;

/**
 * Config for {@link BlockDelay}.
 * @author rubensworks
 */
public class BlockDelayConfig extends BlockConfigCommon<IntegratedDynamics> {

    @ConfigurablePropertyCommon(category = "machine", comment = "The maximum value history length that can be maintained..", minimalValue = 1)
    public static int maxHistoryCapacity = 1024;

    public BlockDelayConfig() {
        super(
            IntegratedDynamics._instance,
            "delay",
                (eConfig, properties) -> new BlockDelay(properties
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.METAL)),
                (eConfig, block) -> new ItemBlockDelay(block, eConfig.createDefaultItemProperties())
        );
    }

}
