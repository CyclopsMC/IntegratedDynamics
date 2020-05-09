package org.cyclops.integrateddynamics.core;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.network.IFullNetworkListener;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.diagnostics.NetworkDiagnostics;
import org.cyclops.integrateddynamics.core.persist.world.NetworkWorldStorage;

/**
 * Handles server ticks to delegate to networks.
 * @author rubensworks
 */
public final class TickHandler {

    private static TickHandler INSTANCE;
    private int tick = 0;
    private boolean shouldCrash = false;

    private TickHandler() {

    }

    public static TickHandler getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new TickHandler();
        }
        return INSTANCE;
    }

    public void setShouldCrash() {
        this.shouldCrash = true;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (shouldCrash) {
            throw new RuntimeException("Forcefully crashed the server.");
        }
        if(event.type == TickEvent.Type.SERVER && event.phase == TickEvent.Phase.END && NetworkHelpers.shouldWork()) {
            boolean isBeingDiagnozed = NetworkDiagnostics.getInstance().isBeingDiagnozed();
            if (isBeingDiagnozed) {
                tick = (tick + 1) % MinecraftHelpers.SECOND_IN_TICKS;
            }
            boolean shouldSendTickDurationInfo = isBeingDiagnozed && tick == 0;
            for(INetwork network : NetworkWorldStorage.getInstance(IntegratedDynamics._instance).getNetworks()) {
                if (isBeingDiagnozed && (shouldSendTickDurationInfo || network.hasChanged())) {
                    NetworkDiagnostics.getInstance().sendNetworkUpdate(network);
                    network.resetLastSecondDurations();

                    // Also reset durations of indexes
                    for (IFullNetworkListener fullNetworkListener : network.getFullNetworkListeners()) {
                        if (fullNetworkListener instanceof IPositionedAddonsNetworkIngredients) {
                            IPositionedAddonsNetworkIngredients<?, ?> networkIngredients = (IPositionedAddonsNetworkIngredients<?, ?>) fullNetworkListener;
                            networkIngredients.resetLastSecondDurationsIndex();
                        }
                    }
                }
                try {
                    if (!network.isCrashed()) {
                        network.update();
                    }
                } catch (Throwable e) {
                    network.setCrashed(true);
                    throw e;
                }
            }
        }
    }

}
