package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SoundType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.item.ItemBlockProxy;

/**
 * Config for {@link BlockProxy}.
 * @author rubensworks
 */
public class BlockProxyConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockProxyConfig() {
        super(
                IntegratedDynamics._instance,
                "proxy",
                (eConfig, properties) -> new BlockProxy(properties
                        .strength(2.0F, 5.0F)
                        .sound(SoundType.METAL)),
                (eConfig, block) -> new ItemBlockProxy(block, eConfig.createDefaultItemProperties())
        );
    }

}
