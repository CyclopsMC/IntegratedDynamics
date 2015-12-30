package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IProxyVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.network.INetworkEventListener;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.core.evaluate.ProxyVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.item.ProxyVariableFacade;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectableInventory;
import org.cyclops.integrateddynamics.network.ProxyNetworkElement;

import java.util.List;
import java.util.Set;

/**
 * A tile entity for the variable proxy.
 * @author rubensworks
 */
public class TileProxy extends TileCableConnectableInventory implements IDirtyMarkListener, IVariableFacade.IValidator, INetworkEventListener<IPartNetwork, ProxyNetworkElement> {

    public static final int SLOT_READ = 0;
    public static final int SLOT_WRITE_IN = 1;
    public static final int SLOT_WRITE_OUT = 2;

    public static final String GLOBALCOUNTER_KEY = "proxy";

    protected IVariableFacade variableStored = null;
    @NBTPersist
    @Getter
    private int proxyId = -1;
    @NBTPersist
    @Getter
    private List<L10NHelpers.UnlocalizedString> errors = Lists.newLinkedList();

    public TileProxy() {
        super(3, "proxy", 1);
        inventory.addDirtyMarkListener(this);

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

    @Override
    public void onLoad() {
        super.onLoad();
        if(!MinecraftHelpers.isClientSide() && this.proxyId == -1) {
            this.proxyId = IntegratedDynamics.globalCounters.getNext(GLOBALCOUNTER_KEY);
        }
    }

    @Override
    public void setNetwork(IPartNetwork network) {
        super.setNetwork(network);
        updateReadVariable();
    }

    public boolean hasVariable() {
        return getStackInSlot(SLOT_READ) != null;
    }

    protected void updateReadVariable() {
        IPartNetwork network = getNetwork();

        int lastVariabledId = this.variableStored == null ? -1 : this.variableStored.getId();
        int variableId = -1;
        if (getStackInSlot(SLOT_READ) != null) {
            // Update proxy input
            ItemStack itemStack = getStackInSlot(SLOT_READ);
            IVariableFacadeHandlerRegistry handler = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
            this.variableStored = handler.handle(itemStack);
            if(this.variableStored != null) {
                variableId = this.variableStored.getId();
            }
        } else {
            this.variableStored = null;
        }

        this.errors.clear();
        if (network == null) {
            addError(new L10NHelpers.UnlocalizedString(L10NValues.GENERAL_ERROR_NONETWORK));
        } else if (this.variableStored != null) {
            variableStored.validate(network, this, ValueTypes.CATEGORY_ANY);
        }
        if(network != null && lastVariabledId != variableId) {
            network.getEventBus().post(new VariableContentsUpdatedEvent(network));
        }
        sendUpdate();
    }

    @Override
    public void onDirty() {
        if(!worldObj.isRemote) {
            updateReadVariable();

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

    public IVariable<?> getVariable(IPartNetwork network) {
        if(variableStored == null) return null;
        return variableStored.getVariable(network);
    }

    @Override
    public void addError(L10NHelpers.UnlocalizedString error) {
        errors.add(error);
    }

    @Override
    public boolean hasEventSubscriptions() {
        return true;
    }

    @Override
    public Set<Class<? extends INetworkEvent<IPartNetwork>>> getSubscribedEvents() {
        return Sets.<Class<? extends INetworkEvent<IPartNetwork>>>newHashSet(VariableContentsUpdatedEvent.class);
    }

    @Override
    public void onEvent(INetworkEvent<IPartNetwork> event, ProxyNetworkElement networkElement) {
        if(event instanceof VariableContentsUpdatedEvent) {
            updateReadVariable();
        }
    }
}
