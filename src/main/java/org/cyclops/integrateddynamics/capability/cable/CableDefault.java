package org.cyclops.integrateddynamics.capability.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;

/**
 * Default implementation of {@link ICable}.
 * @author rubensworks
 */
public abstract class CableDefault implements ICable {

    protected abstract boolean isForceDisconnectable();
    protected abstract EnumFacingMap<Boolean> getForceDisconnected();
    protected abstract EnumFacingMap<Boolean> getConnected();
    protected abstract void setChanged();
    protected abstract void sendUpdate();
    protected abstract Level getLevel();
    protected abstract BlockPos getPos();

    protected boolean isForceDisconnected(Direction side) {
        if (!isForceDisconnectable()) return false;
        if (!getForceDisconnected().containsKey(side)) return false;
        return getForceDisconnected().get(side);
    }

    @Override
    public boolean canConnect(ICable connector, Direction side) {
        return !isForceDisconnected(side);
    }

    @Override
    public void updateConnections() {
        Level world = getLevel();
        for (Direction side : Direction.values()) {
            boolean cableConnected = CableHelpers.canCableConnectTo(world, getPos(), side, this);
            getConnected().put(side, cableConnected);

            // Remove any already existing force-disconnects for this side.
            if (!cableConnected && isForceDisconnectable() && this.canConnect(this, side)) {
                getForceDisconnected().put(side, false);
            }
        }
        setChanged();
        sendUpdate();
    }

    @Override
    public boolean isConnected(Direction side) {
        if(getPos() == null) {
            return false;
        }
        if(getConnected().isEmpty()) {
            updateConnections();
        }
        return getConnected().containsKey(side) && getConnected().get(side);
    }

    @Override
    public void disconnect(Direction side) {
        if (isForceDisconnectable()) {
            getForceDisconnected().put(side, true);
        }
    }

    @Override
    public void reconnect(Direction side) {
        if (isForceDisconnectable()) {
            getForceDisconnected().remove(side);
        }
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(RegistryEntries.BLOCK_CABLE.get());
    }

}
