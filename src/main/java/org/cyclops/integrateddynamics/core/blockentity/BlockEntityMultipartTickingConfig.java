package org.cyclops.integrateddynamics.core.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfigCommon;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityMultipartTickingConfigClient;

/**
 * Config for the {@link BlockEntityMultipartTicking}.
 * @author rubensworks
 *
 */
public class BlockEntityMultipartTickingConfig extends BlockEntityConfigCommon<BlockEntityMultipartTicking, IntegratedDynamics> {

    public BlockEntityMultipartTickingConfig() {
        super(
                IntegratedDynamics._instance,
                "multipart_ticking",
                (eConfig) -> new BlockEntityType<>(BlockEntityMultipartTicking::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_CABLE.get()))
        );
        IntegratedDynamics._instance.getModEventBus().addListener(this::registerCapability);
    }

    protected void registerCapability(RegisterCapabilitiesEvent event) {
        BlockEntityMultipartTicking.registerMultipartTickingCapabilities(event, getInstance());
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        if(IModHelpers.get().getMinecraftHelpers().isClientSide()) {
            new BlockEntityMultipartTickingConfigClient().onRegistered(this);
        }
    }

}
