package org.cyclops.integrateddynamics.modcompat.tesla.capabilities;

import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.SimpleCapabilityConstructor;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;

import javax.annotation.Nullable;

/**
 * Compatibility for energy battery tesla producer capability.
 * @author rubensworks
 */
public class TeslaProducerEnergyBatteryTileCompat extends SimpleCapabilityConstructor<ITeslaProducer, TileEnergyBattery> {

    @Override
    public Capability<ITeslaProducer> getCapability() {
        return Capabilities.TESLA_PRODUCER;
    }

    @Nullable
    @Override
    public ICapabilityProvider createProvider(TileEnergyBattery host) {
        return new DefaultCapabilityProvider<>(Capabilities.TESLA_PRODUCER, new TeslaProducer(host));
    }

    public static class TeslaProducer implements ITeslaProducer {

        private final TileEnergyBattery provider;

        public TeslaProducer(TileEnergyBattery provider) {
            this.provider = provider;
        }

        @Override
        public long takePower(long power, boolean simulated) {
            return provider.consume((int) Math.min(power, Integer.MAX_VALUE), simulated);
        }
    }
}
