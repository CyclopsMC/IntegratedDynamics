package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.client.render.blockentity.RenderBlockEntityDryingBasin;

/**
 * Config for the {@link BlockEntityDryingBasin}.
 * @author rubensworks
 *
 */
public class BlockEntityDryingBasinConfig extends BlockEntityConfig<BlockEntityDryingBasin> {

    public BlockEntityDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "drying_basin",
                (eConfig) -> new BlockEntityType<>(BlockEntityDryingBasin::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_DRYING_BASIN.get()), null)
        );
        IntegratedDynamics._instance.getModEventBus().addListener(this::registerCapability);
    }

    protected void registerCapability(RegisterCapabilitiesEvent event) {
        BlockEntityDryingBasin.registerDryingBasinCapabilities(event, getInstance());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        getMod().getProxy().registerRenderer(getInstance(), RenderBlockEntityDryingBasin::new);
    }

}
