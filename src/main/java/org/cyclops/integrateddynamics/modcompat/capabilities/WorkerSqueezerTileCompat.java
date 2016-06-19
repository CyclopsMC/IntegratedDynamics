package org.cyclops.integrateddynamics.modcompat.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.cyclops.commoncapabilities.api.capability.work.IWorker;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.SimpleCapabilityConstructor;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.tileentity.TileSqueezer;

import javax.annotation.Nullable;

/**
 * Compatibility for squeezer worker capability.
 * @author rubensworks
 */
public class WorkerSqueezerTileCompat extends SimpleCapabilityConstructor<IWorker, TileSqueezer> {

    @Override
    public Capability<IWorker> getCapability() {
        return Capabilities.WORKER;
    }

    @Nullable
    @Override
    public ICapabilityProvider createProvider(TileSqueezer host) {
        return new DefaultCapabilityProvider<>(Capabilities.WORKER, new Worker(host));
    }

    public static class Worker implements IWorker {

        private final TileSqueezer provider;

        public Worker(TileSqueezer provider) {
            this.provider = provider;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean hasWork() {
            return provider.getCurrentRecipe() != null;
        }

        @Override
        public boolean canWork() {
            return false;
        }
    }
}
