package org.cyclops.integrateddynamics.core.network.diagnostics;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.network.*;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.persist.world.NetworkWorldStorage;
import org.cyclops.integrateddynamics.network.packet.NetworkDiagnosticsNetworkPacket;

import java.util.*;

/**
 * @author rubensworks
 */
public class NetworkDiagnostics {

    private static final NetworkDiagnostics _INSTANCE = new NetworkDiagnostics();

    private final List<UUID> players = Lists.newArrayList();
    private final Map<UUID, MeasurementSession> activeMeasurements = Maps.newHashMap();
    private final Set<UUID> playerMeasurements = Sets.newHashSet(); // Track which measurements are player-based
    private final Map<UUID, UUID> playerToMeasurement = Maps.newHashMap(); // Map player UUID to measurement UUID

    private NetworkDiagnostics() {

    }

    public static NetworkDiagnostics getInstance() {
        return _INSTANCE;
    }

    protected ServerPlayer getPlayer(UUID uuid) {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
    }

    public synchronized void registerPlayer(ServerPlayer player) {
        if (!players.contains(player.getUUID())) {
            players.add(player.getUUID());
            for (INetwork network : NetworkWorldStorage.getInstance(IntegratedDynamics._instance).getNetworks()) {
                sendNetworkUpdateToPlayer(player, network);
            }

        }
    }

    public synchronized void unRegisterPlayer(ServerPlayer player) {
        players.remove(player.getUUID());
    }

