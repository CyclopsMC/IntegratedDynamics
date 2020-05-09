package org.cyclops.integrateddynamics.api.network;

import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PrioritizedPartPos;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A network that can hold prioritized positions.
 * @author rubensworks
 */
public interface IPositionedAddonsNetwork {

    /**
     * The channel that should be used for anything that doesn't allow the player to select a channel.
     */
    public static final int DEFAULT_CHANNEL = 0;

    /**
     * Specifying this channel will allow interaction with all channels.
     */
    public static final int WILDCARD_CHANNEL = -1;

    /**
     * Whether two parts on the given channels may interact.
     * @param first The id of the first channel.
     * @param second The id of the second channel.
     * @return If the two channels match.
     */
    public static boolean channelsMatch(int first, int second) {
        return first == second || first == WILDCARD_CHANNEL || second == WILDCARD_CHANNEL;
    }

    /**
     * @return The channels that have at least one active position.
     */
    public int[] getChannels();

    /**
     * @return If any positioned addons are present in this network.
     */
    public boolean hasPositions();

    /**
     * @param channel The channel id.
     * @return The stored positions, sorted by priority.
     */
    public Collection<PrioritizedPartPos> getPrioritizedPositions(int channel);

    /**
     * Get the channel this position is present in.
     * -1 if it is not present in any channel.
     * @param pos A position.
     * @return A channel.
     */
    public int getPositionChannel(PartPos pos);

    /**
     * @param channel The channel id.
     * @return The stored positions, sorted by priority.
     */
    public default Collection<PartPos> getPositions(int channel) {
        return getPrioritizedPositions(channel).stream().map(PrioritizedPartPos::getPartPos).collect(Collectors.toList());
    }

    /**
     * @return All stored positions, order is undefined.
     */
    public Collection<PrioritizedPartPos> getPrioritizedPositions();

    /**
     * @return All stored positions, order is undefined.
     */
    public default Collection<PartPos> getPositions() {
        return getPrioritizedPositions().stream().map(PrioritizedPartPos::getPartPos).collect(Collectors.toList());
    }

    /**
     * @return The part positions iterator handler for this network.
     */
    @Nullable
    public IPartPosIteratorHandler getPartPosIteratorHandler();

    /**
     * Set a part positions iterator handler for this network.
     * @param iteratorHandler An iterator handler or null if it should be reset.
     */
    public void setPartPosIteratorHandler(@Nullable IPartPosIteratorHandler iteratorHandler);

    /**
     * Add the given position.
     * @param pos The position.
     * @param priority The priority.
     * @param channel The channel id.
     * @return If the position was added, otherwise it was already present.
     */
    public boolean addPosition(PartPos pos, int priority, int channel);

    /**
     * Remove the given position.
     * @param pos The position.
     */
    public void removePosition(PartPos pos);

    /**
     * Check if the given position is disabled.
     * @param pos The position.
     * @return If it is disabled.
     */
    public boolean isPositionDisabled(PartPos pos);

    /**
     * Disable a position.
     * @param pos The position.
     */
    public void disablePosition(PartPos pos);

    /**
     * Enable a position.
     * @param pos The position.
     */
    public void enablePosition(PartPos pos);

}
