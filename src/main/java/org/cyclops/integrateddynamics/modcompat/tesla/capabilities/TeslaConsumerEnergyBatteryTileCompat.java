package org.cyclops.integrateddynamics.modcompat.tesla.capabilities;

import net.darkhax.tesla.api.ITeslaConsumer;
import org.cyclops.cyclopscore.modcompat.ICapabilityCompat;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;

/**
 * Compatibility for energy battery tesla consumer capability.
 * @author rubensworks
 */
public class TeslaConsumerEnergyBatteryTileCompat implements ICapabilityCompat<TileEnergyBattery> {

    @Override
    public void attach(final TileEnergyBattery provider) {
        provider.addCapabilityInternal(Capabilities.TESLA_CONSUMER, new TeslaConsumer(provider));
    }

    public static class TeslaConsumer implements ITeslaConsumer {

        private final TileEnergyBattery provider;

        public TeslaConsumer(TileEnergyBattery provider) {
            this.provider = provider;
        }

        @Override
        public long givePower(long power, boolean simulated) {
            return provider.addEnergy((int) Long.min(power, Integer.MAX_VALUE), simulated);
        }
    }
}
