package org.cyclops.integrateddynamics.modcompat.tesla.capabilities;

import net.darkhax.tesla.api.ITeslaProducer;
import org.cyclops.cyclopscore.modcompat.ICapabilityCompat;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.tileentity.TileCoalGenerator;

/**
 * Compatibility for coal generator tesla producer capability.
 * @author rubensworks
 */
public class TeslaProducerCoalGeneratorTileCompat implements ICapabilityCompat<TileCoalGenerator> {

    @Override
    public void attach(final TileCoalGenerator provider) {
        provider.addCapabilityInternal(Capabilities.TESLA_PRODUCER, new TeslaProducer());
    }

    public static class TeslaProducer implements ITeslaProducer {
        @Override
        public long takePower(long power, boolean simulated) {
            return 0;
        }
    }
}
