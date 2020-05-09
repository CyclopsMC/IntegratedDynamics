package org.cyclops.integrateddynamics.core.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkEventListener;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.capability.valueinterface.ValueInterfaceConfig;
import org.cyclops.integrateddynamics.core.evaluate.InventoryVariableEvaluator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Base part entity that can hold variables.
 * @param <E> The type of event listener
 * @author rubensworks
 */
public abstract class TileActiveVariableBase<E> extends TileCableConnectableInventory implements IDirtyMarkListener, INetworkEventListener<E> {

    private final InventoryVariableEvaluator<IValue> evaluator;

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

        this.evaluator = createEvaluator();
    }

    protected InventoryVariableEvaluator<IValue> createEvaluator() {
        return new InventoryVariableEvaluator<>(this, getSlotRead(), ValueTypes.CATEGORY_ANY);
    }

    public InventoryVariableEvaluator getEvaluator() {
        return evaluator;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        List<L10NHelpers.UnlocalizedString> errors = evaluator.getErrors();
        NBTClassType.writeNbt(List.class, "errors", errors, tag);
        return super.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        evaluator.setErrors(NBTClassType.readNbt(List.class, "errors", tag));
        super.readFromNBT(tag);
    }

    public abstract int getSlotRead();

    public boolean hasVariable() {
        return !getStackInSlot(getSlotRead()).isEmpty();
    }

    protected void updateReadVariable(boolean sendVariablesUpdateEvent) {
        evaluator.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
        sendUpdate();
    }

    @Override
    public void onDirty() {
        if(!world.isRemote) {
            updateReadVariable(true);
        }
    }

    @Nullable
    public IVariable<?> getVariable(IPartNetwork network) {
        return evaluator.getVariable(network);
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
