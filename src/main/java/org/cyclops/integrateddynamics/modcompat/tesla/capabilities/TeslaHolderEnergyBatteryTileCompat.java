package org.cyclops.integrateddynamics.modcompat.tesla.capabilities;

import net.darkhax.tesla.api.ITeslaHolder;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.SimpleCapabilityConstructor;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;

import javax.annotation.Nullable;

/**
 * Compatibility for energy battery tesla holder capability.
 * @author rubensworks
 */
public class TeslaHolderEnergyBatteryTileCompat extends SimpleCapabilityConstructor<ITeslaHolder, TileEnergyBattery> {

    @Override
    public Capability<ITeslaHolder> getCapability() {
        return Capabilities.TESLA_HOLDER;
    }

    @Nullable
    @Override
    public ICapabilityProvider createProvider(TileEnergyBattery host) {
        return new DefaultCapabilityProvider<>(Capabilities.TESLA_HOLDER, new TeslaHolder(host));
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
