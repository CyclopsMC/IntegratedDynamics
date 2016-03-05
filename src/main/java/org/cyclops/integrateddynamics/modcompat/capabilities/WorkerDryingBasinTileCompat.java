package org.cyclops.integrateddynamics.modcompat.capabilities;

import org.cyclops.commoncapabilities.api.capability.work.IWorker;
import org.cyclops.cyclopscore.modcompat.ICapabilityCompat;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.tileentity.TileDryingBasin;

/**
 * Compatibility for drying basin worker capability.
 * @author rubensworks
 */
public class WorkerDryingBasinTileCompat implements ICapabilityCompat<TileDryingBasin> {

    @Override
    public void attach(final TileDryingBasin provider) {
        provider.addCapabilityInternal(Capabilities.WORKER, new Worker(provider));
    }

    public static class Worker implements IWorker {

        private final TileDryingBasin provider;

        public Worker(TileDryingBasin provider) {
            this.provider = provider;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean hasWork() {
            return provider.getCurrentRecipe() != null;
        }

        @Override
        public boolean canWork() {
            return true;
        }
    }
}
