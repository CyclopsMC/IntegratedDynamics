package org.cyclops.integrateddynamics.core.persist.world;

import com.google.common.collect.Sets;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.persist.world.WorldStorage;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.core.TickHandler;

import java.util.Collections;
import java.util.Set;

/**
 * World NBT storage for all active networks.
 * @author rubensworks
 */
public class NetworkWorldStorage extends WorldStorage {

    private static NetworkWorldStorage INSTANCE = null;

    @NBTPersist
    private Set<INetwork> networks = Sets.newHashSet();

    private Set<INetwork> unmodifiableSafeNetworks = null;

    private NetworkWorldStorage(ModBase mod) {
        super(mod);
    }

    public static NetworkWorldStorage getInstance(ModBase mod) {
        if (INSTANCE == null) {
            INSTANCE = new NetworkWorldStorage(mod);
        }
        return INSTANCE;
    }

    @Override
    public void readGeneratedFieldsFromNBT(CompoundTag arg0, HolderLookup.Provider arg1) {
        super.readGeneratedFieldsFromNBT(arg0, arg1);
        unmodifiableSafeNetworks = null;
    }

    @Override
    public void reset() {
        networks.clear();
        unmodifiableSafeNetworks = null;
    }

    @Override
    protected String getDataId() {
        return "Networks";
    }

    /**
     * Add a network that needs persistence.
     * @param network The network.
     */
    public synchronized void addNewNetwork(INetwork network) {
        if (networks.add(network)) {
            unmodifiableSafeNetworks = null;
        }
    }

    /**
     * Remove a network that was invalidated and does not need persistence anymore.
     * This is allowed to be called if the network was already removed.
     * @param network The network.
     */
    public synchronized void removeInvalidatedNetwork(INetwork network) {
        if (networks.remove(network)) {
            unmodifiableSafeNetworks = null;
        }
    }

    /**
     * @return A thread-safe copy of the current network set.
     */
    public synchronized Set<INetwork> getNetworks() {
        if (unmodifiableSafeNetworks == null) {
            unmodifiableSafeNetworks = Collections.unmodifiableSet(Sets.newHashSet(networks));
        }
        return unmodifiableSafeNetworks;
    }

    @Override
    public void afterLoad() {
        TickHandler.getInstance().ticked = false;
        for (INetwork network : networks) {
            network.afterServerLoad();
        }
    }

    @Override
    public void beforeSave() {
        for (INetwork network : networks) {
            network.beforeServerStop();
        }
    }
}
