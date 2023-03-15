package org.cyclops.integrateddynamics.core.part;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.MinecraftForge;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.evaluate.InventoryVariableEvaluator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.part.event.PartVariableDrivenVariableContentsUpdatedEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Handles dynamic offsets inside part states.
 * @author rubensworks
 */
public class PartStateOffsetHandler<P extends IPartType> {

    public final List<InventoryVariableEvaluator<ValueTypeInteger.ValueInteger>> offsetVariableEvaluators = Lists.newArrayList();
    public final Int2ObjectMap<MutableComponent> offsetVariablesSlotMessages = new Int2ObjectArrayMap<>();
    public boolean offsetVariablesDirty = true;
    public final IntSet offsetVariableSlotDirty = new IntArraySet();
    public final Map<IVariable, Boolean> offsetVariableListeners = new MapMaker().weakKeys().makeMap();

    public void updateOffsetVariables(P partType, IPartState<P> partState, INetwork network, IPartNetwork partNetwork, PartTarget target) {
        // Reload offset variables if needed
        if (offsetVariablesDirty) {
            offsetVariablesDirty = false;
            reloadOffsetVariables(partType, partState, network, partNetwork, target);
        }

        // Only update single slots if needed
        if (!offsetVariableSlotDirty.isEmpty()) {
            IntArraySet offsetVariableSlotDirtyCopy = new IntArraySet(offsetVariableSlotDirty);
            offsetVariableSlotDirty.clear();
            for (Integer slot : offsetVariableSlotDirtyCopy) {
                this.reloadOffsetVariable(partType, partState, network, partNetwork, target, slot);
            }
        }
    }

    public void markOffsetVariablesChanged() {
        this.offsetVariablesDirty = true;
    }

    public SimpleInventory getOffsetVariablesInventory(IPartState<P> partState) {
        SimpleInventory offsetVariablesInventory = new SimpleInventory(3, 1);
        partState.loadInventoryNamed("offsetVariablesInventory", offsetVariablesInventory);
        return offsetVariablesInventory;
    }

    public void reloadOffsetVariables(P partType, IPartState<P> partState, INetwork network, IPartNetwork partNetwork, PartTarget target) {
        offsetVariableEvaluators.clear();
        offsetVariableSlotDirty.clear();
        SimpleInventory offsetVariablesInventory = getOffsetVariablesInventory(partState);
        for (int i = 0; i < 3; i++) {
            int slot = i;
            offsetVariableEvaluators.add(new InventoryVariableEvaluator<>(
                    offsetVariablesInventory, slot, ValueDeseralizationContext.of(target.getCenter().getPos().getLevel(true)), ValueTypes.INTEGER) {
                @Override
                public void onErrorsChanged() {
                    super.onErrorsChanged();
                    setOffsetVariableErrors(slot, getErrors());
                }
            });
        }
        for (int i = 0; i < offsetVariablesInventory.getContainerSize(); i++) {
            reloadOffsetVariable(partType, partState, network, partNetwork, target, i);
        }
    }

    private void setOffsetVariableErrors(int slot, List<MutableComponent> errors) {
        if (errors.isEmpty()) {
            if (this.offsetVariablesSlotMessages.size() > slot) {
                this.offsetVariablesSlotMessages.remove(slot);
            }
        } else {
            this.offsetVariablesSlotMessages.put(slot, errors.get(0));
        }
    }

    @Nullable
    public MutableComponent getOffsetVariableError(int slot) {
        return this.offsetVariablesSlotMessages.get(slot);
    }

    protected void reloadOffsetVariable(P partType, IPartState<P> partState, INetwork network, IPartNetwork partNetwork, PartTarget target, int slot) {
        if (this.offsetVariablesSlotMessages.size() > slot) {
            this.offsetVariablesSlotMessages.remove(slot);
        }

        InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator = offsetVariableEvaluators.get(slot);
        evaluator.refreshVariable(network, false);
        IVariable<ValueTypeInteger.ValueInteger> variable = evaluator.getVariable(network);
        if (variable != null) {
            try {
                // Refresh the recipe if variable is changed
                // The map is needed because we only want to register the listener once for each variable
                if (!this.offsetVariableListeners.containsKey(variable)) {
                    variable.addInvalidationListener(() -> {
                        this.offsetVariableListeners.remove(variable);
                        this.offsetVariableSlotDirty.add(slot);
                    });
                    this.offsetVariableListeners.put(variable, true);
                }

                IValue value = variable.getValue();
                if (value.getType() == ValueTypes.INTEGER) {
                    int valueRaw = ((ValueTypeInteger.ValueInteger) value).getRawValue();
                    Vec3i offset = partState.getTargetOffset();
                    if (slot == 0) {
                        offset = new Vec3i(valueRaw, offset.getY(), offset.getZ());
                    }
                    if (slot == 1) {
                        offset = new Vec3i(offset.getX(), valueRaw, offset.getZ());
                    }
                    if (slot == 2) {
                        offset = new Vec3i(offset.getX(), offset.getY(), valueRaw);
                    }
                    boolean valid = partType.setTargetOffset(partState, offset);
                    if (!valid) {
                        this.offsetVariablesSlotMessages.put(slot, Component.translatable("gui.integrateddynamics.partoffset.slot.message.outofrange"));
                        partState.markDirty();
                    }
                } else {
                    this.offsetVariablesSlotMessages.put(slot, Component.translatable("gui.integrateddynamics.partoffset.slot.message.noint"));
                    partState.markDirty();
                }
            } catch (EvaluationException e) {
                this.offsetVariablesSlotMessages.put(slot, e.getErrorMessage());
                partState.markDirty();
            }
        } else if (evaluator.hasVariable()) {
            this.offsetVariableSlotDirty.add(slot);
        }

        try {
            MinecraftForge.EVENT_BUS.post(new PartVariableDrivenVariableContentsUpdatedEvent<>(network,
                    partNetwork, target,
                    partType, partState, null, variable,
                    variable != null ? variable.getValue() : null));
        } catch (EvaluationException e) {
            // Ignore error
        }
    }

}
