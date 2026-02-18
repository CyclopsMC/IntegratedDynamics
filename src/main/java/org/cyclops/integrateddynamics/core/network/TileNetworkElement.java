package org.cyclops.integrateddynamics.core.network;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPositionedNetworkElement;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityCableConnectableInventory;

import java.util.Objects;
import java.util.Optional;

/**
 * Network element for part entities.
 * @author rubensworks
 */
public abstract class TileNetworkElement<T extends BlockEntityCableConnectableInventory> extends ConsumingNetworkElementBase
        implements IPositionedNetworkElement {

    private final DimPos pos;

    public TileNetworkElement(DimPos pos) {
        this.pos = pos;
    }

    public DimPos getPos() {
        return pos;
    }

    protected abstract Class<T> getTileClass();

    protected Optional<T> getTile() {
        return IModHelpers.get().getBlockEntityHelpers().get(getPos().getLevel(true), getPos().getBlockPos(), getTileClass());
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

    @Override
    public boolean isLoaded() {
        return INetworkElement.shouldTick(this.getPos());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TileNetworkElement<?> that = (TileNetworkElement<?>) o;
        return Objects.equals(pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pos);
    }
}
