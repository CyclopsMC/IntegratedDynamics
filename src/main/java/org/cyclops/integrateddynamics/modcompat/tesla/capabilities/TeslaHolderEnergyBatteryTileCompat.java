package org.cyclops.integrateddynamics.modcompat.tesla.capabilities;

import net.darkhax.tesla.api.ITeslaHolder;
import org.cyclops.cyclopscore.modcompat.ICapabilityCompat;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;

/**
 * Compatibility for energy battery tesla holder capability.
 * @author rubensworks
 */
public class TeslaHolderEnergyBatteryTileCompat implements ICapabilityCompat<TileEnergyBattery> {

    @Override
    public void attach(final TileEnergyBattery provider) {
        provider.addCapabilityInternal(Capabilities.TESLA_HOLDER, new TeslaHolder(provider));
    }

    public static class TeslaHolder implements ITeslaHolder {

        private final TileEnergyBattery provider;

        public TeslaHolder(TileEnergyBattery provider) {
            this.provider = provider;
        }

        @Override
        public long getStoredPower() {
            return provider.getStoredEnergy();
        }

        @Override
        public long getCapacity() {
            return provider.getMaxStoredEnergy();
        }
    }
}
