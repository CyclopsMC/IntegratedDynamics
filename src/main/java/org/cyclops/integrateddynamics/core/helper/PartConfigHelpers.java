package org.cyclops.integrateddynamics.core.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.network.PartNetworkElement;
import org.cyclops.integrateddynamics.core.part.PartConfigApplyResult;
import org.cyclops.integrateddynamics.core.part.PartConfigSection;
import org.cyclops.integrateddynamics.core.part.PartConfigSnapshot;
import org.cyclops.integrateddynamics.core.part.PartStateActiveVariableBase;
import org.cyclops.integrateddynamics.core.part.PartStateAspectVariablesHandler;
import org.cyclops.integrateddynamics.core.part.PartStateOffsetHandler;
import org.cyclops.integrateddynamics.core.part.PartTypeAspects;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;
import org.cyclops.integrateddynamics.item.ItemWrench;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Helpers for copying and pasting part configurations.
 *
 * All methods in here are meant to be called server-side only.
 *
 * @author rubensworks
 */
public final class PartConfigHelpers {

    private PartConfigHelpers() {
    }

    /**
     * @param partType A part type.
     * @return All aspects that the given part type can hold.
     */
    public static Set<IAspect> getAspects(IPartType<?, ?> partType) {
        return partType instanceof PartTypeAspects<?, ?> partTypeAspects
                ? partTypeAspects.getAspects() : Collections.emptySet();
    }

    /**
     * Take a snapshot of the configuration of the given part.
     * @param valueDeseralizationContext A value deserialization context.
     * @param partType The part type.
     * @param state The part state.
     * @param sections The sections to include.
     * @return The snapshot.
     */
    @SuppressWarnings("unchecked")
    public static PartConfigSnapshot snapshot(ValueDeseralizationContext valueDeseralizationContext,
                                              IPartType partType, IPartState<?> state, Set<PartConfigSection> sections) {
        Optional<PartConfigSnapshot.PartSettings> partSettings = Optional.empty();
        if (sections.contains(PartConfigSection.PART_SETTINGS)) {
            PartConfigSnapshot.PartSettings settings = snapshotPartSettings(partType, state);
            if (!settings.isEmpty()) {
                partSettings = Optional.of(settings);
            }
        }

        Map<ResourceLocation, CompoundTag> aspectProperties = Maps.newLinkedHashMap();
        if (sections.contains(PartConfigSection.ASPECT_PROPERTIES)) {
            for (IAspect aspect : getAspects(partType)) {
                if (aspect.hasProperties()) {
                    IAspectProperties properties = state.getAspectProperties(aspect);
                    if (properties != null) {
                        IAspectProperties modified = filterNonDefaultProperties(properties, aspect);
                        if (countPropertyTypes(modified) > 0) {
                            aspectProperties.put(aspect.getUniqueName(), modified.toNBT(valueDeseralizationContext));
                        }
                    }
                }
            }
        }

        List<PartConfigSnapshot.VariableCard> variableCards = Lists.newArrayList();
        if (sections.contains(PartConfigSection.VARIABLE_CARDS)) {
            for (Map.Entry<String, NonNullList<ItemStack>> entry : state.getInventoriesNamed().entrySet()) {
                NonNullList<ItemStack> inventory = entry.getValue();
                for (int slot = 0; slot < inventory.size(); slot++) {
                    if (!inventory.get(slot).isEmpty()) {
                        variableCards.add(new PartConfigSnapshot.VariableCard(entry.getKey(), slot,
                                inventory.get(slot).copy()));
                    }
                }
            }
            if (state instanceof PartStateActiveVariableBase<?> activeState) {
                SimpleInventory inventory = activeState.getInventory();
                for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                    if (!inventory.getItem(slot).isEmpty()) {
                        variableCards.add(new PartConfigSnapshot.VariableCard(
                                PartConfigSnapshot.INVENTORY_NAME_ACTIVE, slot, inventory.getItem(slot).copy()));
                    }
                }
            }
        }

