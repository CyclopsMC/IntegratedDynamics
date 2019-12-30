package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link TileEnergyBattery}.
 * @author rubensworks
 *
 */
public class TileEnergyBatteryConfig extends TileEntityConfig<TileEnergyBattery> {

    public TileEnergyBatteryConfig() {
        super(
                IntegratedDynamics._instance,
                "energy_battery",
                (eConfig) -> new TileEntityType<>(TileEnergyBattery::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_ENERGY_BATTERY), null)
        );
    }

}
