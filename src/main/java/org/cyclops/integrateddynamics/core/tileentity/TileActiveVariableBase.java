package org.cyclops.integrateddynamics.core.tileentity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkEventListener;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.capability.valueinterface.ValueInterfaceConfig;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Base part entity that can hold variables.
 * @param <E> The type of event listener
 * @author rubensworks
 */
public abstract class TileActiveVariableBase<E> extends TileCableConnectableInventory implements IDirtyMarkListener, IVariableFacade.IValidator, INetworkEventListener<E> {

    protected IVariableFacade variableStored = null;
    @NBTPersist
    @Getter
    private List<L10NHelpers.UnlocalizedString> errors = Lists.newLinkedList();

    public TileActiveVariableBase(int inventorySize, String inventoryName) {
        super(inventorySize, inventoryName, 1);
        inventory.addDirtyMarkListener(this);
        addCapabilityInternal(ValueInterfaceConfig.CAPABILITY, () -> {
            INetwork network = getNetwork();
            IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network);
            if (network == null || partNetwork == null) {
                throw new EvaluationException("No valid network was found");
            }
            if (hasVariable()) {
                IVariable<?> variable = getVariable(partNetwork);
                if (variable != null) {
                    return Optional.of(variable.getValue());
                }
            }
            return Optional.empty();
        });
    }

    public abstract int getSlotRead();

    public boolean hasVariable() {
        return !getStackInSlot(getSlotRead()).isEmpty();
    }

    protected void updateReadVariable(boolean sendVariablesUpdateEvent) {
        INetwork network = getNetwork();
        IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network);

        int lastVariabledId = this.variableStored == null ? -1 : this.variableStored.getId();
        int variableId = -1;
        if (!getStackInSlot(getSlotRead()).isEmpty() && NetworkHelpers.shouldWork()) {
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
        if (partNetwork == null) {
            addError(new L10NHelpers.UnlocalizedString(L10NValues.GENERAL_ERROR_NONETWORK), false);
        } else if (this.variableStored != null) {
            preValidate(variableStored);
            try {
                variableStored.validate(partNetwork, this, ValueTypes.CATEGORY_ANY);
            } catch (IllegalArgumentException e) {
                addError(new L10NHelpers.UnlocalizedString(e.getMessage()), false);
            }
        }
        if(sendVariablesUpdateEvent && partNetwork != null && lastVariabledId != variableId) {
            network.getEventBus().post(new VariableContentsUpdatedEvent(network));
        }
        sendUpdate();
    }

    protected void preValidate(IVariableFacade variableStored) {

    }

    @Override
    public void onDirty() {
        if(!world.isRemote) {
            updateReadVariable(true);
        }
    }

    public IVariable<?> getVariable(IPartNetwork network) {
        if(variableStored == null || !getErrors().isEmpty()) return null;
        try {
            return variableStored.getVariable(network);
        } catch (IllegalArgumentException e) {
            addError(new L10NHelpers.UnlocalizedString(e.getMessage()), false);
            return null;
        }
    }

    @Override
    public void addError(L10NHelpers.UnlocalizedString error, boolean transientError) {
        errors.add(error);
    }

    @Override
    public boolean hasEventSubscriptions() {
        return true;
    }

    @Override
    public Set<Class<? extends INetworkEvent>> getSubscribedEvents() {
        return Sets.<Class<? extends INetworkEvent>>newHashSet(VariableContentsUpdatedEvent.class);
    }

    @Override
    public void onEvent(INetworkEvent event, E networkElement) {
        if(event instanceof VariableContentsUpdatedEvent) {
            updateReadVariable(false);
        }
    }

    @Override
    public void afterNetworkReAlive() {
        super.afterNetworkReAlive();
        updateReadVariable(true);
    }
}
