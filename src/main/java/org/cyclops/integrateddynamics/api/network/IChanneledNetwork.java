package org.cyclops.integrateddynamics.api.network;

/**
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
     */
    public static boolean channelsMatch(int first, int second) {
        return first == second || first == WILDCARD_CHANNEL || second == WILDCARD_CHANNEL;
    }

    /**
     * @return A T that only interacts with the given channel.
     */
    public T getChannel(int channel);
}
