package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link BlockEntitySqueezer}.
 * @author rubensworks
 *
 */
public class BlockEntitySqueezerConfig extends BlockEntityConfigCommon<BlockEntitySqueezer, IntegratedDynamics> {

    public BlockEntitySqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "squeezer",
                (eConfig) -> new BlockEntityType<>(BlockEntitySqueezer::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_SQUEEZER.get()))
        );
        IntegratedDynamics._instance.getModEventBus().addListener(new BlockEntitySqueezer.CapabilityRegistrar(this::getInstance)::register);
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide()) {
            new BlockEntitySqueezerConfigClient().onRegistered(this);
        }
    }

}
