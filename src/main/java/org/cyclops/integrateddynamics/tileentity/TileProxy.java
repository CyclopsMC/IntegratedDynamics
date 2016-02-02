package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.item.IProxyVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.network.INetworkEventListener;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.evaluate.ProxyVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.item.ProxyVariableFacade;
import org.cyclops.integrateddynamics.core.tileentity.TileActiveVariableBase;
import org.cyclops.integrateddynamics.network.ProxyNetworkElement;

/**
 * A tile entity for the variable proxy.
 * @author rubensworks
 */
public class TileProxy extends TileActiveVariableBase<ProxyNetworkElement> implements IDirtyMarkListener, IVariableFacade.IValidator, INetworkEventListener<IPartNetwork, ProxyNetworkElement> {

    public static final int SLOT_READ = 0;
    public static final int SLOT_WRITE_IN = 1;
    public static final int SLOT_WRITE_OUT = 2;

    public static final String GLOBALCOUNTER_KEY = "proxy";

    @NBTPersist
    @Getter
    private int proxyId = -1;

    public TileProxy() {
        super(3, "proxy");

        addSlotsToSide(EnumFacing.UP, Sets.newHashSet(SLOT_READ));
        addSlotsToSide(EnumFacing.DOWN, Sets.newHashSet(SLOT_READ));
        addSlotsToSide(EnumFacing.SOUTH, Sets.newHashSet(SLOT_READ));
        addSlotsToSide(EnumFacing.WEST, Sets.newHashSet(SLOT_WRITE_OUT));
        addSlotsToSide(EnumFacing.EAST, Sets.newHashSet(SLOT_WRITE_IN));
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack, EnumFacing side) {
        return slot != SLOT_WRITE_OUT && super.canInsertItem(slot, itemStack, side);
    }

    /**
     * This will generate a new proxy id.
     * Be careful when calling this!
     */
    public void generateNewProxyId() {
        this.proxyId = IntegratedDynamics.globalCounters.getNext(GLOBALCOUNTER_KEY);
        markDirty();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(!MinecraftHelpers.isClientSide() && this.proxyId == -1) {
            generateNewProxyId();
        }
    }

    @Override
    public int getSlotRead() {
        return SLOT_READ;
    }

    @Override
    public void onDirty() {
        super.onDirty();
        if(!worldObj.isRemote) {
            if (getStackInSlot(SLOT_WRITE_IN) != null && getStackInSlot(SLOT_WRITE_OUT) == null) {
                // Write proxy reference
                ItemStack outputStack = writeProxyInfo(!getWorld().isRemote, removeStackFromSlot(SLOT_WRITE_IN), proxyId);
                setInventorySlotContents(SLOT_WRITE_OUT, outputStack);
            }
        }
    }

    public ItemStack writeProxyInfo(boolean generateId, ItemStack itemStack, final int proxyId) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        return registry.writeVariableFacadeItem(generateId, itemStack, ProxyVariableFacadeHandler.getInstance(), new IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IProxyVariableFacade>() {
            @Override
            public IProxyVariableFacade create(boolean generateId) {
                return new ProxyVariableFacade(generateId, proxyId);
            }

            @Override
            public IProxyVariableFacade create(int id) {
                return new ProxyVariableFacade(id, proxyId);
            }
        });
    }

    @Override
    protected void preValidate(IVariableFacade variableStored) {
        super.preValidate(variableStored);
        // Hard check to make sure the variable is not directly referring to this proxy.
        if(variableStored instanceof IProxyVariableFacade) {
            if(((IProxyVariableFacade) variableStored).getProxyId() == getProxyId()) {
                addError(new L10NHelpers.UnlocalizedString(L10NValues.VARIABLE_ERROR_RECURSION, variableStored.getId()));
            }
        }
    }
}
