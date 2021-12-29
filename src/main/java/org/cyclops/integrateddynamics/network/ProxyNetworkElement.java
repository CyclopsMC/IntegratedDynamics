package org.cyclops.integrateddynamics.network;

import net.minecraft.resources.ResourceLocation;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.network.IEventListenableNetworkElement;
import org.cyclops.integrateddynamics.api.network.IIdentifiableNetworkElement;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.blockentity.BlockEntityProxy;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.TileNetworkElement;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Network element for coal generators.
 * @author rubensworks
 */
public class ProxyNetworkElement extends TileNetworkElement<BlockEntityProxy> implements
        IEventListenableNetworkElement<BlockEntityProxy>, IIdentifiableNetworkElement {

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
                            IntegratedDynamics.clog(org.apache.logging.log4j.Level.WARN, "A proxy already existed in the network, this is possibly a " +
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
        return GeneralConfig.proxyBaseConsumption;
    }

    @Nullable
    @Override
    public Optional<BlockEntityProxy> getNetworkEventListener() {
        return getTile();
    }

    @Override
    protected Class<BlockEntityProxy> getTileClass() {
        return BlockEntityProxy.class;
    }
}
