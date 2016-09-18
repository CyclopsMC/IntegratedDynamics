package org.cyclops.integrateddynamics.core.persist.world;

import com.google.common.collect.Sets;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.persist.world.WorldStorage;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.core.network.Network;

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
    public void readGeneratedFieldsFromNBT(NBTTagCompound tag) {
        // TODO: backwards compat, remove in next major MC update.
        if (tag.hasKey("networks", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal())
                && "org.cyclops.integrateddynamics.core.network.PartNetwork".equals(tag.getCompoundTag("networks").getString("elementType"))) {
            NBTTagCompound collectionTag = tag.getCompoundTag("networks");
            networks = Sets.newHashSet();
            NBTTagList list = collectionTag.getTagList("collection", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal());
            if(list.tagCount() > 0) {
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound entryTag = list.getCompoundTagAt(i);
                    Network network = new Network();
                    network.fromNBT(entryTag.getCompoundTag("element"));
                    networks.add(network);
                }
            }
        } else {
            super.readGeneratedFieldsFromNBT(tag);
        }
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
    public synchronized void addNewNetwork(INetwork network) {
        networks.add(network);
    }

    /**
     * Remove a network that was invalidated and does not need persistence anymore.
     * This is allowed to be called if the network was already removed.
     * @param network The network.
     */
    public synchronized void removeInvalidatedNetwork(INetwork network) {
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
        for(INetwork network : networks) {
            network.afterServerLoad();
        }
    }

    @Override
    public void beforeSave() {
        for(INetwork network : networks) {
            network.beforeServerStop();
        }
    }

}
