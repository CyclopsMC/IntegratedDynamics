package org.cyclops.integrateddynamics.network;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.IEventListenableNetworkElement;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.core.network.TileNetworkElement;
import org.cyclops.integrateddynamics.tileentity.TileMaterializer;

import java.util.Optional;

/**
 * Network element for materializers.
 * @author rubensworks
 */
public class MaterializerNetworkElement extends TileNetworkElement<TileMaterializer> implements IEventListenableNetworkElement<TileMaterializer> {

    public MaterializerNetworkElement(DimPos pos) {
        super(pos);
    }

    @Override
    public Optional<TileMaterializer> getNetworkEventListener() {
        return getTile();
    }

    @Override
    protected Class<TileMaterializer> getTileClass() {
        return TileMaterializer.class;
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
}
