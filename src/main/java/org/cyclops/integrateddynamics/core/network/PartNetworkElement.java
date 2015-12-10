package org.cyclops.integrateddynamics.core.network;

import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.IEventListenableNetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.part.IPartContainerFacade;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;

import java.util.List;

/**
 * A network element for parts.
 * @author rubensworks
 */
@Data
public class PartNetworkElement<P extends IPartType<P, S>, S extends IPartState<P>> implements IEventListenableNetworkElement<P> {

    private final P part;
    private final IPartContainerFacade partContainerFacade;
    private final PartTarget target;

    protected final DimPos getCenterPos() {
        return getTarget().getCenter().getPos();
    }

    protected final EnumFacing getCenterSide() {
        return getTarget().getCenter().getSide();
    }

    protected final DimPos getTargetPos() {
        return getTarget().getTarget().getPos();
    }

    protected final EnumFacing getTargetSide() {
        return getTarget().getTarget().getSide();
    }

    /**
     * @return The state for this part.
     */
    public S getPartState() {
        return (S) partContainerFacade.getPartContainer(getCenterPos().getWorld(), getCenterPos().getBlockPos()).
               getPartState(getCenterSide());
    }

    @Override
    public int getUpdateInterval() {
        return part.getUpdateInterval(getPartState());
    }

    @Override
    public boolean isUpdate() {
        return part.isUpdate(getPartState());
    }

    @Override
    public void update(Network network) {
        part.update(network, getTarget(), getPartState());
    }

    @Override
    public void beforeNetworkKill(Network network) {
        part.beforeNetworkKill(network, target, getPartState());
    }

    @Override
    public void afterNetworkAlive(Network network) {
        part.afterNetworkAlive(network, target, getPartState());
    }

    @Override
    public void addDrops(List<ItemStack> itemStacks) {
        part.addDrops(getTarget(), getPartState(), itemStacks);
    }

    @Override
    public boolean onNetworkAddition(Network network) {
        boolean res = network.addPart(getPartState().getId(), getTarget().getCenter());
        if(res) {
            part.onNetworkAddition(network, target, getPartState());
        }
        return res;
    }

    @Override
    public void onNetworkRemoval(Network network) {
        network.removePart(getPartState().getId());
        part.onNetworkRemoval(network, target, getPartState());
    }

    @Override
    public void onPreRemoved(Network network) {
        part.onPreRemoved(network, target, getPartState());
    }

    @Override
    public void onNeighborBlockChange(Network network, IBlockAccess world, Block neighborBlock) {
        part.onBlockNeighborChange(network, target, getPartState(), world, neighborBlock);
    }

    @Override
    public P getNetworkEventListener() {
        return getPart();
    }

    public boolean equals(Object o) {
        return o instanceof PartNetworkElement && compareTo((INetworkElement) o) == 0;
    }

    @Override
    public int compareTo(INetworkElement o) {
        if(o instanceof PartNetworkElement) {
            PartNetworkElement p = (PartNetworkElement) o;
            int compPart = Integer.compare(part.hashCode(), p.part.hashCode());
            if(compPart == 0) {
                int compPos = getCenterPos().compareTo(p.getCenterPos());
                if(compPos == 0) {
                    return getCenterSide().compareTo(p.getCenterSide());
                }
                return compPos;
            }
            return compPart;
        }
        return Integer.compare(hashCode(), o.hashCode());
    }
}
