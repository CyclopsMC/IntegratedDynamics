package org.cyclops.integrateddynamics.core.part;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.evaluate.InventoryVariableEvaluator;
import org.cyclops.integrateddynamics.core.part.event.PartVariableDrivenVariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles variables that dynamically determine aspect properties (aspect settings) inside part states.
 *
 * Each aspect that has properties can have an inventory with one variable slot for each of its properties.
 * If a variable is present in such a slot, its value will override the statically configured property value.
 *
 * @author rubensworks
 */
public class PartStateAspectVariablesHandler<P extends IPartType> {

    /**
     * Prefix of the named inventories in which the aspect setting variables are stored.
     * The prefix is followed by the unique name of the aspect.
     */
    public static final String INVENTORY_NAME_PREFIX = "aspectVariables_";

    protected final Map<IAspect, AspectVariables> aspectVariables = Maps.newIdentityHashMap();
    protected boolean dirty = true;

    /**
     * @param aspect An aspect.
     * @return The name of the named inventory in which the setting variables of the given aspect are stored.
     */
    public static String getInventoryName(IAspect aspect) {
        return INVENTORY_NAME_PREFIX + aspect.getUniqueName();
    }

    /**
     * @param inventoryName A named inventory name.
     * @return The aspect this inventory name refers to, or null if it is not an aspect variables inventory.
     */
    @Nullable
    public static IAspect getAspectByInventoryName(String inventoryName) {
        if (!inventoryName.startsWith(INVENTORY_NAME_PREFIX)) {
            return null;
        }
        ResourceLocation aspectName = ResourceLocation.tryParse(inventoryName.substring(INVENTORY_NAME_PREFIX.length()));
        return aspectName == null ? null : Aspects.REGISTRY.getAspect(aspectName);
    }

    /**
     * @param aspect An aspect.
     * @return The ordered list of property types of the given aspect.
     */
    public static List<IAspectPropertyTypeInstance> getPropertyTypes(IAspect aspect) {
        return Lists.newArrayList(aspect.getPropertyTypes());
    }

    /**
     * Get the variables inventory for the given aspect within the given part state.
     * @param partState A part state.
     * @param aspect An aspect.
     * @return The inventory, which has one slot for each property of the aspect.
     */
    public static SimpleInventory getVariablesInventory(IPartState<?> partState, IAspect aspect) {
        SimpleInventory inventory = new SimpleInventory(getPropertyTypes(aspect).size(), 1);
        partState.loadInventoryNamed(getInventoryName(aspect), inventory);
        return inventory;
    }

    /**
     * Indicate that the contents of one of the aspect variables inventories have changed.
     */
    public void markAspectVariablesChanged() {
        this.dirty = true;
    }

    /**
     * Indicate that the statically configured properties of the given aspect have changed,
     * so that any derived properties must be recalculated.
     * @param aspect An aspect.
     */
    public void invalidateDerivedProperties(IAspect aspect) {
        AspectVariables variables = this.aspectVariables.get(aspect);
        if (variables != null) {
            variables.invalidateDerivedProperties();
        }
    }

    /**
     * Get the properties of the given aspect after applying all variable-driven property values.
     * @param aspect An aspect.
     * @param baseProperties The statically configured properties.
     * @return The derived properties, or null if no variable-driven values are present.
     */
    @Nullable
    public IAspectProperties getDerivedProperties(IAspect aspect, IAspectProperties baseProperties) {
        AspectVariables variables = this.aspectVariables.get(aspect);
        return variables == null ? null : variables.getDerivedProperties(baseProperties);
    }

    /**
     * @param aspect An aspect.
     * @param slot A property slot.
     * @return The value that the variable in the given slot currently produces,
     *         or null if the slot has no (valid) variable.
     */
    @Nullable
    public IValue getAspectVariableValue(IAspect aspect, int slot) {
        AspectVariables variables = this.aspectVariables.get(aspect);
        return variables == null ? null : variables.getSlotValue(slot);
    }

    /**
     * @param aspect An aspect.
     * @param slot A property slot.
     * @return The current error in the given slot, or null if there is no error.
     */
    @Nullable
    public MutableComponent getAspectVariableError(IAspect aspect, int slot) {
        AspectVariables variables = this.aspectVariables.get(aspect);
        return variables == null ? null : variables.getSlotError(slot);
    }

