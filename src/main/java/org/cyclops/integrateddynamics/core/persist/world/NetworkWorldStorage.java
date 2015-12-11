package org.cyclops.integrateddynamics.core.persist.world;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.persist.world.WorldStorage;
import org.cyclops.integrateddynamics.api.network.INetwork;

import java.util.Set;

/**
 * World NBT storage for all active networks.
 * @author rubensworks
 */
public class NetworkWorldStorage extends WorldStorage {

    private static NetworkWorldStorage INSTANCE = null;

    @NBTPersist
    private Set<INetwork<?>> networks = Sets.newHashSet();

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
    public synchronized void addNewNetwork(INetwork<?> network) {
        networks.add(network);
    }

    /**
     * Remove a network that was invalidated and does not need persistence anymore.
     * This is allowed to be called if the network was already removed.
     * @param network The network.
     */
    public synchronized void removeInvalidatedNetwork(INetwork<?> network) {
        networks.remove(network);
    }

    /**
     * @return A thread-safe copy of the current network set.
     */
    public synchronized Set<INetwork<?>> getNetworks() {
        return ImmutableSet.copyOf(networks);
    }

    @Override
    public void afterLoad() {
        for(INetwork<?> network : networks) {
            network.afterServerLoad();
        }
    }

    @Override
    public void beforeSave() {
        for(INetwork<?> network : networks) {
            network.beforeServerStop();
        }
    }

}
