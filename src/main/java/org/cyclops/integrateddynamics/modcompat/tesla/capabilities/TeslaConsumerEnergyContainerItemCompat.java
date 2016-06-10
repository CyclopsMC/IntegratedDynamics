package org.cyclops.integrateddynamics.modcompat.tesla.capabilities;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.ICapabilityConstructor;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;

import javax.annotation.Nullable;

/**
 * Compatibility for energy battery item tesla holder capability.
 * @author rubensworks
 */
public class TeslaConsumerEnergyContainerItemCompat implements ICapabilityConstructor<ITeslaConsumer, ItemStack> {

    @Override
    public Capability<ITeslaConsumer> getCapability() {
        return Capabilities.TESLA_CONSUMER;
    }

    @Nullable
    @Override
    public ICapabilityProvider createProvider(ItemStack host) {
        return new DefaultCapabilityProvider<>(Capabilities.TESLA_CONSUMER, new TeslaHolder(host));
    }

    public static class TeslaHolder implements ITeslaConsumer {

        private final ItemStack provider;

        public TeslaHolder(ItemStack provider) {
            this.provider = provider;
        }

        @Override
        public long givePower(long power, boolean simulated) {
            return ((ItemBlockEnergyContainer) provider.getItem()).addEnergy(provider, (int) Long.min(power, Integer.MAX_VALUE), simulated);
        }
    }
}
