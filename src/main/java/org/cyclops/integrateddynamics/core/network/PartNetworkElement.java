package org.cyclops.integrateddynamics.core.network;

import lombok.Data;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.part.IPartContainerFacade;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;

import java.util.List;

/**
 * A network element for parts.
 * @author rubensworks
 */
@Data
public class PartNetworkElement<P extends IPartType<P, S>, S extends IPartState<P>> implements INetworkElement {

    private final P part;
    private final IPartContainerFacade partContainerFacade;
    private final DimPos pos;
    private final EnumFacing side;

    /**
     * @return The state for this part.
     */
    public S getPartState() {
        return (S) partContainerFacade.getPartContainer(pos.getWorld(), pos.getBlockPos()).getPartState(side);
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
    public void update() {
        part.update(getPartState());
    }

    @Override
    public void beforeNetworkKill() {
        part.beforeNetworkKill(getPartState());
    }

    @Override
    public void afterNetworkAlive() {
        part.afterNetworkAlive(getPartState());
    }

    @Override
    public void addDrops(List<ItemStack> itemStacks) {
        part.addDrops(getPartState(), itemStacks);
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
                int compPos = pos.compareTo(p.pos);
                if(compPos == 0) {
                    return side.compareTo(p.side);
                }
                return compPos;
            }
            return compPart;
        }
        return Integer.compare(hashCode(), o.hashCode());
    }
}
