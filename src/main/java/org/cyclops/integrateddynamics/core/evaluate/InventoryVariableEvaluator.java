package org.cyclops.integrateddynamics.core.evaluate;

import com.google.common.collect.Lists;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A convenience holder class for getting variables from variable cards in a certain inventory slot.
 * @param <V> The variable value type
 * @author rubensworks
 */
public class InventoryVariableEvaluator<V extends IValue> implements IVariableFacade.IValidator {

    private final IVariableFacadeHandlerRegistry handler = IntegratedDynamics._instance.getRegistryManager()
            .getRegistry(IVariableFacadeHandlerRegistry.class);
    private final IInventory inventory;
    private final int slot;
    private final IValueType containingValueType;

    private IVariableFacade variableStored = null;
    private List<ITextComponent> errors = Lists.newLinkedList();

    public InventoryVariableEvaluator(IInventory inventory, int slot, IValueType<V> containingValueType) {
        this.inventory = inventory;
        this.slot = slot;
        this.containingValueType = containingValueType;
    }

    /**
     * @return If the configured slot has an item.
     */
    public boolean hasVariable() {
        return !inventory.getStackInSlot(slot).isEmpty();
    }

    /**
     * Refresh the variable reference by checking the inventory,
     * and validating the containing variable.
     * @param network The network.
     * @param sendVariablesUpdateEvent If a {@link VariableContentsUpdatedEvent} event must be sent
     *                                 if the variable has changed.
     */
    public void refreshVariable(@Nullable INetwork network, boolean sendVariablesUpdateEvent) {
        IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network).orElse(null);

        int lastVariabledId = this.variableStored == null ? -1 : this.variableStored.getId();
        int variableId = -1;
        if (!inventory.getStackInSlot(slot).isEmpty() && NetworkHelpers.shouldWork()) {
            // Update proxy input
            ItemStack itemStack = inventory.getStackInSlot(slot);
            this.variableStored = handler.handle(itemStack);
            if(this.variableStored != null) {
                variableId = this.variableStored.getId();
            }
        } else {
            this.variableStored = null;
        }

        clearErrors();
        if (partNetwork == null) {
            addError(new TranslationTextComponent(L10NValues.GENERAL_ERROR_NONETWORK));
        } else if (this.variableStored != null) {
            preValidate();
            try {
                variableStored.validate(partNetwork, this, containingValueType);
            } catch (IllegalArgumentException e) {
                addError(new TranslationTextComponent(e.getMessage()));
            }
        }
        if(sendVariablesUpdateEvent && partNetwork != null && lastVariabledId != variableId) {
            network.getEventBus().post(new VariableContentsUpdatedEvent(network));
        }
    }

    @Nullable
    public IVariable<V> getVariable(INetwork network) {
        return getVariable(NetworkHelpers.getPartNetworkChecked(network));

    }

    @Nullable
    public IVariable<V> getVariable(IPartNetwork network) {
        if(getVariableFacade() == null || !getErrors().isEmpty()) return null;
        try {
            return getVariableFacade().getVariable(network);
        } catch (IllegalArgumentException e) {
            addError(new TranslationTextComponent(e.getMessage()));
            return null;
        }
    }

    public IVariableFacade getVariableFacade() {
        return variableStored;
    }

    protected void preValidate() {

    }

    public void clearErrors() {
        this.errors.clear();
        onErrorsChanged();
    }

    public void setErrors(List<ITextComponent> errors) {
        this.errors = errors;
        onErrorsChanged();
    }

    public List<ITextComponent> getErrors() {
        return errors;
    }

    @Override
    public void addError(IFormattableTextComponent error) {
        errors.add(error);
    }

    public void onErrorsChanged() {

    }
}
