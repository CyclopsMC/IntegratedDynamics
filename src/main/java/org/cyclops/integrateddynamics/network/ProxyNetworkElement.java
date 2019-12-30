package org.cyclops.integrateddynamics.network;

import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.network.IEventListenableNetworkElement;
import org.cyclops.integrateddynamics.api.network.IIdentifiableNetworkElement;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.TileNetworkElement;
import org.cyclops.integrateddynamics.tileentity.TileProxy;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Network element for coal generators.
 * @author rubensworks
 */
public class ProxyNetworkElement extends TileNetworkElement<TileProxy> implements
        IEventListenableNetworkElement<TileProxy>, IIdentifiableNetworkElement {

    public static final ResourceLocation GROUP = new ResourceLocation(Reference.MOD_ID, "proxy");

    public ProxyNetworkElement(DimPos pos) {
        super(pos);
    }

    @Override
    public int getId() {
        return getTile().get().getProxyId();
    }

    @Override
    public ResourceLocation getGroup() {
        return ProxyNetworkElement.GROUP;
    }

    @Override
    public boolean onNetworkAddition(INetwork network) {
        if(super.onNetworkAddition(network)) {
            return NetworkHelpers.getPartNetwork(network)
                    .map(partNetwork -> {
                        if(!partNetwork.addProxy(getId(), getPos())) {
                            IntegratedDynamics.clog(Level.WARN, "A proxy already existed in the network, this is possibly a " +
                                    "result from item duplication.");
                            getTile().get().generateNewProxyId();
                            return partNetwork.addProxy(getId(), getPos());
                        }
                        return true;
                    })
                    .orElse(false);
        }
        return false;
    }

    @Override
    public void onNetworkRemoval(INetwork network) {
        super.onNetworkRemoval(network);
        NetworkHelpers.getPartNetwork(network)
                .ifPresent(partNetwork -> partNetwork.removeProxy(getId()));
    }

    @Override
    public void setPriorityAndChannel(INetwork network, int priority, int channel) {

    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getChannel() {
        return IPositionedAddonsNetwork.DEFAULT_CHANNEL;
    }

    @Override
    public int getConsumptionRate() {
        return 2;
    }

    @Nullable
    @Override
    public Optional<TileProxy> getNetworkEventListener() {
        return getTile();
    }

    @Override
    protected Class<TileProxy> getTileClass() {
        return TileProxy.class;
    }
}
