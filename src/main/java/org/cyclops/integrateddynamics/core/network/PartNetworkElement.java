package org.cyclops.integrateddynamics.core.network;

import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.IEnergyConsumingNetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetworkElement;
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
public class PartNetworkElement<P extends IPartType<P, S>, S extends IPartState<P>> implements IPartNetworkElement<P, S>, IEnergyConsumingNetworkElement<IPartNetwork> {

    private final P part;
    private final IPartContainerFacade partContainerFacade;
    private final PartTarget target;

    protected static DimPos getCenterPos(PartTarget target) {
        return target.getCenter().getPos();
    }

    protected static EnumFacing getCenterSide(PartTarget target) {
        return target.getCenter().getSide();
    }

    protected static DimPos getTargetPos(PartTarget target) {
        return target.getTarget().getPos();
    }

    protected static EnumFacing getTargetSide(PartTarget target) {
        return target.getTarget().getSide();
    }

    @Override
    public S getPartState() {
        return (S) partContainerFacade.getPartContainer(getCenterPos(getTarget()).getWorld(), getCenterPos(getTarget()).getBlockPos()).
               getPartState(getCenterSide(getTarget()));
    }

    @Override
    public int getConsumptionRate() {
        return getPart().getConsumptionRate(getPartState());
    }

    @Override
    public void postUpdate(IPartNetwork network, boolean updated) {
        part.postUpdate(network, getTarget(), getPartState(), updated);
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
    public void update(IPartNetwork network) {
        part.update(network, getTarget(), getPartState());
    }

    @Override
    public void beforeNetworkKill(IPartNetwork network) {
        part.beforeNetworkKill(network, target, getPartState());
    }

    @Override
    public void afterNetworkAlive(IPartNetwork network) {
        part.afterNetworkAlive(network, target, getPartState());
    }

    @Override
    public void addDrops(List<ItemStack> itemStacks) {
        part.addDrops(getTarget(), getPartState(), itemStacks);
    }

    @Override
    public boolean onNetworkAddition(IPartNetwork network) {
        boolean res = network.addPart(getPartState().getId(), getTarget().getCenter());
        if(res) {
            part.onNetworkAddition(network, target, getPartState());
        }
        return res;
    }

    @Override
    public void onNetworkRemoval(IPartNetwork network) {
        network.removePart(getPartState().getId());
        part.onNetworkRemoval(network, target, getPartState());
    }

    @Override
    public void onPreRemoved(IPartNetwork network) {
        part.onPreRemoved(network, target, getPartState());
    }

    @Override
    public void onNeighborBlockChange(IPartNetwork network, IBlockAccess world, Block neighborBlock) {
        part.onBlockNeighborChange(network, target, getPartState(), world, neighborBlock);
    }

    @Override
    public P getNetworkEventListener() {
        return getPart();
    }

    public boolean equals(Object o) {
        return o instanceof IPartNetworkElement && compareTo((INetworkElement) o) == 0;
    }

    @Override
    public int hashCode() {
        int result = part.hashCode();
        result = 31 * result + partContainerFacade.hashCode();
        result = 31 * result + target.hashCode();
        return result;
    }

    @Override
    public int compareTo(INetworkElement o) {
        if(o instanceof IPartNetworkElement) {
            IPartNetworkElement p = (IPartNetworkElement) o;
            int compPart = Integer.compare(part.hashCode(), p.getPart().hashCode());
            if(compPart == 0) {
                int compPos = getCenterPos(getTarget()).compareTo(getCenterPos(p.getTarget()));
                if(compPos == 0) {
                    return getCenterSide(getTarget()).compareTo(getCenterSide(p.getTarget()));
                }
                return compPos;
            }
            return compPart;
        }
        return Integer.compare(hashCode(), o.hashCode());
    }
}
