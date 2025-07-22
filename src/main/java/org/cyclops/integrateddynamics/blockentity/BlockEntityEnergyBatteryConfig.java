package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link BlockEntityEnergyBattery}.
 * @author rubensworks
 *
 */
public class BlockEntityEnergyBatteryConfig extends BlockEntityConfigCommon<BlockEntityEnergyBattery, IntegratedDynamics> {

    public BlockEntityEnergyBatteryConfig() {
        super(
                IntegratedDynamics._instance,
                "energy_battery",
                (eConfig) -> new BlockEntityType<>(BlockEntityEnergyBattery::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_ENERGY_BATTERY.get(), RegistryEntries.BLOCK_ENERGY_BATTERY_CREATIVE.get()))
        );
        IntegratedDynamics._instance.getModEventBus().addListener(new BlockEntityEnergyBattery.CapabilityRegistrar(this::getInstance)::register);
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide()) {
            new BlockEntityEnergyBatteryConfigClient().onRegistered(this);
        }
    }

}
