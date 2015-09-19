package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.InventoryTileEntity;
import org.cyclops.integrateddynamics.core.block.IVariableContainer;
import org.cyclops.integrateddynamics.core.block.cable.CableNetworkComponent;
import org.cyclops.integrateddynamics.core.block.cable.ICable;
import org.cyclops.integrateddynamics.core.item.IVariableFacade;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.tileentity.ITileCableNetwork;
import org.cyclops.integrateddynamics.item.ItemVariable;

import java.util.Collection;
import java.util.Map;

/**
 * A tile entity used to store variables.
 * Internally, this also acts as an expression cache
 * @author rubensworks
 */
public class TileDatastore extends InventoryTileEntity implements ITileCableNetwork, IVariableContainer, IDirtyMarkListener, CyclopsTileEntity.ITickingTile {

    public static final int ROWS = 5;
    public static final int COLS = 9;

    @Delegate
    protected final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    private Map<Integer, Boolean> connected = Maps.newHashMap();

    @Getter
    @Setter
    private Network network;
    private Map<Integer, IVariableFacade> variableCache = Maps.newHashMap();

    public TileDatastore() {
        super(ROWS * COLS, "variables", 1);
        inventory.addDirtyMarkListener(this);

        // Make all sides active for all slots
        Collection<Integer> slots = Lists.newArrayListWithCapacity(getInventory().getSizeInventory());
        for(int i = 0; i < getInventory().getSizeInventory(); i++) {
            slots.add(i);
        }
        for(EnumFacing side : EnumFacing.VALUES) {
            addSlotsToSide(side, slots);
        }
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        // If the connection data were reset, update the cable connections
        if (connected.isEmpty()) {
            updateConnections();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        refreshVariables(inventory);
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

    protected void refreshVariables(IInventory inventory) {
        variableCache.clear();
        for(int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if(itemStack != null) {
                IVariableFacade variableFacade = ItemVariable.getInstance().getVariableFacade(itemStack);
                if(variableFacade.isValid()) {
                    variableCache.put(variableFacade.getId(), variableFacade);
                }
            }
        }
        Network network = getNetwork();
        if(network != null) {
            network.getEventBus().post(new VariableContentsUpdatedEvent(network));
        }
    }

    @Override
    public DimPos getPosition() {
        return DimPos.of(getWorld(), getPos());
    }

    @Override
    public Map<Integer, IVariableFacade> getVariableCache() {
        return variableCache;
    }

    @Override
    public void onDirty() {
        if(!worldObj.isRemote) {
            refreshVariables(inventory);
        }
    }

}
