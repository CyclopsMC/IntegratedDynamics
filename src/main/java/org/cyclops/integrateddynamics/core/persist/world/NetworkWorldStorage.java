package org.cyclops.integrateddynamics.core.persist.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.cyclops.cyclopscore.init.ModBaseNeoForge;
import org.cyclops.cyclopscore.persist.world.WorldStorage;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.core.TickHandler;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.network.NetworkParams;

import java.util.Collections;
import java.util.List;
import java.util.Set;
/**
 * World NBT storage for all active networks.
 * @author rubensworks
 */
public class NetworkWorldStorage extends WorldStorage<NetworkWorldStorage> {

    private final List<NetworkParams> networkParams;
    private Set<Network> networks = Sets.newHashSet();
    private Set<INetwork> unmodifiableSafeNetworks = null;

    public NetworkWorldStorage(List<NetworkParams> networkParams) {
        this.networkParams = Lists.newArrayList(networkParams);
    }

    public List<NetworkParams> getNetworkParams() {
        return networkParams;
    }

    /**
     * Add a network that needs persistence.
     * @param network The network.
     */
    public synchronized void addNewNetwork(Network network) {
        if (networks.add(network)) {
            unmodifiableSafeNetworks = null;
            setDirty();
        }
    }

    /**
     * Remove a network that was invalidated and does not need persistence anymore.
     * This is allowed to be called if the network was already removed.
     * @param network The network.
     */
    public synchronized void removeInvalidatedNetwork(Network network) {
        if (networks.remove(network)) {
            unmodifiableSafeNetworks = null;
            setDirty();
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
        // Load from params
        for (NetworkParams networkParam : networkParams) {
            Network network = new Network();
            network.fromParams(networkParam);
            networks.add(network);
        }

        TickHandler.getInstance().ticked = false;
        for (INetwork network : networks) {
            network.afterServerLoad();
        }
    }

    @Override
    public void beforeSave() {
        for(Network network : networks) {
            network.beforeServerStop();
        }

        // Save to params
        for(Network network : networks) {
            // Save to params
            this.networkParams.add(network.toParams());
        }

        // Consider always dirty
        setDirty();
    }

    public static class Access extends WorldStorage.Access<NetworkWorldStorage> {

        private static NetworkWorldStorage.Access INSTANCE = null;

        public static NetworkWorldStorage.Access getInstance(ModBaseNeoForge<?> mod) {
            if(INSTANCE == null) {
                INSTANCE = new NetworkWorldStorage.Access(mod);
            }
            return INSTANCE;
        }

        public Access(ModBaseNeoForge<?> mod) {
            super(new SavedDataType<NetworkWorldStorage>(
                    Identifier.fromNamespaceAndPath(mod.getModId(), "networks"),
                    (ctx) -> new NetworkWorldStorage(Lists.newArrayList()),
                    ctx -> RecordCodecBuilder.create(instance -> instance.group(
                            RecordCodecBuilder.point(ctx.getLevel()),
                            Codec.list(NetworkParams.CODEC).fieldOf("networks").forGetter(data -> data.getNetworkParams())
                    ).apply(instance, (level, networkParams) -> new NetworkWorldStorage(networkParams)))
            ), mod);
        }
    }
}
