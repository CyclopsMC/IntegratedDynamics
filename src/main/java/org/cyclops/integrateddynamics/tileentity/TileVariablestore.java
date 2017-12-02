package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Lists;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerConfig;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerDefault;
import org.cyclops.integrateddynamics.capability.variablefacade.VariableFacadeHolderConfig;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectableInventory;
import org.cyclops.integrateddynamics.item.ItemVariable;
import org.cyclops.integrateddynamics.network.VariablestoreNetworkElement;

import java.util.Collection;

/**
 * A part entity used to store variables.
 * Internally, this also acts as an expression cache
 * @author rubensworks
 */
public class TileVariablestore extends TileCableConnectableInventory implements IDirtyMarkListener {

    public static final int ROWS = 5;
    public static final int COLS = 9;

    private final IVariableContainer variableContainer;

    private boolean shouldSendUpdateEvent = false;

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

        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new VariablestoreNetworkElement(DimPos.of(world, blockPos));
            }
        });
        variableContainer = new VariableContainerDefault();
        addCapabilityInternal(VariableContainerConfig.CAPABILITY, variableContainer);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return super.isItemValidForSlot(index, stack)
                && (stack.isEmpty() || stack.hasCapability(VariableFacadeHolderConfig.CAPABILITY, null));
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        refreshVariables(inventory);
    }

    protected void refreshVariables(IInventory inventory) {
        variableContainer.getVariableCache().clear();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                IVariableFacade variableFacade = ItemVariable.getInstance().getVariableFacade(itemStack);
                if (variableFacade != null && variableFacade.isValid()) {
                    variableContainer.getVariableCache().put(variableFacade.getId(), variableFacade);
                }
            }
        }

        INetwork network = getNetwork();
        if(network != null) {
            network.getEventBus().post(new VariableContentsUpdatedEvent(network));
        }
    }

    @Override
    public void onDirty() {
        if(!world.isRemote) {
            refreshVariables(inventory);
        }
    }

    // Make sure that when this TE is loaded, and after the network has been set,
    // that we trigger a variable update event in the network.

    @Override
    public void onLoad() {
        super.onLoad();
        if(!MinecraftHelpers.isClientSide()) {
            shouldSendUpdateEvent = true;
        }
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (shouldSendUpdateEvent && getNetwork() != null) {
            shouldSendUpdateEvent = false;
            refreshVariables(inventory);
        }
    }
}
