package org.cyclops.integrateddynamics.core.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
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

    public TileActiveVariableBase(TileEntityType<?> type, int inventorySize) {
        super(type, inventorySize, 1);
        getInventory().addDirtyMarkListener(this);
        addCapabilityInternal(ValueInterfaceConfig.CAPABILITY, LazyOptional.of(() -> () -> {
            INetwork network = getNetwork();
            IPartNetwork partNetwork = NetworkHelpers.getPartNetworkChecked(network);
            if (hasVariable()) {
                IVariable<?> variable = getVariable(partNetwork);
                if (variable != null) {
                    return Optional.of(variable.getValue());
                }
            }
            return Optional.empty();
        }));

        this.evaluator = createEvaluator();
    }

    protected InventoryVariableEvaluator<IValue> createEvaluator() {
        return new InventoryVariableEvaluator<>(this.getInventory(), getSlotRead(), ValueTypes.CATEGORY_ANY);
    }

    public InventoryVariableEvaluator getEvaluator() {
        return evaluator;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        List<ITextComponent> errors = evaluator.getErrors();
        NBTClassType.writeNbt(List.class, "errors", errors, tag);
        return super.write(tag);
    }

    @Override
    public void read(CompoundNBT tag) {
        evaluator.setErrors(NBTClassType.readNbt(List.class, "errors", tag));
        super.read(tag);
    }

    public abstract int getSlotRead();

    public boolean hasVariable() {
        return !getInventory().getStackInSlot(getSlotRead()).isEmpty();
    }

    protected void updateReadVariable(boolean sendVariablesUpdateEvent) {
        evaluator.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
        sendUpdate();
    }

    @Override
    public void onDirty() {
        if(!world.isRemote()) {
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
