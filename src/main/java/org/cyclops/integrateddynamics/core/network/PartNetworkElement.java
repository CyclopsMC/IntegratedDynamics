package org.cyclops.integrateddynamics.core.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.PartStateException;
import org.cyclops.integrateddynamics.api.network.IEnergyConsumingNetworkElement;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetworkElement;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A network element for parts.
 * @author rubensworks
 */
public class PartNetworkElement<P extends IPartType<P, S>, S extends IPartState<P>> extends NetworkElementBase
        implements IPartNetworkElement<P, S>, IEnergyConsumingNetworkElement {

    private final P part;
    private final PartPos center;

    private S tempState = null;

    public PartNetworkElement(P part, PartPos center) {
        this.part = part;
        this.center = center;
    }

    protected static DimPos getCenterPos(PartTarget target) {
        return target.getCenter().getPos();
    }

    protected static Direction getCenterSide(PartTarget target) {
        return target.getCenter().getSide();
    }

    protected static DimPos getTargetPos(PartTarget target) {
        return target.getTarget().getPos();
    }

    protected static Direction getTargetSide(PartTarget target) {
        return target.getTarget().getSide();
    }

    @Override
    public P getPart() {
        return part;
    }

    @Override
    public PartTarget getTarget() {
        return getPart().getTarget(this.center, getPartState());
    }

    public PartTarget getTarget(S partState) {
        return getPart().getTarget(this.center, partState);
    }

    public S getTempState() {
        return tempState;
    }

    public void setTempState(S tempState) {
        this.tempState = tempState;
    }

    @Override
    public IPartContainer getPartContainer() {
        return PartHelpers.getPartContainerChecked(this.center.getPos(), this.center.getSide());
    }

    @Override
    public void setPriorityAndChannel(INetwork network, int priority, int channel) {
        //noinspection deprecation
        part.setPriorityAndChannel(network, NetworkHelpers.getPartNetworkChecked(network), getTarget(), getPartState(), priority, channel);
    }

    @Override
    public int getPriority() {
        return hasPartState() ? part.getPriority(getPartState()) : 0;
    }

    @Override
    public int getChannel() {
        return hasPartState() ? part.getChannel(getPartState()) : IPositionedAddonsNetwork.DEFAULT_CHANNEL;
    }

    @Override
    public boolean canRevalidate(INetwork network) {
        return canRevalidatePositioned(network, this.center.getPos());
    }

    @Override
    public void revalidate(INetwork network) {
        super.revalidate(network);
        revalidatePositioned(network, this.center.getPos());
    }

    @Override
    public boolean isLoaded() {
        return this.center.getPos().isLoaded();
    }

    public boolean hasPartState() {
        if (isLoaded()) {
            IPartContainer partContainer = getPartContainer();
            return partContainer != null && partContainer.hasPart(this.center.getSide());
        }
        return false;
    }

    @Override
    public S getPartState() throws PartStateException {
        IPartContainer partContainer = getPartContainer();
        if(partContainer != null) {
            return (S) partContainer.getPartState(this.center.getSide());
        } else {
            throw new PartStateException(this.center.getPos(), this.center.getSide());
        }
    }

    @Override
    public int getConsumptionRate() {
        return getPart().getConsumptionRate(getPartState());
    }

    @Override
    public void postUpdate(INetwork network, boolean updated) {
        part.postUpdate(network, NetworkHelpers.getPartNetworkChecked(network), getTarget(), getPartState(), updated);
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
    public void update(INetwork network) {
        part.update(network, NetworkHelpers.getPartNetworkChecked(network), getTarget(), getPartState());
    }

    @Override
    public void beforeNetworkKill(INetwork network) {
        part.beforeNetworkKill(network, NetworkHelpers.getPartNetworkChecked(network), getTarget(), getPartState());
    }

    @Override
    public void afterNetworkAlive(INetwork network) {
        part.afterNetworkAlive(network, NetworkHelpers.getPartNetworkChecked(network), getTarget(), getPartState());
    }

    @Override
    public void afterNetworkReAlive(INetwork network) {
        part.afterNetworkReAlive(network, NetworkHelpers.getPartNetworkChecked(network), getTarget(), getPartState());
    }

    @Override
    public void addDrops(List<ItemStack> itemStacks, boolean dropMainElement, boolean saveState) {
        part.addDrops(getTarget(), getPartState(), itemStacks, dropMainElement, saveState);
    }

    @Override
    public boolean onNetworkAddition(INetwork network) {
        IPartNetwork partNetwork = NetworkHelpers.getPartNetworkChecked(network);
        boolean res = partNetwork.addPart(getPartState().getId(), this.center);
        if(res) {
            part.onNetworkAddition(network, partNetwork, getTarget(), getPartState());
        }
        return res;
    }

    @Override
    public void onNetworkRemoval(INetwork network) {
        IPartNetwork partNetwork = NetworkHelpers.getPartNetworkChecked(network);
        partNetwork.removePart(getPartState().getId());
        part.onNetworkRemoval(network, partNetwork, getTarget(), getPartState());
    }

    @Override
    public void onPreRemoved(INetwork network) {
        part.onPreRemoved(network, NetworkHelpers.getPartNetworkChecked(network), getTarget(), (tempState = getPartState()));
    }

    @Override
    public void onPostRemoved(INetwork network) {
        part.onPostRemoved(network, NetworkHelpers.getPartNetworkChecked(network), getTarget(tempState), Objects.requireNonNull(tempState));
        tempState = null;
    }

    @Override
    public void onNeighborBlockChange(@Nullable INetwork network, BlockGetter world, Block neighbourBlock,
                                      BlockPos neighbourBlockPos) {
        part.onBlockNeighborChange(network, NetworkHelpers.getPartNetworkChecked(network), getTarget(), getPartState(), world,
                neighbourBlock, neighbourBlockPos);
    }

    @Override
    public Optional<P> getNetworkEventListener() {
        return Optional.of(getPart());
    }

    public boolean equals(Object o) {
        return o instanceof IPartNetworkElement && compareTo((INetworkElement) o) == 0;
    }

    @Override
    public int hashCode() {
        int result = part.hashCode();
        result = 31 * result + this.center.hashCode();
        return result;
    }

    @Override
    public int compareTo(INetworkElement o) {
        if (o instanceof IPartNetworkElement) {
            IPartNetworkElement p = (IPartNetworkElement) o;
            int compClass = this.getPart().getUniqueName().compareTo(p.getPart().getUniqueName());
            if (compClass == 0) {
                // If this or the other part is not loaded, we IGNORE the priority,
                // because that depends on tile entity data, which requires loading the part/chunk.
                int compPriority = !isLoaded() || !p.isLoaded() ? 0 : -Integer.compare(this.getPriority(), p.getPriority());
                if (compPriority == 0) {
                    int compPart = getPart().getTranslationKey().compareTo(p.getPart().getTranslationKey());
                    if (compPart == 0) {
                        int compPos = this.center.getPos().compareTo(p.getPosition());
                        if (compPos == 0) {
                            return this.center.getSide().compareTo(p.getSide());
                        }
                        return compPos;
                    }
                    return compPart;
                } else {
                    return compPriority;
                }
            } else {
                return compClass;
            }
        }

        return this.getClass().getName().compareTo(o.getClass().getName());
    }

    @Override
    public DimPos getPosition() {
        return this.center.getPos();
    }

    @Override
    public Direction getSide() {
        return this.center.getSide();
    }

    @Override
    public int getId() {
        if (!hasPartState()) {
            return -1;
        }
        return getPartState().getId();
    }

    @Override
    public ResourceLocation getGroup() {
        return IPartNetworkElement.GROUP;
    }
}