    /**
     * @return If at least one aspect has a variable-driven property.
     */
    public boolean hasAspectVariables() {
        return !this.aspectVariables.isEmpty();
    }

    /**
     * Tick all aspect variables.
     * @param partType The part type.
     * @param partState The part state.
     * @param network The network.
     * @param partNetwork The part network.
     * @param target The part target.
     */
    public void updateAspectVariables(P partType, IPartState<P> partState, INetwork network, IPartNetwork partNetwork, PartTarget target) {
        // Reload all aspect variables if needed
        if (this.dirty) {
            this.dirty = false;
            reloadAspectVariables(partType, partState, network, partNetwork, target);
        }

        // Only update single slots if needed
        for (AspectVariables variables : Lists.newArrayList(this.aspectVariables.values())) {
            variables.updateDirtySlots(partType, partState, network, partNetwork, target);
        }
    }

    /**
     * Reload all aspect variables based on the named inventories inside the given part state.
     * @param partType The part type.
     * @param partState The part state.
     * @param network The network.
     * @param partNetwork The part network.
     * @param target The part target.
     */
    public void reloadAspectVariables(P partType, IPartState<P> partState, INetwork network, IPartNetwork partNetwork, PartTarget target) {
        // Determine all aspects that have at least one variable
        Set<IAspect> presentAspects = Sets.newIdentityHashSet();
        for (Map.Entry<String, NonNullList<ItemStack>> entry : partState.getInventoriesNamed().entrySet()) {
            IAspect aspect = getAspectByInventoryName(entry.getKey());
            if (aspect != null && aspect.hasProperties()
                    && entry.getValue().stream().anyMatch(itemStack -> !itemStack.isEmpty())) {
                presentAspects.add(aspect);
            }
        }

        // Forget about aspects that no longer have variables
        this.aspectVariables.keySet().retainAll(presentAspects);

        // (Re)load all aspects that have variables
        for (IAspect aspect : presentAspects) {
            AspectVariables variables = this.aspectVariables.get(aspect);
            if (variables == null) {
                variables = new AspectVariables(aspect);
                this.aspectVariables.put(aspect, variables);
            }
            variables.reload(partType, partState, network, partNetwork, target);
        }
    }

    /**
     * Holder for the variables of all properties of a single aspect.
     */
    public class AspectVariables {

        private final IAspect aspect;
        private final List<IAspectPropertyTypeInstance> propertyTypes;
        private final List<InventoryVariableEvaluator<IValue>> evaluators = Lists.newArrayList();
        private final Int2ObjectMap<MutableComponent> slotMessages = new Int2ObjectArrayMap<>();
        private final Int2ObjectMap<IValue> slotValues = new Int2ObjectArrayMap<>();
        private final Int2ObjectMap<IVariable> slotVariables = new Int2ObjectArrayMap<>();
        private final Int2ObjectMap<IVariable> slotListenedVariables = new Int2ObjectArrayMap<>();
        private final IntSet dirtySlots = new IntArraySet();

        private IAspectProperties derivedProperties = null;
        private IAspectProperties derivedPropertiesBase = null;

        public AspectVariables(IAspect aspect) {
            this.aspect = aspect;
            this.propertyTypes = getPropertyTypes(aspect);
        }

        public void invalidateDerivedProperties() {
            this.derivedProperties = null;
            this.derivedPropertiesBase = null;
        }

        @Nullable
        public IAspectProperties getDerivedProperties(IAspectProperties baseProperties) {
            if (this.slotValues.isEmpty()) {
                return null;
            }
            if (this.derivedProperties == null || this.derivedPropertiesBase != baseProperties) {
                IAspectProperties derived = baseProperties.clone();
                for (Int2ObjectMap.Entry<IValue> entry : this.slotValues.int2ObjectEntrySet()) {
                    derived.setValue(this.propertyTypes.get(entry.getIntKey()), entry.getValue());
                }
                this.derivedProperties = derived;
                this.derivedPropertiesBase = baseProperties;
            }
            return this.derivedProperties;
        }

        @Nullable
        public IValue getSlotValue(int slot) {
            return this.slotValues.get(slot);
        }

        @Nullable
        public MutableComponent getSlotError(int slot) {
            return this.slotMessages.get(slot);
        }

