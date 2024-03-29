package org.cyclops.integrateddynamics.core.network;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPositionedNetworkElement;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityCableConnectableInventory;

import java.util.Optional;

/**
 * Network element for part entities.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public abstract class TileNetworkElement<T extends BlockEntityCableConnectableInventory> extends ConsumingNetworkElementBase
        implements IPositionedNetworkElement {

    private final DimPos pos;

    protected abstract Class<T> getTileClass();

    protected Optional<T> getTile() {
        return BlockEntityHelpers.get(getPos(), getTileClass());
    }

    @Override
    public int compareTo(INetworkElement o) {
        if(o instanceof TileNetworkElement) {
            return getPos().compareTo(((TileNetworkElement) o).getPos());
        }
        return this.getClass().getCanonicalName().compareTo(o.getClass().getCanonicalName());
    }

    @Override
    public void afterNetworkReAlive(INetwork network) {
        super.afterNetworkReAlive(network);
        getTile().ifPresent(T::afterNetworkReAlive);
    }

    @Override
    public boolean canRevalidate(INetwork network) {
        return canRevalidatePositioned(network, pos);
    }

    @Override
    public void revalidate(INetwork network) {
        super.revalidate(network);
        revalidatePositioned(network, pos);
    }

    @Override
    public DimPos getPosition() {
        return this.pos;
    }
}
