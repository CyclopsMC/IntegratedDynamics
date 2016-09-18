package org.cyclops.integrateddynamics.modcompat.tesla.capabilities;

import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.ICapabilityConstructor;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.capability.energybattery.EnergyBatteryConfig;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;

import javax.annotation.Nullable;

/**
 * Compatibility for energy battery item tesla holder capability.
 * @author rubensworks
 */
public class TeslaProducerEnergyContainerItemCompat implements ICapabilityConstructor<ITeslaProducer, ItemBlockEnergyContainer, ItemStack> {

    @Override
    public Capability<ITeslaProducer> getCapability() {
        return Capabilities.TESLA_PRODUCER;
    }

    @Nullable
    @Override
    public ICapabilityProvider createProvider(ItemBlockEnergyContainer hostType, ItemStack host) {
        return new DefaultCapabilityProvider<>(Capabilities.TESLA_PRODUCER, new TeslaHolder(host));
    }

    public static class TeslaHolder implements ITeslaProducer {

        private final ItemStack provider;

        public TeslaHolder(ItemStack provider) {
            this.provider = provider;
        }

        @Override
        public long takePower(long power, boolean simulated) {
            return provider.getCapability(EnergyBatteryConfig.CAPABILITY, null).consume((int) Math.min(power, Integer.MAX_VALUE), simulated);
        }
    }
}
