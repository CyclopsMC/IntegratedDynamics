package org.cyclops.integrateddynamics.core.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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
public abstract class BlockEntityActiveVariableBase<E> extends BlockEntityCableConnectableInventory implements IDirtyMarkListener, INetworkEventListener<E> {

    private final InventoryVariableEvaluator<IValue> evaluator;

    public BlockEntityActiveVariableBase(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState, int inventorySize) {
        super(type, blockPos, blockState, inventorySize, 1);
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
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        List<MutableComponent> errors = evaluator.getErrors();
        NBTClassType.writeNbt(List.class, "errors", errors, tag);
    }

    @Override
    public void read(CompoundTag tag) {
        evaluator.setErrors(NBTClassType.readNbt(List.class, "errors", tag));
        super.read(tag);
    }

    public abstract int getSlotRead();

    public boolean hasVariable() {
        return !getInventory().getItem(getSlotRead()).isEmpty();
    }

    protected void updateReadVariable(boolean sendVariablesUpdateEvent) {
        evaluator.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
        sendUpdate();
    }

    @Override
    public void onDirty() {
        if(!level.isClientSide()) {
            updateReadVariable(true);
        }
    }

    @Nullable
    public IVariable<?> getVariable(IPartNetwork network) { // TODO: in next major version, add network as first param to avoid calling getNetwork() in the impl.
        return evaluator.getVariable(getNetwork(), network);
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
