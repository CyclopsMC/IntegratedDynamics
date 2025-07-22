package org.cyclops.integrateddynamics.core.persist.world;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.apache.commons.compress.utils.Lists;
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

    private static NetworkWorldStorage INSTANCE = null;

    private List<NetworkParams> networkParams;
    private Set<Network> networks = Sets.newHashSet();

    private NetworkWorldStorage(ModBaseNeoForge mod) {
        super(mod);
        this.networkParams = Lists.newArrayList();
    }

    public NetworkWorldStorage(ModBaseNeoForge mod, List<NetworkParams> networkParams) {
        super(mod);
        this.networkParams = networkParams;
    }

    public static NetworkWorldStorage getInstance(ModBaseNeoForge mod) {
        if(INSTANCE == null) {
            INSTANCE = new NetworkWorldStorage(mod);
        }
        return INSTANCE;
    }

    @Override
    public void reset() {
        networkParams.clear();
    }

    @Override
    protected SavedDataType<NetworkWorldStorage> constructSavedDataType() {
        return new SavedDataType<>(
                this.mod.getModId() + "_networks",
                (ctx) -> new NetworkWorldStorage(this.mod),
                ctx -> RecordCodecBuilder.create(instance -> instance.group(
                        RecordCodecBuilder.point(ctx.levelOrThrow()),
                        Codec.list(NetworkParams.CODEC).fieldOf("networks").forGetter(data -> data.networkParams)
                ).apply(instance, (level, networkParams) -> new NetworkWorldStorage(this.mod, networkParams)))
        );
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
    public synchronized Set<INetwork> getNetworks() {
        return Collections.unmodifiableSet(Sets.newHashSet(networks));
    }

    @Override
    public void afterLoad() {
        // Load from params
        networks.clear();
        for (NetworkParams networkParam : networkParams) {
            Network network = new Network();
            network.fromParams(networkParam);
            networks.add(network);
        }

        TickHandler.getInstance().ticked = false;
        for(INetwork network : networks) {
            network.afterServerLoad();
        }
    }

    @Override
    public void beforeSave() {
        for(Network network : networks) {
            network.beforeServerStop();
        }

        // Save to params
        this.networkParams.clear();
        for(Network network : networks) {
            // Save to params
            this.networkParams.add(network.toParams());
        }
    }

}
