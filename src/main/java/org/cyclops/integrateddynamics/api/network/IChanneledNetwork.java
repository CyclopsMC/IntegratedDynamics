package org.cyclops.integrateddynamics.api.network;

/**
 * A bus of channels within a network.
 * This allows instances of type T to be retrieved from channel ids.
 * @param <T> The channel type.
 * @author josephcsible
 */
public interface IChanneledNetwork<T> {
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
     * Get the channel from the given channel id.
     * @param channel The channel id.
     * @return A T that only interacts with the given channel.
     */
    public T getChannel(int channel);
}
