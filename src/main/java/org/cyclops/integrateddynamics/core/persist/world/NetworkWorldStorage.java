package org.cyclops.integrateddynamics.core.persist.world;

import com.google.common.collect.Sets;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.persist.world.WorldStorage;
import org.cyclops.integrateddynamics.core.network.Network;

import java.util.Set;

/**
 * World NBT storage for all active networks.
 * @author rubensworks
 */
public class NetworkWorldStorage extends WorldStorage {

    private static NetworkWorldStorage INSTANCE = null;

    @NBTPersist
    private Set<Network> networks = Sets.newHashSet();

    private NetworkWorldStorage(ModBase mod) {
        super(mod);
    }

    public static NetworkWorldStorage getInstance(ModBase mod) {
        if(INSTANCE == null) {
            INSTANCE = new NetworkWorldStorage(mod);
        }
        return INSTANCE;
    }

    @Override
    public void reset() {
        networks.clear();
    }

    @Override
    protected String getDataId() {
        return "Networks";
    }

    /**
     * Add a network that needs persistence.
     * @param network The network.
     */
    public void addNewNetwork(Network network) {
        networks.add(network);
    }

    /**
     * Remove a network that was invalidated and does not need persistence anymore.
     * This is allowed to be called if the network was already removed.
     * @param network The network.
     */
    public void removeInvalidatedNetwork(Network network) {
        networks.remove(network);
    }
}
