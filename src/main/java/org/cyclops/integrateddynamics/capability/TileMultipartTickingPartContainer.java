package org.cyclops.integrateddynamics.capability;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartContainerFacade;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

/**
 * Implementation of an {@link IPartContainer} for a tile entity.
 * @author rubensworks
 */
public class TileMultipartTickingPartContainer extends DefaultPartContainer {

    private final TileMultipartTicking tile;

    public TileMultipartTickingPartContainer(TileMultipartTicking tile) {
        this.tile = tile;
    }

    protected TileMultipartTicking getTile() {
        return tile;
    }

    @Override
    protected void markDirty() {
        getTile().markDirty();
    }

    @Override
    protected void sendUpdate() {
        getTile().sendUpdate();
    }

    @Override
    protected World getWorld() {
        return getTile().getWorld();
    }

    @Override
    protected BlockPos getPos() {
        return getTile().getPos();
    }

    @Override
    protected IPartNetwork getNetwork() {
        return getTile().getNetwork();
    }

    @Override
    protected IPartContainerFacade getPartContainerFacade() {
        return (IPartContainerFacade) getTile().getBlock();
    }
}
