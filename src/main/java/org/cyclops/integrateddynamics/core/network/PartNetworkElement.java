package org.cyclops.integrateddynamics.core.network;

import lombok.Data;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.core.part.IPartContainerFacade;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;

/**
 * A network element for parts.
 * @author rubensworks
 */
@Data
public class PartNetworkElement<P extends IPartType<P, S>, S extends IPartState<P>> implements INetworkElement {

    private final P part;
    private final IPartContainerFacade partContainerFacade;
    private final World world;
    private final BlockPos pos;
    private final EnumFacing side;

    /**
     * @return The state for this part.
     */
    public S getPartState() {
        return (S) partContainerFacade.getPartContainer(world, pos).getPartState(side);
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

    public boolean equals(Object o) {
        return o instanceof PartNetworkElement
                && part == ((PartNetworkElement) o).part
                && partContainerFacade == ((PartNetworkElement) o).partContainerFacade
                && world == ((PartNetworkElement) o).world && pos.equals(((PartNetworkElement) o).pos)
                && side == ((PartNetworkElement) o).side;
    }
}
