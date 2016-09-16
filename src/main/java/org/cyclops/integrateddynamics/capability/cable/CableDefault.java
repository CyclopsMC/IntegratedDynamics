package org.cyclops.integrateddynamics.capability.cable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;

/**
 * Default implementation of {@link ICable}.
 * @author rubensworks
 */
public abstract class CableDefault implements ICable {

    protected abstract boolean isForceDisconnectable();
    protected abstract EnumFacingMap<Boolean> getForceDisconnected();
    protected abstract EnumFacingMap<Boolean> getConnected();
    protected abstract void markDirty();
    protected abstract void sendUpdate();
    protected abstract World getWorld();
    protected abstract BlockPos getPos();

    protected boolean isForceDisconnected(EnumFacing side) {
        if (!isForceDisconnectable()) return false;
        if (!getForceDisconnected().containsKey(side)) return false;
        return getForceDisconnected().get(side);
    }

    @Override
    public boolean canConnect(ICable connector, EnumFacing side) {
        return !isForceDisconnected(side);
    }

    @Override
    public void updateConnections() {
        World world = getWorld();
        for (EnumFacing side : EnumFacing.VALUES) {
            boolean cableConnected = CableHelpers.canCableConnectTo(world, getPos(), side, this);
            getConnected().put(side, cableConnected);

            // Remove any already existing force-disconnects for this side.
            if (!cableConnected && isForceDisconnectable() && this.canConnect(this, side)) {
                getForceDisconnected().put(side, false);
            }
        }
        markDirty();
        sendUpdate();
    }

    @Override
    public boolean isConnected(EnumFacing side) {
        if(getPos() == null) {
            return false;
        }
        if(getConnected().isEmpty()) {
            updateConnections();
        }
        return getConnected().containsKey(side) && getConnected().get(side);
    }

    @Override
    public void disconnect(EnumFacing side) {
        if (isForceDisconnectable()) {
            getForceDisconnected().put(side, true);
        }
    }

    @Override
    public void reconnect(EnumFacing side) {
        if (isForceDisconnectable()) {
            getForceDisconnected().remove(side);
        }
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(BlockCable.getInstance());
    }

}
