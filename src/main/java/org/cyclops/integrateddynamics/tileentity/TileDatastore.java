package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.integrateddynamics.core.block.cable.CableNetworkComponent;
import org.cyclops.integrateddynamics.core.block.cable.ICable;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.tileentity.ITileCableNetwork;

import java.util.Map;

/**
 * A tile entity used to store variables.
 * @author rubensworks
 */
public class TileDatastore extends CyclopsTileEntity implements ITileCableNetwork {

    @NBTPersist
    private Map<Integer, Boolean> connected = Maps.newHashMap();

    @Getter
    @Setter
    private Network network;
    @Getter
    private SimpleInventory inventory = new SimpleInventory(9 * 5, "variables", 1);

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        inventory.writeToNBT(tag, "inventory");
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        inventory.readFromNBT(tag, "inventory");
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
        return connected.containsKey(side) && connected.get(side);
    }

    @Override
    public void disconnect(EnumFacing side) {
        // Do nothing
    }
}