        return new PartConfigSnapshot(PartConfigSnapshot.VERSION, partType.getUniqueName(),
                partSettings, aspectProperties, variableCards);
    }

    /**
     * @param partType A part type.
     * @param state A part state.
     * @return The settings of the given part that differ from the defaults of a freshly placed part.
     */
    @SuppressWarnings("unchecked")
    protected static PartConfigSnapshot.PartSettings snapshotPartSettings(IPartType partType, IPartState<?> state) {
        int updateInterval = partType.getUpdateInterval(state);
        int defaultUpdateInterval = Math.max(partType.getMinimumUpdateInterval(state), state.getDefaultUpdateInterval());
        Vec3i targetOffset = partType.getTargetOffset(state);
        return new PartConfigSnapshot.PartSettings(
                updateInterval == defaultUpdateInterval ? Optional.empty() : Optional.of(updateInterval),
                partType.getPriority(state) == 0 ? Optional.empty() : Optional.of(partType.getPriority(state)),
                partType.getChannel(state) == 0 ? Optional.empty() : Optional.of(partType.getChannel(state)),
                Optional.ofNullable(partType.getTargetSideOverride(state)),
                targetOffset.equals(Vec3i.ZERO) ? Optional.empty() : Optional.of(targetOffset));
    }

    /**
     * @param properties The properties of an aspect.
     * @param aspect The aspect that they belong to.
     * @return Only the properties whose value differs from the aspect's default.
     */
    @SuppressWarnings({"unchecked", "deprecation"})
    protected static IAspectProperties filterNonDefaultProperties(IAspectProperties properties, IAspect<?, ?> aspect) {
        IAspectProperties defaultProperties = aspect.getDefaultProperties();
        IAspectProperties modified = new AspectProperties();
        for (IAspectPropertyTypeInstance propertyType : properties.getTypes()) {
            if (!properties.getValue(propertyType).equals(defaultProperties.getValue(propertyType))) {
                modified.setValue(propertyType, properties.getValue(propertyType));
            }
        }
        return modified;
    }

    /**
     * Paste the given snapshot onto the given part.
     * @param valueDeseralizationContext A value deserialization context.
     * @param network The network of the part, or null if it is not in a network.
     * @param target The part target.
     * @param partType The part type.
     * @param state The part state.
     * @param snapshot The snapshot to paste.
     * @param sections The sections to paste.
     * @param player The player that is pasting, used to consume and eject variable cards.
     * @return The outcome.
     */
    public static PartConfigApplyResult apply(ValueDeseralizationContext valueDeseralizationContext,
                                              @Nullable INetwork network, PartTarget target,
                                              IPartType partType, IPartState<?> state, PartConfigSnapshot snapshot,
                                              Set<PartConfigSection> sections, Player player) {
        PartConfigApplyResult result = new PartConfigApplyResult();

        if (sections.contains(PartConfigSection.PART_SETTINGS) && snapshot.partSettings().isPresent()) {
            applyPartSettings(network, target, partType, state, snapshot.partSettings().get(), result);
        }
        if (sections.contains(PartConfigSection.ASPECT_PROPERTIES)) {
            applyAspectProperties(valueDeseralizationContext, target, partType, state, snapshot, result);
        }
        if (sections.contains(PartConfigSection.VARIABLE_CARDS)) {
            applyVariableCards(valueDeseralizationContext, target, partType, state,
                    snapshot.variableCards(), player, result);
        }

        return result;
    }

    /**
     * Only the settings that the snapshot actually holds are applied,
     * so pasting never resets a setting that was left at its default on the copied part.
     */
    @SuppressWarnings("unchecked")
    protected static void applyPartSettings(@Nullable INetwork network, PartTarget target, IPartType partType,
                                            IPartState<?> state, PartConfigSnapshot.PartSettings settings,
                                            PartConfigApplyResult result) {
        settings.updateInterval().ifPresent(updateInterval -> partType.setUpdateInterval(state,
                Math.max(partType.getMinimumUpdateInterval(state), updateInterval)));
        settings.targetSide().ifPresent(targetSide -> partType.setTargetSideOverride(state, targetSide));
        settings.targetOffset().ifPresent(targetOffset -> {
            if (!partType.setTargetOffset(state, target.getCenter(), targetOffset)) {
                result.setOffsetFailed(true);
            }
        });
        if (settings.priority().isPresent() || settings.channel().isPresent()) {
            int priority = settings.priority().orElseGet(() -> partType.getPriority(state));
            int channel = settings.channel().orElseGet(() -> partType.getChannel(state));
            if (network != null) {
                network.setPriorityAndChannel(new PartNetworkElement(partType, target.getCenter()), priority, channel);
            } else {
                state.setPriority(priority);
                state.setChannel(channel);
            }
        }
        result.setPartSettingsApplied(true);
        state.markDirty();
        state.sendUpdate();
    }

    @SuppressWarnings("unchecked")
    protected static void applyAspectProperties(ValueDeseralizationContext valueDeseralizationContext, PartTarget target,
                                                IPartType partType, IPartState<?> state, PartConfigSnapshot snapshot,
                                                PartConfigApplyResult result) {
        Set<IAspect> aspects = getAspects(partType);
        for (Map.Entry<ResourceLocation, CompoundTag> entry : snapshot.aspectProperties().entrySet()) {
            IAspect aspect = Aspects.REGISTRY.getAspect(entry.getKey());
            if (aspect == null || !aspects.contains(aspect)) {
                // The target part does not have this aspect
                continue;
            }
            IAspectProperties source = readProperties(valueDeseralizationContext, entry.getValue());
            IAspectProperties properties = aspect.getStaticProperties(partType, target, state).clone();
            int applied = applyPropertiesByType(source, properties, aspect);
            if (applied > 0) {
                aspect.setProperties(partType, target, state, properties);
            }
            result.addAppliedProperties(applied);
            result.addSkippedProperties(countPropertyTypes(source) - applied);
        }
    }

    /**
     * @param valueDeseralizationContext A value deserialization context.
     * @param tag Serialized aspect properties.
     * @return The deserialized aspect properties.
     */
    public static IAspectProperties readProperties(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag) {
        IAspectProperties properties = new AspectProperties();
        properties.fromNBT(valueDeseralizationContext, tag);
        return properties;
    }

    /**
     * Copy all property values from the source into the target properties,
     * for all property types that the given aspect declares.
     *
     * Property types are matched by their value type and translation key,
     * so properties can also be copied between different aspects.
     *
     * @param source The properties to copy from.
     * @param properties The properties to copy into.
     * @param aspect The aspect that the target properties belong to.
     * @return The number of copied properties.
     */
    @SuppressWarnings({"unchecked", "deprecation"})
    public static int applyPropertiesByType(IAspectProperties source, IAspectProperties properties, IAspect<?, ?> aspect) {
        Collection<IAspectPropertyTypeInstance> sourceTypes = source.getTypes();
        int applied = 0;
        for (IAspectPropertyTypeInstance propertyType : aspect.getPropertyTypes()) {
            if (sourceTypes.contains(propertyType)) {
                properties.setValue(propertyType, source.getValue(propertyType));
                applied++;
            }
        }
        return applied;
    }

    @SuppressWarnings("deprecation")
    protected static int countPropertyTypes(IAspectProperties properties) {
        return properties.getTypes().size();
    }

    /**
     * Paste the given variable cards into the given part.
     *
     * Only the inventories that occur in the given cards are touched:
     * their current contents are given back to the player, and replaced by copies of the given cards.
     * Each pasted card consumes one blank Variable Card from the player's inventory,
     * unless the player is in creative mode.
     *
     * @param valueDeseralizationContext A value deserialization context.
     * @param target The part target.
     * @param partType The part type.
     * @param state The part state.
     * @param cards The cards to paste.
     * @param player The player that is pasting.
     * @param result The outcome to report into.
     */
    public static void applyVariableCards(ValueDeseralizationContext valueDeseralizationContext, PartTarget target,
                                          IPartType partType, IPartState<?> state,
                                          List<PartConfigSnapshot.VariableCard> cards, Player player,
                                          PartConfigApplyResult result) {
        // Group the cards by inventory, and resolve the inventories that the target part actually has
        Map<String, List<PartConfigSnapshot.VariableCard>> cardsByInventory = Maps.newLinkedHashMap();
        for (PartConfigSnapshot.VariableCard card : cards) {
            cardsByInventory.computeIfAbsent(card.inventoryName(), name -> Lists.newArrayList()).add(card);
        }
        Map<String, SimpleInventory> inventories = Maps.newLinkedHashMap();
        int required = 0;
        for (Map.Entry<String, List<PartConfigSnapshot.VariableCard>> entry : cardsByInventory.entrySet()) {
            SimpleInventory inventory = resolveInventory(partType, state, entry.getKey());
            if (inventory == null) {
                result.addCardsSkipped(entry.getValue().size());
                continue;
            }
            inventories.put(entry.getKey(), inventory);
            for (PartConfigSnapshot.VariableCard card : entry.getValue()) {
                if (card.slot() < inventory.getContainerSize()) {
                    required++;
                } else {
                    result.addCardsSkipped(1);
                }
            }
        }
        if (required == 0) {
            return;
        }

        // Consume the required blank variable cards
        if (!player.isCreative()) {
            int available = countBlankVariables(player);
            if (available < required) {
                result.setMissingBlanks(required - available);
                result.addCardsSkipped(required);
                return;
            }
            consumeBlankVariables(player, required);
        }

        for (Map.Entry<String, SimpleInventory> entry : inventories.entrySet()) {
            SimpleInventory inventory = entry.getValue();

            // Give the cards that are currently present back to the player
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack current = inventory.getItem(slot);
                if (!current.isEmpty()) {
                    giveOrDrop(player, current);
                    inventory.setItem(slot, ItemStack.EMPTY);
                }
            }

            for (PartConfigSnapshot.VariableCard card : cardsByInventory.get(entry.getKey())) {
                if (card.slot() < inventory.getContainerSize()) {
                    inventory.setItem(card.slot(), copyVariable(valueDeseralizationContext, card.itemStack()));
                    result.addCardsPasted(1);
                }
            }

            saveInventory(partType, state, target, entry.getKey(), inventory, player);
        }
    }

    /**
     * @param partType A part type.
     * @param state A part state.
     * @param inventoryName An inventory name from a snapshot.
     * @return The matching inventory inside the given part, or null if the part does not have it.
     */
    @Nullable
    public static SimpleInventory resolveInventory(IPartType partType, IPartState<?> state, String inventoryName) {
        if (PartConfigSnapshot.INVENTORY_NAME_ACTIVE.equals(inventoryName)) {
            return state instanceof PartStateActiveVariableBase<?> activeState ? activeState.getInventory() : null;
        }
        if (PartStateOffsetHandler.INVENTORY_NAME.equals(inventoryName)) {
            if (!partType.supportsOffsets()) {
                return null;
            }
            SimpleInventory inventory = new SimpleInventory(3, 1);
            state.loadInventoryNamed(inventoryName, inventory);
            return inventory;
        }
        IAspect aspect = PartStateAspectVariablesHandler.getAspectByInventoryName(inventoryName);
        if (aspect != null) {
            return getAspects(partType).contains(aspect)
                    ? PartStateAspectVariablesHandler.getVariablesInventory(state, aspect) : null;
        }
        // Unknown named inventories are only pasted if the target part already has them
        NonNullList<ItemStack> existing = state.getInventoryNamed(inventoryName);
        if (existing == null) {
            return null;
        }
        SimpleInventory inventory = new SimpleInventory(existing.size(), 1);
        state.loadInventoryNamed(inventoryName, inventory);
        return inventory;
    }

    @SuppressWarnings("unchecked")
    protected static void saveInventory(IPartType partType, IPartState<?> state, PartTarget target, String inventoryName,
                                        SimpleInventory inventory, Player player) {
        if (PartConfigSnapshot.INVENTORY_NAME_ACTIVE.equals(inventoryName)) {
            // The active inventory is the live inventory of the part state, so it does not have to be saved
            if (partType instanceof IPartTypeWriter partTypeWriter) {
                partTypeWriter.updateActivation(target, (IPartStateWriter) state, player);
            } else if (state instanceof PartStateActiveVariableBase activeState) {
                activeState.onVariableContentsUpdated(partType, target);
            }
            return;
        }
        state.saveInventoryNamed(inventoryName, inventory);
        if (PartStateOffsetHandler.INVENTORY_NAME.equals(inventoryName)) {
            partType.onOffsetVariablesChanged(target, state);
        } else if (PartStateAspectVariablesHandler.getAspectByInventoryName(inventoryName) != null) {
            partType.onAspectVariablesChanged(target, state);
        }
    }

    /**
     * @param itemStack An item stack.
     * @return If the given stack is a Variable Card without any variable in it.
     */
    public static boolean isBlankVariable(ItemStack itemStack) {
        return itemStack.is(RegistryEntries.ITEM_VARIABLE.get())
                && !itemStack.has(RegistryEntries.DATACOMPONENT_VARIABLE_FACADE.get());
    }

    /**
     * @param player A player.
     * @return The number of blank Variable Cards in the inventory of the given player.
     */
    public static int countBlankVariables(Player player) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack itemStack = player.getInventory().getItem(slot);
            if (isBlankVariable(itemStack)) {
                count += itemStack.getCount();
            }
        }
        return count;
    }

    /**
     * Remove the given number of blank Variable Cards from the inventory of the given player.
     * @param player A player.
     * @param count The number of cards to remove.
     */
    public static void consumeBlankVariables(Player player, int count) {
        int remaining = count;
        for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
            ItemStack itemStack = player.getInventory().getItem(slot);
            if (isBlankVariable(itemStack)) {
                int consumed = Math.min(remaining, itemStack.getCount());
                itemStack.shrink(consumed);
                if (itemStack.isEmpty()) {
                    player.getInventory().setItem(slot, ItemStack.EMPTY);
                }
                remaining -= consumed;
            }
        }
    }

    /**
     * Copy the given variable card, so that the copy refers to a new variable.
     * @param valueDeseralizationContext A value deserialization context.
     * @param itemStack A variable card.
     * @return The copy.
     */
    public static ItemStack copyVariable(ValueDeseralizationContext valueDeseralizationContext, ItemStack itemStack) {
        if (!itemStack.is(RegistryEntries.ITEM_VARIABLE.get())
                || !itemStack.has(RegistryEntries.DATACOMPONENT_VARIABLE_FACADE.get())) {
            return itemStack.copy();
        }

        IVariableFacade facade = RegistryEntries.ITEM_VARIABLE.get()
                .getVariableFacade(valueDeseralizationContext, itemStack);
        ItemStack copy = IntegratedDynamics._instance.getRegistryManager()
                .getRegistry(IVariableFacadeHandlerRegistry.class).copy(true, itemStack);

        // If the original had a label, also copy the label
        if (facade.isValid()) {
            LabelsWorldStorage labels = LabelsWorldStorage.getInstance(IntegratedDynamics._instance);
            String label = labels.getLabel(facade.getId());
            if (label != null) {
                IVariableFacade facadeCopy = RegistryEntries.ITEM_VARIABLE.get()
                        .getVariableFacade(valueDeseralizationContext, copy);
                if (facadeCopy != null && facadeCopy.isValid()) {
                    labels.put(facadeCopy.getId(), label);
                }
            }
        }

        return copy;
    }

    /**
     * Give the given item stack to the player, or drop it on the ground if their inventory is full.
     * @param player A player.
     * @param itemStack An item stack.
     */
    public static void giveOrDrop(Player player, ItemStack itemStack) {
        if (!player.getInventory().add(itemStack) && !itemStack.isEmpty()) {
            Containers.dropItemStack(player.level(), player.getX(), player.getY(), player.getZ(), itemStack);
        }
    }

    /**
     * Find the Wrench that the given player is holding or carrying.
     * @param player A player.
     * @return The Wrench, or empty if the player has none.
     */
    public static Optional<ItemStack> findWrench(Player player) {
        for (ItemStack itemStack : List.of(player.getMainHandItem(), player.getOffhandItem())) {
            if (itemStack.getItem() instanceof ItemWrench) {
                return Optional.of(itemStack);
            }
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack itemStack = player.getInventory().getItem(slot);
            if (itemStack.getItem() instanceof ItemWrench) {
                return Optional.of(itemStack);
            }
        }
        return Optional.empty();
    }

    /**
     * @param provider A holder lookup provider.
     * @param wrench A Wrench.
     * @return The configuration snapshot inside the given Wrench, if any.
     */
    public static Optional<PartConfigSnapshot> getSnapshot(HolderLookup.Provider provider, ItemStack wrench) {
        CompoundTag tag = wrench.get(RegistryEntries.DATACOMPONENT_WRENCH_PART_CONFIG.get());
        return tag == null ? Optional.empty() : PartConfigSnapshot.fromNBT(provider, tag);
    }

    /**
     * Store the given configuration snapshot inside the given Wrench.
     * @param provider A holder lookup provider.
     * @param wrench A Wrench.
     * @param snapshot A configuration snapshot.
     */
    public static void setSnapshot(HolderLookup.Provider provider, ItemStack wrench, PartConfigSnapshot snapshot) {
        wrench.set(RegistryEntries.DATACOMPONENT_WRENCH_PART_CONFIG.get(), snapshot.toNBT(provider));
    }

    /**
     * @return A message telling the player that they need a Wrench to copy or paste a configuration.
     */
    public static Component getNoWrenchMessage() {
        return Component.translatable("gui.integrateddynamics.config.nowrench");
    }

}