        protected void setSlotErrors(int slot, List<MutableComponent> errors) {
            if (errors.isEmpty()) {
                this.slotMessages.remove(slot);
            } else {
                this.slotMessages.put(slot, errors.get(0));
            }
        }

        protected void setSlotValue(int slot, @Nullable IValue value) {
            if (value == null) {
                if (this.slotValues.remove(slot) != null) {
                    invalidateDerivedProperties();
                }
            } else if (!value.equals(this.slotValues.put(slot, value))) {
                invalidateDerivedProperties();
            }
        }

        public void reload(P partType, IPartState<P> partState, INetwork network, IPartNetwork partNetwork, PartTarget target) {
            this.dirtySlots.clear();
            SimpleInventory inventory = getVariablesInventory(partState, this.aspect);

            this.evaluators.clear();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                int slot = i;
                this.evaluators.add(new InventoryVariableEvaluator<>(
                        inventory, slot, ValueDeseralizationContext.of(target.getCenter().getPos().getLevel(true)),
                        (IValueType<IValue>) this.propertyTypes.get(slot).getType()) {
                    @Override
                    public void onErrorsChanged() {
                        super.onErrorsChanged();
                        setSlotErrors(slot, getErrors());
                    }
                });
            }

            for (int i = 0; i < inventory.getContainerSize(); i++) {
                reloadSlot(partType, partState, network, partNetwork, target, i);
            }
        }

        public void updateDirtySlots(P partType, IPartState<P> partState, INetwork network, IPartNetwork partNetwork, PartTarget target) {
            if (!this.dirtySlots.isEmpty()) {
                IntSet dirtySlotsCopy = new IntArraySet(this.dirtySlots);
                this.dirtySlots.clear();
                for (Integer slot : dirtySlotsCopy) {
                    reloadSlot(partType, partState, network, partNetwork, target, slot);
                }
            }
        }

        protected void reloadSlot(P partType, IPartState<P> partState, INetwork network, IPartNetwork partNetwork, PartTarget target, int slot) {
            this.slotMessages.remove(slot);
            IVariable lastVariable = this.slotVariables.get(slot);
            if (lastVariable != null) {
                lastVariable.invalidate();
            }

            InventoryVariableEvaluator<IValue> evaluator = this.evaluators.get(slot);
            evaluator.refreshVariable(network, false);
            IVariable<IValue> variable = evaluator.getVariable(network);
            if (variable != null) {
                this.slotVariables.put(slot, variable);
                try {
                    // Reload the slot if the variable is changed.
                    // The map is needed because we only want to register the listener once for each variable.
                    if (this.slotListenedVariables.get(slot) != variable) {
                        this.slotListenedVariables.put(slot, variable);
                        variable.addInvalidationListener(() -> {
                            this.slotListenedVariables.remove(slot);
                            this.dirtySlots.add(slot);
                        });
                    }

                    IAspectPropertyTypeInstance propertyType = this.propertyTypes.get(slot);
                    IValue value = variable.getValue();
                    if (value.getType() == propertyType.getType()) {
                        if (propertyType.getValidator().test(value)) {
                            setSlotValue(slot, value);
                        } else {
                            setSlotValue(slot, null);
                            this.slotMessages.put(slot, Component.translatable("gui.integrateddynamics.aspectsettings.slot.message.invalidvalue"));
                            partState.markDirty();
                        }
                    } else {
                        setSlotValue(slot, null);
                        this.slotMessages.put(slot, Component.translatable("gui.integrateddynamics.aspectsettings.slot.message.invalidtype",
                                Component.translatable(propertyType.getType().getTranslationKey()),
                                Component.translatable(value.getType().getTranslationKey())));
                        partState.markDirty();
                    }
                } catch (EvaluationException e) {
                    setSlotValue(slot, null);
                    this.slotMessages.put(slot, e.getErrorMessage());
                    partState.markDirty();
                }
            } else {
                setSlotValue(slot, null);
                this.slotVariables.remove(slot);
                if (evaluator.hasVariable()) {
                    // The variable could not be resolved yet, try again later
                    this.dirtySlots.add(slot);
                }
            }

            try {
                NeoForge.EVENT_BUS.post(new PartVariableDrivenVariableContentsUpdatedEvent<>(network,
                        partNetwork, target,
                        partType, partState, null, variable,
                        variable != null ? variable.getValue() : null));
            } catch (EvaluationException e) {
                // Ignore error
            }
        }

    }

}
