package org.cyclops.integrateddynamics.modcompat.capabilities;

import org.cyclops.commoncapabilities.api.capability.work.IWorker;
import org.cyclops.cyclopscore.modcompat.ICapabilityCompat;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.tileentity.TileCoalGenerator;

/**
 * Compatibility for coal generator worker capability.
 * @author rubensworks
 */
public class WorkerCoalGeneratorTileCompat implements ICapabilityCompat<TileCoalGenerator> {

    @Override
    public void attach(final TileCoalGenerator provider) {
        provider.addCapabilityInternal(Capabilities.WORKER, new Worker(provider));
    }

    public static class Worker implements IWorker {

        private final TileCoalGenerator provider;

        public Worker(TileCoalGenerator provider) {
            this.provider = provider;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean hasWork() {
            return provider.getStackInSlot(TileCoalGenerator.SLOT_FUEL) != null || provider.isBurning();
        }

        @Override
        public boolean canWork() {
            return provider.canAddEnergy(TileCoalGenerator.ENERGY_PER_TICK);
        }
    }
}
