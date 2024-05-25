package org.cyclops.integrateddynamics.core.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.client.render.blockentity.RenderCable;

/**
 * Config for the {@link BlockEntityMultipartTicking}.
 * @author rubensworks
 *
 */
public class BlockEntityMultipartTickingConfig extends BlockEntityConfig<BlockEntityMultipartTicking> {

    public BlockEntityMultipartTickingConfig() {
        super(
                IntegratedDynamics._instance,
                "multipart_ticking",
                (eConfig) -> new BlockEntityType<>(BlockEntityMultipartTicking::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_CABLE.get()), null)
        );
        IntegratedDynamics._instance.getModEventBus().addListener(this::registerCapability);
    }

    protected void registerCapability(RegisterCapabilitiesEvent event) {
        BlockEntityMultipartTicking.registerMultipartTickingCapabilities(event, getInstance());
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        if(MinecraftHelpers.isClientSide()) {
            registerClientSide();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void registerClientSide() {
        IntegratedDynamics._instance.getProxy().registerRenderer(getInstance(), RenderCable::new);
    }

}
