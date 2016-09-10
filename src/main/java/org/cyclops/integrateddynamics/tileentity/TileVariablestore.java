package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.capability.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectableInventory;
import org.cyclops.integrateddynamics.item.ItemVariable;
import org.cyclops.integrateddynamics.network.VariablestoreNetworkElement;

import java.util.Collection;
import java.util.Map;

/**
 * A tile entity used to store variables.
 * Internally, this also acts as an expression cache
 * @author rubensworks
 */
public class TileVariablestore extends TileCableConnectableInventory implements IVariableContainer, IDirtyMarkListener {

    public static final int ROWS = 5;
    public static final int COLS = 9;
    private Map<Integer, IVariableFacade> variableCache = Maps.newHashMap();

    public TileVariablestore() {
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

        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, new NetworkElementProviderSingleton<IPartNetwork>() {
            @Override
            public INetworkElement<IPartNetwork> createNetworkElement(World world, BlockPos blockPos) {
                return new VariablestoreNetworkElement(DimPos.of(world, blockPos));
            }
        });
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        refreshVariables(inventory);
    }

    protected void refreshVariables(IInventory inventory) {
        variableCache.clear();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (itemStack != null) {
                IVariableFacade variableFacade = ItemVariable.getInstance().getVariableFacade(itemStack);
                if (variableFacade.isValid()) {
                    variableCache.put(variableFacade.getId(), variableFacade);
                }
            }
        }

        IPartNetwork network = getNetwork();
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
