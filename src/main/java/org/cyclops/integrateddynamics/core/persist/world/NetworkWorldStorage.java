package org.cyclops.integrateddynamics.core.persist.world;

import com.google.common.collect.ImmutableSet;
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
    public synchronized void addNewNetwork(Network network) {
        networks.add(network);
    }

    /**
     * Remove a network that was invalidated and does not need persistence anymore.
     * This is allowed to be called if the network was already removed.
     * @param network The network.
     */
    public synchronized void removeInvalidatedNetwork(Network network) {
        networks.remove(network);
    }

    /**
     * @return A thread-safe copy of the current network set.
     */
    public synchronized Set<Network> getNetworks() {
        //System.out.println(networks);
        return ImmutableSet.copyOf(networks);
    }

    @Override
    public void afterLoad() {
        for(Network network : networks) {
            network.afterServerLoad();
        }
    }

    @Override
    public void beforeSave() {
        for(Network network : networks) {
            network.beforeServerStop();
        }
    }

}
