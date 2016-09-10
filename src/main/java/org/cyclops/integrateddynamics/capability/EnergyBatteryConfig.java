package org.cyclops.integrateddynamics.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.block.IEnergyBattery;

/**
 * Config for the energy battery capability.
 * @author rubensworks
 *
 */
public class EnergyBatteryConfig extends CapabilityConfig {

    /**
     * The unique instance.
     */
    public static EnergyBatteryConfig _instance;

    @CapabilityInject(IEnergyBattery.class)
    public static Capability<IEnergyBattery> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public EnergyBatteryConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "energyBatteryCap",
                "Allows storage of energy.",
                IEnergyBattery.class,
                new DefaultCapabilityStorage<IEnergyBattery>(),
                EnergyBatteryDefault.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
