package org.cyclops.integrateddynamics.core.tileentity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.network.INetworkEventListener;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;

import java.util.List;
import java.util.Set;

/**
 * Base tile entity that can hold variables.
 * @param <E> The type of event listener
 * @author rubensworks
 */
public abstract class TileActiveVariableBase<E> extends TileCableConnectableInventory implements IDirtyMarkListener, IVariableFacade.IValidator, INetworkEventListener<IPartNetwork, E> {

    protected IVariableFacade variableStored = null;
    @NBTPersist
    @Getter
    private List<L10NHelpers.UnlocalizedString> errors = Lists.newLinkedList();

    public TileActiveVariableBase(int inventorySize, String inventoryName) {
        super(inventorySize, inventoryName, 1);
        inventory.addDirtyMarkListener(this);
    }

    public abstract int getSlotRead();

    public boolean hasVariable() {
        return getStackInSlot(getSlotRead()) != null;
    }

    protected void updateReadVariable() {
        IPartNetwork network = getNetwork();

        int lastVariabledId = this.variableStored == null ? -1 : this.variableStored.getId();
        int variableId = -1;
        if (getStackInSlot(getSlotRead()) != null) {
            // Update proxy input
            ItemStack itemStack = getStackInSlot(getSlotRead());
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
            preValidate(variableStored);
            try {
                variableStored.validate(network, this, ValueTypes.CATEGORY_ANY);
            } catch (IllegalArgumentException e) {
                addError(new L10NHelpers.UnlocalizedString(e.getMessage()));
            }
        }
        if(network != null && lastVariabledId != variableId) {
            network.getEventBus().post(new VariableContentsUpdatedEvent(network));
        }
        sendUpdate();
    }

    protected void preValidate(IVariableFacade variableStored) {

    }

    @Override
    public void onDirty() {
        if(!worldObj.isRemote) {
            updateReadVariable();
        }
    }

    public IVariable<?> getVariable(IPartNetwork network) {
        if(variableStored == null || !getErrors().isEmpty()) return null;
        try {
            return variableStored.getVariable(network);
        } catch (IllegalArgumentException e) {
            addError(new L10NHelpers.UnlocalizedString(e.getMessage()));
            return null;
        }
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
    public void onEvent(INetworkEvent<IPartNetwork> event, E networkElement) {
        if(event instanceof VariableContentsUpdatedEvent) {
            updateReadVariable();
        }
    }

    @Override
    public void afterNetworkReAlive() {
        super.afterNetworkReAlive();
        updateReadVariable();
    }
}
