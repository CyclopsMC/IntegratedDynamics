package org.cyclops.integrateddynamics.network;

import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.network.IEventListenableNetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.network.TileNetworkElement;
import org.cyclops.integrateddynamics.tileentity.TileProxy;

import javax.annotation.Nullable;

/**
 * Network element for coal generators.
 * @author rubensworks
 */
public class ProxyNetworkElement extends TileNetworkElement<TileProxy> implements IEventListenableNetworkElement<IPartNetwork, TileProxy> {

    public ProxyNetworkElement(DimPos pos) {
        super(pos);
    }

    protected int getId() {
        return getTile().getProxyId();
    }

    @Override
    public boolean onNetworkAddition(IPartNetwork network) {
        if(super.onNetworkAddition(network)) {
            if(!network.addProxy(getId(), getPos())) {
                IntegratedDynamics.clog(Level.WARN, "A proxy already existed in the network, this is possibly a " +
                        "result from item duplication.");
                getTile().generateNewProxyId();
                return network.addProxy(getId(), getPos());
            }
            return true;
        }
        return false;
    }

    @Override
    public void onNetworkRemoval(IPartNetwork network) {
        super.onNetworkRemoval(network);
        network.removeProxy(getId());
    }

    @Override
    public int getConsumptionRate() {
        return 2;
    }

    @Nullable
    @Override
    public TileProxy getNetworkEventListener() {
        return getTile();
    }

    @Override
    protected Class<TileProxy> getTileClass() {
        return TileProxy.class;
    }
}
