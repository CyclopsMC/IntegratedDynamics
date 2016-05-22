package org.cyclops.integrateddynamics.modcompat.tesla.capabilities;

import net.darkhax.tesla.api.ITeslaProducer;
import org.cyclops.cyclopscore.modcompat.ICapabilityCompat;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;

/**
 * Compatibility for energy battery tesla producer capability.
 * @author rubensworks
 */
public class TeslaProducerEnergyBatteryTileCompat implements ICapabilityCompat<TileEnergyBattery> {

    @Override
    public void attach(final TileEnergyBattery provider) {
        provider.addCapabilityInternal(Capabilities.TESLA_PRODUCER, new TeslaProducer(provider));
    }

    public static class TeslaProducer implements ITeslaProducer {

        private final TileEnergyBattery provider;

        public TeslaProducer(TileEnergyBattery provider) {
            this.provider = provider;
        }

        @Override
        public long takePower(long power, boolean simulated) {
            return provider.consume((int) Long.min(power, Integer.MAX_VALUE), simulated);
        }
    }
}
