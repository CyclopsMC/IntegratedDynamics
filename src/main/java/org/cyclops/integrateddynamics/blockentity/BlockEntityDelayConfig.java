package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link BlockEntityDelay}.
 * @author rubensworks
 *
 */
public class BlockEntityDelayConfig extends BlockEntityConfigCommon<BlockEntityDelay, IntegratedDynamics> {

    public BlockEntityDelayConfig() {
        super(
                IntegratedDynamics._instance,
                "delay",
                (eConfig) -> new BlockEntityType<>(BlockEntityDelay::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_DELAY.get()))
        );
        IntegratedDynamics._instance.getModEventBus().addListener(new BlockEntityDelay.CapabilityRegistrar(this::getInstance)::register);
    }

}
