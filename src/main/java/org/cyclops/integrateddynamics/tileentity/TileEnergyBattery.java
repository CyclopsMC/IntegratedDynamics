package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.integrateddynamics.api.block.IEnergyBattery;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.tileentity.ITileCableNetwork;
import org.cyclops.integrateddynamics.core.block.cable.CableNetworkComponent;

import java.util.Map;

/**
 * A tile entity used to store variables.
 * Internally, this also acts as an expression cache
 * @author rubensworks
 */
public class TileEnergyBattery extends CyclopsTileEntity implements ITileCableNetwork, IEnergyBattery, CyclopsTileEntity.ITickingTile {

    @Delegate
    protected final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    private Map<Integer, Boolean> connected = Maps.newHashMap();

    @Getter
    @Setter
    private IPartNetwork network;

    @NBTPersist
    private int energy;

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        // If the connection data were reset, update the cable connections
        if (connected.isEmpty()) {
            updateConnections();
        }
    }

    @Override
    public void resetCurrentNetwork() {
        if(network != null) setNetwork(null);
    }

    @Override
    public boolean canConnect(ICable connector, EnumFacing side) {
        return true;
    }

    @Override
    public void updateConnections() {
        World world = getWorld();
        for(EnumFacing side : EnumFacing.VALUES) {
            boolean cableConnected = CableNetworkComponent.canSideConnect(world, pos, side, (ICable) getBlock());
            connected.put(side.ordinal(), cableConnected);
        }
        markDirty();
    }

    @Override
    public boolean isConnected(EnumFacing side) {
        return connected.containsKey(side.ordinal()) && connected.get(side.ordinal());
    }

    @Override
    public void disconnect(EnumFacing side) {
        // Do nothing
    }

    @Override
    public void reconnect(EnumFacing side) {
        // Do nothing
    }

    @Override
    public DimPos getPosition() {
        return DimPos.of(getWorld(), getPos());
    }

    @Override
    public int getStoredEnergy() {
        return this.energy;
    }

    @Override
    public int getMaxStoredEnergy() {
        return 1000; // TODO
    }

    @Override
    public void addEnergy(int energy) {
        this.energy += energy;
        this.energy = Math.min(energy, getMaxStoredEnergy());
        sendUpdate();
    }

    @Override
    public int consume(int energy, boolean simulate) {
        int stored = getStoredEnergy();
        int newEnergy = Math.max(stored - energy, 0);
        if(!simulate) {
            this.energy = newEnergy;
            sendUpdate();
        }
        return stored - newEnergy;
    }

}
