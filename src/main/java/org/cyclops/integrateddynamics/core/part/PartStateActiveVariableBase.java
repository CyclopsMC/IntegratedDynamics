package org.cyclops.integrateddynamics.core.part;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;
import org.cyclops.integrateddynamics.api.evaluate.IValueInterface;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartCapability;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerDefault;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

import java.util.List;
import java.util.Optional;

/**
 * An abstract part state with a focus on activatable variables.
 * @author rubensworks
 */
public abstract class PartStateActiveVariableBase<P extends IPartType> extends PartStateBase<P> {

    private boolean checkedForWriteVariable = false;
    protected IVariableFacade currentVariableFacade = null;
    private final IVariableContainer variableContainer;
    @Getter
    @Setter
    private boolean deactivated = false;
    private SimpleInventory inventory;
    private List<MutableComponent> globalErrorMessages = Lists.newLinkedList();
    @Getter
    @Setter
    private boolean retryEvaluation = false;

    public PartStateActiveVariableBase(int inventorySize) {
        this.inventory = new SingularInventory(inventorySize);
        this.inventory.addDirtyMarkListener(this); // No need to remove myself eventually. If I am removed, inv is also removed.
        variableContainer = new VariableContainerDefault();
        addVolatileCapability(Capabilities.VariableContainer.PART, Optional.of(variableContainer));
    }

    /**
     * @return The inner inventory
     */
    public SimpleInventory getInventory() {
        return this.inventory;
    }

    protected void validate(INetwork network, IPartNetwork partNetwork) {
        // Note that this is only called server-side, so these errors are sent via NBT to the client(s).
        this.currentVariableFacade.validate(network, partNetwork,
                new PartStateActiveVariableBase.Validator(this), currentVariableFacade.getOutputType());
    }

    protected void onCorruptedState() {
        IntegratedDynamics.clog(org.apache.logging.log4j.Level.ERROR, "A corrupted part state was found at, repairing...");
        Thread.dumpStack();
        this.checkedForWriteVariable = false;
        this.deactivated = true;
    }

    /**
     * @return If there is an active variable present for this state.
     */
    public boolean hasVariable() {
        return (getGlobalErrors().isEmpty() || isRetryEvaluation()) && !getInventory().isEmpty();
    }

    /**
     * Get the active variable in this state.
     * @param <V> The variable value type.
     * @param network The network.
     * @param partNetwork The part network.
     * @param valueDeseralizationContext
     * @return The variable.
     */
    public <V extends IValue> IVariable<V> getVariable(INetwork network, IPartNetwork partNetwork, ValueDeseralizationContext valueDeseralizationContext) {
        if(!checkedForWriteVariable) {
            if (variableContainer.getVariableCache().isEmpty()) {
                variableContainer.refreshVariables(network, inventory, false, valueDeseralizationContext);
            }
            for (IVariableFacade facade : variableContainer.getVariableCache().values()) {
                if (facade != null) {
                    currentVariableFacade = facade;
                    validate(network, partNetwork);
                }
            }
            this.checkedForWriteVariable = true;
        }
        if(currentVariableFacade == null) {
            onCorruptedState();
            return null;
        }
        return currentVariableFacade.getVariable(network, partNetwork);
    }

    /**
     * Refresh the current variable to have its current info reset and updated.
     * @param partType The corresponding part type.
     * @param target The target of the part.
     */
    public void onVariableContentsUpdated(P partType, PartTarget target) {
        // Resets the errors for this aspect
        this.checkedForWriteVariable = false;
        addGlobalError(null);
        this.currentVariableFacade = null;
        //this.deactivated = false; // This *should* not be required anymore, re-activation is handled in AspectWriteBase#update.

        // Refresh any contained variables
        PartPos center = target.getCenter();
        Level level = center.getPos().getLevel(true);
        NetworkHelpers.getNetwork(level, center.getPos().getBlockPos(), center.getSide())
                .ifPresent(network -> variableContainer.refreshVariables(network, inventory, false, ValueDeseralizationContext.of(level)));
    }

    /**
     * @return All global error messages.
     */
    public List<MutableComponent> getGlobalErrors() {
        return globalErrorMessages;
    }

    /**
     * Add a global error message.
     * @param error The message to add.
     */
    public void addGlobalError(MutableComponent error) {
        setRetryEvaluation(false);
        if(error == null) {
            globalErrorMessages.clear();
        } else {
            globalErrorMessages.add(error);
        }
        onDirty();
        sendUpdate(); // We want this error messages to be sent to the client(s).
    }

    @Override
    public void writeToNBT(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag) {
        super.writeToNBT(valueDeseralizationContext, tag);
        NBTClassType.writeNbt(List.class, "globalErrorMessages", globalErrorMessages, tag, valueDeseralizationContext.holderLookupProvider());
        inventory.writeToNBT(valueDeseralizationContext.holderLookupProvider(), tag, "inventory");
    }

    @Override
    public void readFromNBT(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag) {
        super.readFromNBT(valueDeseralizationContext, tag);
        //noinspection unchecked
        this.globalErrorMessages = NBTClassType.readNbt(List.class, "globalErrorMessages", tag, valueDeseralizationContext.holderLookupProvider());
        inventory.readFromNBT(valueDeseralizationContext.holderLookupProvider(), tag, "inventory");
    }

    @Override
    public <T> Optional<T> getCapability(P partType, PartCapability<T> capability, INetwork network, IPartNetwork partNetwork, PartTarget target) {
        if (capability == Capabilities.ValueInterface.PART) {
            if (hasVariable()) {
                IVariable<IValue> variable = getVariable(network, partNetwork, ValueDeseralizationContext.of(target.getCenter().getPos().getLevel(true)));
                if (variable != null) {
                    return (Optional<T>) Optional.<IValueInterface>of(() -> Optional.of(variable.getValue()));
                }
            }
            return Optional.empty();
        }
        return super.getCapability(partType, capability, network, partNetwork, target);
    }

    /**
     * An inventory that can only hold one filled slot at a time.
     */
    public static class SingularInventory extends SimpleInventory {

        /**
         * Make a new instance.
         *
         * @param size The amount of slots in the inventory.
         */
        public SingularInventory(int size) {
            super(size, 1);
        }

        protected boolean canInsert(int slot) {
            for (int i = 0; i < getContainerSize(); i++) {
                // Only allow insertion if the target slot is the same as the non-empty slot
                if (i != slot && !getItem(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean canPlaceItem(int i, ItemStack itemstack) {
            return canInsert(i) && super.canPlaceItem(i, itemstack);
        }

    }

    public static class Validator implements IVariableFacade.IValidator {

        private final PartStateActiveVariableBase state;

        /**
         * Make a new instance
         * @param state The part state.
         */
        public Validator(PartStateActiveVariableBase state) {
            this.state = state;
        }

        @Override
        public void addError(MutableComponent error) {
            this.state.addGlobalError(error);
        }

    }

}
