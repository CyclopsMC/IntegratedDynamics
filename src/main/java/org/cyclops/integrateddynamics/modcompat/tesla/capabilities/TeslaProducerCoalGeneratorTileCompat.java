package org.cyclops.integrateddynamics.modcompat.tesla.capabilities;

import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.ICapabilityConstructor;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.tileentity.TileCoalGenerator;

import javax.annotation.Nullable;

/**
 * Compatibility for coal generator tesla producer capability.
 * @author rubensworks
 */
public class TeslaProducerCoalGeneratorTileCompat implements ICapabilityConstructor<ITeslaProducer, TileCoalGenerator> {

    @Override
    public Capability<ITeslaProducer> getCapability() {
        return Capabilities.TESLA_PRODUCER;
    }

    @Nullable
    @Override
    public ICapabilityProvider createProvider(TileCoalGenerator host) {
        return new DefaultCapabilityProvider<>(Capabilities.TESLA_PRODUCER, new TeslaProducer());
    }

    public static class TeslaProducer implements ITeslaProducer {
        @Override
        public long takePower(long power, boolean simulated) {
            return 0;
        }
    }
}