    public void sendNetworkUpdateToPlayer(ServerPlayer player, INetwork network) {
        List<RawPartData> rawParts = Lists.newArrayList();
        for (INetworkElement networkElement : network.getElements()) {
            if (network.isValid(networkElement) && networkElement instanceof IPartNetworkElement) {
                IPartNetworkElement partNetworkElement = (IPartNetworkElement) networkElement;
                PartPos pos = partNetworkElement.getTarget().getCenter();
                long lastSecondDurationNs = network.getLastSecondDuration(networkElement);
                rawParts.add(new RawPartData(pos.getPos().getLevelKey(),
                        pos.getPos().getBlockPos(), pos.getSide(),
                        partNetworkElement.getPart().getTranslationKey(),
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
                    rawObservers.add(new RawObserverData(pos.getPos().getLevelKey(),
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
            ServerPlayer player = getPlayer(uuid);
            if (player != null) {
                sendNetworkUpdateToPlayer(player, network);
            } else {
                it.remove();
            }
        }
    }

    public synchronized boolean isBeingDiagnozed() {
        return !players.isEmpty() || !activeMeasurements.isEmpty();
    }

    /**
     * Check if a measurement session is complete for the given player.
     * @param playerId The UUID of the player
     * @return true if the measurement is complete, false otherwise
     */
    public synchronized boolean isMeasurementComplete(UUID playerId) {
        MeasurementSession session = activeMeasurements.get(playerId);
        if (session == null) {
            return false;
        }
        return session.isComplete();
    }

    /**
     * Get the total tick time in milliseconds for a completed measurement.
     * @param playerId The UUID of the player
     * @return The total tick time in milliseconds, or 0.0 if measurement not complete or not found
     */
    public synchronized double getMeasurementTotalTickTime(UUID playerId) {
        MeasurementSession session = activeMeasurements.get(playerId);
        if (session == null || !session.isComplete()) {
            return 0.0;
        }

        long totalNanoseconds = 0;
        for (Long time : session.getPartTimes().values()) {
            totalNanoseconds += time;
        }
        for (Long time : session.getObserverTimes().values()) {
            totalNanoseconds += time;
        }

        return totalNanoseconds / 1_000_000.0;
    }

    /**
     * Get the average tick time in milliseconds for a completed measurement.
     * @param playerId The UUID of the player
     * @return The average tick time per game tick in milliseconds, or 0.0 if measurement not complete or not found
     */
    public synchronized double getMeasurementAverageTickTime(UUID playerId) {
        MeasurementSession session = activeMeasurements.get(playerId);
        if (session == null || !session.isComplete()) {
            return 0.0;
        }

        long totalNanoseconds = 0;
        for (Long time : session.getPartTimes().values()) {
            totalNanoseconds += time;
        }
        for (Long time : session.getObserverTimes().values()) {
            totalNanoseconds += time;
        }

        double totalMilliseconds = totalNanoseconds / 1_000_000.0;
        int totalTicks = session.getDurationSeconds() * 20;
        return totalMilliseconds / totalTicks;
    }

    /**
     * Clear a measurement after it has been read.
     * This is useful for non-player measurements that persist until explicitly cleared.
     * @param measurementId The UUID of the measurement to clear
     */
    public synchronized void clearMeasurement(UUID measurementId) {
        activeMeasurements.remove(measurementId);
        playerMeasurements.remove(measurementId);
    }

    /**
     * Start a measurement session without a player.
     * This is useful for automated benchmarking where no player is online.
     * Measurements started this way will persist until explicitly cleared via {@link #clearMeasurement(UUID)}.
     * @param measurementId A unique identifier for the measurement (e.g., a UUID or string)
     * @param durationSeconds Duration of the measurement in seconds
     * @return The UUID used internally for tracking this measurement
     */
    public synchronized UUID startMeasurementWithoutPlayer(String measurementId, int durationSeconds) {
        UUID internalId = UUID.nameUUIDFromBytes(measurementId.getBytes());
        if (activeMeasurements.containsKey(internalId)) {
            return null; // Measurement already running
        }

        MeasurementSession session = new MeasurementSession(internalId, durationSeconds);
        activeMeasurements.put(internalId, session);
        // Note: NOT adding to playerMeasurements, so it won't be auto-removed
        return internalId;
    }

    /**
     * Start a measurement session for a player.
     * @param player The player initiating the measurement
     * @param durationSeconds Duration of the measurement in seconds
     */
    public synchronized void startMeasurement(ServerPlayer player, int durationSeconds) {
        UUID playerId = player.getUUID();

        // Check if this player already has an active measurement
        if (playerToMeasurement.containsKey(playerId)) {
            player.sendSystemMessage(Component.literal("A measurement is already running for you. Please wait for it to complete.")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        UUID measurementUUID = startMeasurementWithoutPlayer("player:" + playerId, durationSeconds);

        if (measurementUUID != null) {
            playerMeasurements.add(measurementUUID); // Mark as player measurement for auto-removal
            playerToMeasurement.put(playerId, measurementUUID); // Track which measurement belongs to this player
        }

        player.sendSystemMessage(Component.literal("Started measuring network tick times for " + durationSeconds + " seconds...")
                .withStyle(ChatFormatting.GREEN));
    }

    /**
     * Accumulate tick times for active measurement sessions.
     */
    public synchronized void accumulateMeasurements() {
        if (activeMeasurements.isEmpty()) {
            return;
        }

        for (INetwork network : NetworkWorldStorage.getInstance(IntegratedDynamics._instance).getNetworks()) {
            // Accumulate individual parts
            for (INetworkElement element : network.getElements()) {
                long elementDuration = network.getLastSecondDuration(element);
                if (elementDuration > 0 && element instanceof IPartNetworkElement) {
                    IPartNetworkElement partElement = (IPartNetworkElement) element;
                    PartPos pos = partElement.getTarget().getCenter();
                    String partName = partElement.getPart().getUniqueName().toString();
                    String dimension = pos.getPos().getLevelKey().location().toString();
                    int x = pos.getPos().getBlockPos().getX();
                    int y = pos.getPos().getBlockPos().getY();
                    int z = pos.getPos().getBlockPos().getZ();

                    PartInfo partInfo = new PartInfo(partName, dimension, x, y, z);

                    for (MeasurementSession session : activeMeasurements.values()) {
                        session.accumulate(partInfo, elementDuration);
                    }
                }
            }

            // Accumulate individual observers
            for (IFullNetworkListener fullNetworkListener : network.getFullNetworkListeners()) {
                if (fullNetworkListener instanceof IPositionedAddonsNetworkIngredients) {
                    IPositionedAddonsNetworkIngredients<?, ?> networkIngredients = (IPositionedAddonsNetworkIngredients<?, ?>) fullNetworkListener;
                    Map<PartPos, Long> durations = networkIngredients.getLastSecondDurationIndex();
                    String componentName = networkIngredients.getComponent().getName().toString();

                    for (Map.Entry<PartPos, Long> durationEntry : durations.entrySet()) {
                        long observerDuration = durationEntry.getValue();
                        if (observerDuration > 0) {
                            PartPos pos = durationEntry.getKey();
                            String dimension = pos.getPos().getLevelKey().location().toString();
                            int x = pos.getPos().getBlockPos().getX();
                            int y = pos.getPos().getBlockPos().getY();
                            int z = pos.getPos().getBlockPos().getZ();

                            ObserverInfo observerInfo = new ObserverInfo(componentName, dimension, x, y, z);

                            for (MeasurementSession session : activeMeasurements.values()) {
                                session.accumulateObserver(observerInfo, observerDuration);
                            }
                        }
                    }
                }
            }
        }

        // Increment the second counter for all sessions
        for (MeasurementSession session : activeMeasurements.values()) {
            session.incrementSecond();
        }
    }

    /**
     * Check and complete any finished measurements.
     * Player-based measurements are automatically removed after completion and results are sent.
     * Non-player measurements (e.g., from automated tests) persist until explicitly cleared.
     */
    public synchronized void checkCompleteMeasurements() {
        Iterator<Map.Entry<UUID, MeasurementSession>> it = activeMeasurements.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, MeasurementSession> entry = it.next();
            UUID measurementId = entry.getKey();
            MeasurementSession session = entry.getValue();

            if (session.isComplete()) {
                // Only auto-remove player-based measurements
                if (playerMeasurements.contains(measurementId)) {
                    // Find the player who owns this measurement
                    UUID playerId = null;
                    for (Map.Entry<UUID, UUID> playerEntry : playerToMeasurement.entrySet()) {
                        if (playerEntry.getValue().equals(measurementId)) {
                            playerId = playerEntry.getKey();
                            break;
                        }
                    }

                    if (playerId != null) {
                        ServerPlayer player = getPlayer(playerId);
                        if (player != null) {
                            sendMeasurementResults(player, session);
                        }
                        playerToMeasurement.remove(playerId);
                    }

                    playerMeasurements.remove(measurementId);
                    it.remove();
                }
                // Non-player measurements persist for explicit reading
            }
        }
    }

    /**
     * Send measurement results to the player.
     */
    private void sendMeasurementResults(ServerPlayer player, MeasurementSession session) {
        if (session.getPartTimes().isEmpty() && session.getObserverTimes().isEmpty()) {
            player.sendSystemMessage(Component.literal("No network elements found during measurement period.")
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }

        // Calculate totals
        long totalNanoseconds = 0;
        for (Long time : session.getPartTimes().values()) {
            totalNanoseconds += time;
        }
        for (Long time : session.getObserverTimes().values()) {
            totalNanoseconds += time;
        }

        double totalMilliseconds = totalNanoseconds / 1_000_000.0;
        double totalSeconds = totalNanoseconds / 1_000_000_000.0;

        // Format main result
        player.sendSystemMessage(Component.literal("=== Network Tick Time Measurement Results ===")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("Measurement Duration: " + session.getDurationSeconds() + " seconds")
                .withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal(""));

        player.sendSystemMessage(Component.literal("Total Tick Time: ")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.format("%.2f ms (%.3f s)", totalMilliseconds, totalSeconds))
                        .withStyle(ChatFormatting.WHITE)));

        // Calculate average per tick (20 ticks per second)
        int totalTicks = session.getDurationSeconds() * 20;
        double avgPerTick = totalMilliseconds / totalTicks;
        player.sendSystemMessage(Component.literal("Average per Tick: ")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.format("%.4f ms", avgPerTick))
                        .withStyle(ChatFormatting.WHITE)));

        // Show top-5 network parts
        if (!session.getPartTimes().isEmpty()) {
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("Top Network Parts:")
                    .withStyle(ChatFormatting.YELLOW));

            // Sort by time descending
            List<Map.Entry<PartInfo, Long>> sortedParts = Lists.newArrayList(session.getPartTimes().entrySet());
            sortedParts.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

            int count = 0;
            for (Map.Entry<PartInfo, Long> entry : sortedParts) {
                if (count >= 5) break;

                PartInfo info = entry.getKey();
                double partMs = entry.getValue() / 1_000_000.0;
                double percentage = (entry.getValue() * 100.0) / totalNanoseconds;
                player.sendSystemMessage(Component.literal(String.format("  %d. %s @ (%d, %d, %d) [%s]: %.2f ms (%.1f%%)",
                        count + 1, info.partName, info.x, info.y, info.z, info.dimension, partMs, percentage))
                        .withStyle(ChatFormatting.WHITE));
                count++;
            }
        }

        // Show top-3 observers if any exist
        if (!session.getObserverTimes().isEmpty()) {
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("Top Observers:")
                    .withStyle(ChatFormatting.YELLOW));

            // Sort by time descending
            List<Map.Entry<ObserverInfo, Long>> sortedObservers = Lists.newArrayList(session.getObserverTimes().entrySet());
            sortedObservers.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

            int count = 0;
            for (Map.Entry<ObserverInfo, Long> entry : sortedObservers) {
                if (count >= 3) break;

                ObserverInfo info = entry.getKey();
                double obsMs = entry.getValue() / 1_000_000.0;
                double percentage = (entry.getValue() * 100.0) / totalNanoseconds;
                player.sendSystemMessage(Component.literal(String.format("  %d. %s @ (%d, %d, %d) [%s]: %.2f ms (%.1f%%)",
                        count + 1, info.componentName, info.x, info.y, info.z, info.dimension, obsMs, percentage))
                        .withStyle(ChatFormatting.WHITE));
                count++;
            }
        }

        player.sendSystemMessage(Component.literal("===========================================")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
    }

    /**
     * Internal class to track a measurement session.
     */
    private static class MeasurementSession {
        private final UUID playerId;
        private final int durationSeconds;
        private final Map<PartInfo, Long> partTimes = Maps.newHashMap();
        private final Map<ObserverInfo, Long> observerTimes = Maps.newHashMap();
        private int secondsAccumulated = 0;

        public MeasurementSession(UUID playerId, int durationSeconds) {
            this.playerId = playerId;
            this.durationSeconds = durationSeconds;
        }

        public void accumulate(PartInfo part, long durationNs) {
            partTimes.merge(part, durationNs, Long::sum);
        }

        public void accumulateObserver(ObserverInfo observer, long durationNs) {
            observerTimes.merge(observer, durationNs, Long::sum);
        }

        public void incrementSecond() {
            secondsAccumulated++;
        }

        public boolean isComplete() {
            return secondsAccumulated >= durationSeconds;
        }

        public Map<PartInfo, Long> getPartTimes() {
            return partTimes;
        }

        public Map<ObserverInfo, Long> getObserverTimes() {
            return observerTimes;
        }

        public int getDurationSeconds() {
            return durationSeconds;
        }
    }

    /**
     * Information about a network part for tracking.
     */
    private static class PartInfo {
        public final String partName;
        public final String dimension;
        public final int x, y, z;

        public PartInfo(String partName, String dimension, int x, int y, int z) {
            this.partName = partName;
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PartInfo partInfo = (PartInfo) o;
            return x == partInfo.x && y == partInfo.y && z == partInfo.z &&
                    partName.equals(partInfo.partName) && dimension.equals(partInfo.dimension);
        }

        @Override
        public int hashCode() {
            return (31 * (31 * (31 * (31 * partName.hashCode() + dimension.hashCode()) + x) + y) + z);
        }
    }

    /**
     * Information about a network observer for tracking.
     */
    private static class ObserverInfo {
        public final String componentName;
        public final String dimension;
        public final int x, y, z;

        public ObserverInfo(String componentName, String dimension, int x, int y, int z) {
            this.componentName = componentName;
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ObserverInfo that = (ObserverInfo) o;
            return x == that.x && y == that.y && z == that.z &&
                    componentName.equals(that.componentName) && dimension.equals(that.dimension);
        }

        @Override
        public int hashCode() {
            return (31 * (31 * (31 * (31 * componentName.hashCode() + dimension.hashCode()) + x) + y) + z);
        }
    }

}
