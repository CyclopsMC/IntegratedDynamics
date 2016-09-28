package org.cyclops.integrateddynamics.network;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.IEventListenableNetworkElement;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.core.network.TileNetworkElement;
import org.cyclops.integrateddynamics.tileentity.TileMaterializer;

import javax.annotation.Nullable;

/**
 * Network element for materializers.
 * @author rubensworks
 */
public class MaterializerNetworkElement extends TileNetworkElement<TileMaterializer> implements IEventListenableNetworkElement<TileMaterializer> {

    public MaterializerNetworkElement(DimPos pos) {
        super(pos);
    }

    @Nullable
    @Override
    public TileMaterializer getNetworkEventListener() {
        return getTile();
    }

    @Override
    protected Class<TileMaterializer> getTileClass() {
        return TileMaterializer.class;
    }

    @Override
    public void setPriority(INetwork network, int priority) {

    }

    @Override
    public int getPriority() {
        return 0;
    }
}
