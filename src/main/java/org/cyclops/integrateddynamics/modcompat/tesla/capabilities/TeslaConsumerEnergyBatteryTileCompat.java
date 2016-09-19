package org.cyclops.integrateddynamics.modcompat.tesla.capabilities;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.SimpleCapabilityConstructor;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;

import javax.annotation.Nullable;

/**
 * Compatibility for energy battery tesla consumer capability.
 * @author rubensworks
 */
public class TeslaConsumerEnergyBatteryTileCompat extends SimpleCapabilityConstructor<ITeslaConsumer, TileEnergyBattery> {

    @Override
    public Capability<ITeslaConsumer> getCapability() {
        return Capabilities.TESLA_CONSUMER;
    }

    @Nullable
    @Override
    public ICapabilityProvider createProvider(TileEnergyBattery host) {
        return new DefaultCapabilityProvider<>(Capabilities.TESLA_CONSUMER, new TeslaConsumer(host));
    }

    public static class TeslaConsumer implements ITeslaConsumer {

        private final TileEnergyBattery provider;

        public TeslaConsumer(TileEnergyBattery provider) {
            this.provider = provider;
        }

        @Override
        public long givePower(long power, boolean simulated) {
            return provider.receiveEnergy((int) Math.min(power, Integer.MAX_VALUE), simulated);
        }
    }
}
