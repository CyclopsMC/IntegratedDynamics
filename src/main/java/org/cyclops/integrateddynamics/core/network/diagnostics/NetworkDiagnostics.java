package org.cyclops.integrateddynamics.core.network.diagnostics;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.network.IFullNetworkListener;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetworkElement;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.persist.world.NetworkWorldStorage;
import org.cyclops.integrateddynamics.network.packet.NetworkDiagnosticsNetworkPacket;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author rubensworks
 */
public class NetworkDiagnostics {

    private static final NetworkDiagnostics _INSTANCE = new NetworkDiagnostics();

    private final List<UUID> players = Lists.newArrayList();

    private NetworkDiagnostics() {

    }

    public static NetworkDiagnostics getInstance() {
        return _INSTANCE;
    }

    protected ServerPlayerEntity getPlayer(UUID uuid) {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
    }

    public synchronized void registerPlayer(ServerPlayerEntity player) {
        if (!players.contains(player.getUUID())) {
            players.add(player.getUUID());
            for (INetwork network : NetworkWorldStorage.getInstance(IntegratedDynamics._instance).getNetworks()) {
                sendNetworkUpdateToPlayer(player, network);
            }

        }
    }

    public synchronized void unRegisterPlayer(ServerPlayerEntity player) {
        players.remove(player.getUUID());
    }

    public void sendNetworkUpdateToPlayer(ServerPlayerEntity player, INetwork network) {
        List<RawPartData> rawParts = Lists.newArrayList();
        for (INetworkElement networkElement : network.getElements()) {
            if (networkElement instanceof IPartNetworkElement) {
                IPartNetworkElement partNetworkElement = (IPartNetworkElement) networkElement;
                PartPos pos = partNetworkElement.getTarget().getCenter();
                long lastSecondDurationNs = network.getLastSecondDuration(networkElement);
                rawParts.add(new RawPartData(pos.getPos().getWorldKey(),
                        pos.getPos().getBlockPos(), pos.getSide(),
                        L10NHelpers.localize(partNetworkElement.getPart().getTranslationKey()),
                        lastSecondDurationNs));
            } else {
                // If needed, we can send the other part types later on as well
            }
        }

        List<RawObserverData> rawObservers = Lists.newArrayList();
        for (IFullNetworkListener fullNetworkListener : network.getFullNetworkListeners()) {
            if (fullNetworkListener instanceof IPositionedAddonsNetworkIngredients) {
                IPositionedAddonsNetworkIngredients<?, ?> networkIngredients = (IPositionedAddonsNetworkIngredients<?, ?>) fullNetworkListener;
                Map<PartPos, Long> durations = networkIngredients.getLastSecondDurationIndex();
                for (Map.Entry<PartPos, Long> durationEntry : durations.entrySet()) {
                    PartPos pos = durationEntry.getKey();
                    rawObservers.add(new RawObserverData(pos.getPos().getWorldKey(),
                            pos.getPos().getBlockPos(), pos.getSide(),
                            networkIngredients.getComponent().getName().toString(), durationEntry.getValue()));
                }
            }
        }

        RawNetworkData rawNetworkData = new RawNetworkData(network.isKilled(), network.hashCode(), network.getCablesCount(), rawParts, rawObservers);
        IntegratedDynamics._instance.getPacketHandler().sendToPlayer(new NetworkDiagnosticsNetworkPacket(rawNetworkData.toNbt()), player);
    }

    public synchronized void sendNetworkUpdate(INetwork network) {
        for (Iterator<UUID> it = players.iterator(); it.hasNext();) {
            UUID uuid = it.next();
            ServerPlayerEntity player = getPlayer(uuid);
            if (player != null) {
                sendNetworkUpdateToPlayer(player, network);
            } else {
                it.remove();
            }
        }
    }

    public synchronized boolean isBeingDiagnozed() {
        return !players.isEmpty();
    }

}
