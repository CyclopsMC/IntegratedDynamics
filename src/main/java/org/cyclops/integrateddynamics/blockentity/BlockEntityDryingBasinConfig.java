package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link BlockEntityDryingBasin}.
 * @author rubensworks
 *
 */
public class BlockEntityDryingBasinConfig extends BlockEntityConfigCommon<BlockEntityDryingBasin, IntegratedDynamics> {

    public BlockEntityDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "drying_basin",
                (eConfig) -> new BlockEntityType<>(BlockEntityDryingBasin::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_DRYING_BASIN.get()))
        );
        IntegratedDynamics._instance.getModEventBus().addListener(new BlockEntityDryingBasin.CapabilityRegistrar(this::getInstance)::register);
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide()) {
            new BlockEntityDryingBasinConfigClient().onRegistered(this);
        }
    }

}
