package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link BlockEntityProxy}.
 * @author rubensworks
 *
 */
public class BlockEntityProxyConfig extends BlockEntityConfigCommon<BlockEntityProxy, IntegratedDynamics> {

    public BlockEntityProxyConfig() {
        super(
                IntegratedDynamics._instance,
                "proxy",
                (eConfig) -> new BlockEntityType<>(BlockEntityProxy::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_PROXY.get()))
        );
        IntegratedDynamics._instance.getModEventBus().addListener(new BlockEntityProxy.CapabilityRegistrar(this::getInstance)::register);
    }

}
