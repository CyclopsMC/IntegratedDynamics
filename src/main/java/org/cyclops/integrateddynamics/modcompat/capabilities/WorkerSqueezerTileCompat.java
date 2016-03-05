package org.cyclops.integrateddynamics.modcompat.capabilities;

import org.cyclops.commoncapabilities.api.capability.work.IWorker;
import org.cyclops.cyclopscore.modcompat.ICapabilityCompat;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.tileentity.TileSqueezer;

/**
 * Compatibility for squeezer worker capability.
 * @author rubensworks
 */
public class WorkerSqueezerTileCompat implements ICapabilityCompat<TileSqueezer> {

    @Override
    public void attach(final TileSqueezer provider) {
        provider.addCapabilityInternal(Capabilities.WORKER, new Worker(provider));
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
