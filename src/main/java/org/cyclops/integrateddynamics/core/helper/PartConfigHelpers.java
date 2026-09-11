package org.cyclops.integrateddynamics.core.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.GeneralConfig;
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
import org.cyclops.integrateddynamics.core.item.VariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.network.PartNetworkElement;
import org.cyclops.integrateddynamics.core.part.PartConfigApplyResult;
import org.cyclops.integrateddynamics.core.part.PartConfigEntry;
import org.cyclops.integrateddynamics.core.part.PartConfigSection;
import org.cyclops.integrateddynamics.core.part.PartConfigSnapshot;
import org.cyclops.integrateddynamics.core.part.PartStateActiveVariableBase;
import org.cyclops.integrateddynamics.core.part.PartStateAspectVariablesHandler;
import org.cyclops.integrateddynamics.core.part.PartStateOffsetHandler;
import org.cyclops.integrateddynamics.core.part.PartTypeAspects;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;
import org.cyclops.integrateddynamics.part.aspect.Aspects;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Helpers for copying and pasting part configurations.
 *
 * All methods in here are meant to be called server-side only.
 *
 * @author rubensworks
 */
public final class PartConfigHelpers {

    private static final Logger LOGGER = LogUtils.getLogger();

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

        Predicate<IAspect> copiedAspects = getCopiedAspects(state);
        Map<Identifier, CompoundTag> aspectProperties = Maps.newLinkedHashMap();
        if (sections.contains(PartConfigSection.ASPECT)) {
            for (IAspect aspect : getAspects(partType)) {
                if (copiedAspects.test(aspect) && aspect.hasProperties()) {
                    IAspectProperties properties = state.getAspectProperties(aspect);
                    if (properties != null) {
                        IAspectProperties modified = filterNonDefaultProperties(properties, aspect);
                        if (countPropertyTypes(modified) > 0) {
                            aspectProperties.put(aspect.getUniqueName(), writeProperties(valueDeseralizationContext, modified));
                        }
                    }
                }
            }
        }

        // Each variable inventory belongs to one of the sections, so only copy the ones that are included
        List<PartConfigSnapshot.VariableCard> variableCards = Lists.newArrayList();
        for (Map.Entry<String, NonNullList<ItemStack>> entry : state.getInventoriesNamed().entrySet()) {
            if (!sections.contains(PartConfigSection.forInventoryName(entry.getKey()))) {
                continue;
            }
            IAspect inventoryAspect = PartStateAspectVariablesHandler.getAspectByInventoryName(entry.getKey());
            if (inventoryAspect != null && !copiedAspects.test(inventoryAspect)) {
                continue;
            }
            NonNullList<ItemStack> inventory = entry.getValue();
            for (int slot = 0; slot < inventory.size(); slot++) {
                if (!inventory.get(slot).isEmpty()) {
                    variableCards.add(new PartConfigSnapshot.VariableCard(entry.getKey(), slot,
                            inventory.get(slot).copy()));
                }
            }
        }
        if (state instanceof PartStateActiveVariableBase<?> activeState
                && sections.contains(PartConfigSection.forInventoryName(PartConfigSnapshot.INVENTORY_NAME_ACTIVE))) {
            SimpleInventory inventory = activeState.getInventory();
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                if (!inventory.getItem(slot).isEmpty()) {
                    variableCards.add(new PartConfigSnapshot.VariableCard(
                            PartConfigSnapshot.INVENTORY_NAME_ACTIVE, slot, inventory.getItem(slot).copy()));
                }
            }
        }

        // Give the part type itself the chance to store what this snapshot does not know about
        Map<PartConfigSection, CompoundTag> extraData = Maps.newLinkedHashMap();
        for (PartConfigSection section : sections) {
            CompoundTag tag = partType.snapshotConfigExtra(valueDeseralizationContext, state, section);
            if (!tag.isEmpty()) {
                extraData.put(section, tag);
            }
        }

        return new PartConfigSnapshot(PartConfigSnapshot.VERSION, partType.getUniqueName(),
                partSettings, aspectProperties, variableCards, extraData, List.of());
    }

    /**
     * A writer only ever does what its active aspect says, so the aspects that it is not writing
     * hold configuration that does nothing, and pasting it onto another part would only confuse.
     * Parts without an active aspect, such as readers, use all of their aspects at once.
     *
     * @param state A part state.
     * @return Which aspects of that part are worth copying.
     */
    protected static Predicate<IAspect> getCopiedAspects(IPartState<?> state) {
        if (state instanceof IPartStateWriter<?> writerState) {
            IAspect activeAspect = writerState.getActiveAspect();
            return aspect -> aspect == activeAspect;
        }
        return aspect -> true;
    }

    /**
     * @param partType A part type.
     * @param state A part state.
     * @return The settings of the given part that differ from the defaults of a freshly placed part.
     */
    @SuppressWarnings("unchecked")
    protected static PartConfigSnapshot.PartSettings snapshotPartSettings(IPartType partType, IPartState<?> state) {
        int updateInterval = partType.getUpdateInterval(state);
        int defaultUpdateInterval = Math.max(partType.getMinimumUpdateInterval(state), state.getDefaultUpdateIntervalPublic());
        Vec3i targetOffset = partType.getTargetOffset(state);
        return new PartConfigSnapshot.PartSettings(
                updateInterval == defaultUpdateInterval ? Optional.empty() : Optional.of(updateInterval),
                partType.getPriority(state) == 0 ? Optional.empty() : Optional.of(partType.getPriority(state)),
                partType.getChannel(state) == 0 ? Optional.empty() : Optional.of(partType.getChannel(state)),
                Optional.ofNullable(partType.getTargetSideOverride(state)),
                targetOffset.equals(Vec3i.ZERO) ? Optional.empty() : Optional.of(targetOffset),
                state.getMaxOffset() == 0 ? Optional.empty() : Optional.of(state.getMaxOffset()));
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
            applyPartSettings(network, target, partType, state, snapshot, player, result);
        }
        if (sections.contains(PartConfigSection.ASPECT)) {
            applyAspectProperties(valueDeseralizationContext, target, partType, state, snapshot, result);
        }
        applyVariableCards(valueDeseralizationContext, target, partType, state,
                snapshot.getVariableCards(sections), player, result);

        // Give the part type itself the chance to paste back what this snapshot does not know about
        for (PartConfigSection section : sections) {
            if (!snapshot.getExtraData(section).isEmpty()) {
                partType.applyConfigExtra(valueDeseralizationContext, target, state, section, snapshot, player, result);
            }
        }

        return result;
    }

    /**
     * Only the settings that the snapshot actually holds are applied,
     * so pasting never resets a setting that was left at its default on the copied part.
     */
    @SuppressWarnings("unchecked")
    protected static void applyPartSettings(@Nullable INetwork network, PartTarget target, IPartType partType,
                                            IPartState<?> state, PartConfigSnapshot snapshot,
                                            Player player, PartConfigApplyResult result) {
        PartConfigSnapshot.PartSettings settings = snapshot.partSettings().get();
        Optional<Integer> updateInterval = enabled(snapshot, PartConfigSnapshot.SETTING_UPDATE_INTERVAL,
                settings.updateInterval());
        Optional<Direction> targetSide = enabled(snapshot, PartConfigSnapshot.SETTING_TARGET_SIDE,
                settings.targetSide());
        Optional<Integer> maxOffset = enabled(snapshot, PartConfigSnapshot.SETTING_MAX_OFFSET, settings.maxOffset());
        Optional<Vec3i> targetOffset = enabled(snapshot, PartConfigSnapshot.SETTING_TARGET_OFFSET,
                settings.targetOffset());
        Optional<Integer> priority = enabled(snapshot, PartConfigSnapshot.SETTING_PRIORITY, settings.priority());
        Optional<Integer> channel = enabled(snapshot, PartConfigSnapshot.SETTING_CHANNEL, settings.channel());
        if (updateInterval.isEmpty() && targetSide.isEmpty() && maxOffset.isEmpty()
                && targetOffset.isEmpty() && priority.isEmpty() && channel.isEmpty()) {
            // Everything that was copied is switched off
            return;
        }

        updateInterval.ifPresent(interval -> partType.setUpdateInterval(state,
                Math.max(partType.getMinimumUpdateInterval(state), interval)));
        targetSide.ifPresent(side -> partType.setTargetSideOverride(state, side));
        // Before the target offset, as that one is bounded by the maximum offset
        maxOffset.ifPresent(offset -> applyMaxOffset(partType, state, offset, player, result));
        targetOffset.ifPresent(offset -> {
            if (!partType.setTargetOffset(state, target.getCenter(), offset)) {
                result.setOffsetFailed(true);
            }
        });
        if (priority.isPresent() || channel.isPresent()) {
            int priorityValue = priority.orElseGet(() -> partType.getPriority(state));
            int channelValue = channel.orElseGet(() -> partType.getChannel(state));
            if (network != null) {
                network.setPriorityAndChannel(new PartNetworkElement(partType, target.getCenter()),
                        priorityValue, channelValue);
            } else {
                state.setPriority(priorityValue);
                state.setChannel(channelValue);
            }
        }
        result.setPartSettingsApplied(true);
        state.markDirty();
        state.sendUpdate();
    }

    /**
     * @param snapshot A snapshot.
     * @param setting The name of a general part setting.
     * @param value The value that the snapshot holds for it.
     * @return That value, or empty if the player switched the setting off.
     */
    protected static <T> Optional<T> enabled(PartConfigSnapshot snapshot, String setting, Optional<T> value) {
        return snapshot.isEnabled(PartConfigEntry.idPartSetting(setting)) ? value : Optional.empty();
    }

    /**
     * Raise the maximum offset of the given part to the given value,
     * by consuming offset enhancements from the player.
     *
     * Enhancements can not be split, so the player can end up spending a bit more value than needed.
     *
     * @param partType The part type.
     * @param state The part state.
     * @param maxOffset The maximum offset to raise the part to.
     * @param player The player that is pasting.
     * @param result The outcome to report into.
     */
    protected static void applyMaxOffset(IPartType partType, IPartState<?> state, int maxOffset,
                                         Player player, PartConfigApplyResult result) {
        if (!partType.supportsOffsets()) {
            return;
        }
        int required = Math.min(maxOffset, GeneralConfig.maxPartOffset) - state.getMaxOffset();
        if (required <= 0) {
            return;
        }

        int consumed = required;
        if (!player.isCreative()) {
            int available = countOffsetEnhancements(player);
            if (available < required) {
                result.setMissingMaxOffset(required - available);
                return;
            }
            consumed = consumeOffsetEnhancements(player, required);
        }

        int before = state.getMaxOffset();
        state.setMaxOffset(Math.min(before + consumed, GeneralConfig.maxPartOffset));
        result.setAppliedMaxOffset(state.getMaxOffset() - before);
    }

    @SuppressWarnings("unchecked")
    protected static void applyAspectProperties(ValueDeseralizationContext valueDeseralizationContext, PartTarget target,
                                                IPartType partType, IPartState<?> state, PartConfigSnapshot snapshot,
                                                PartConfigApplyResult result) {
        Set<IAspect> aspects = getAspects(partType);
        for (Map.Entry<Identifier, CompoundTag> entry : snapshot.aspectProperties().entrySet()) {
            IAspect aspect = Aspects.REGISTRY.getAspect(entry.getKey());
            if (aspect == null || !aspects.contains(aspect)) {
                // The target part does not have this aspect
                continue;
            }
            IAspectProperties source = readProperties(valueDeseralizationContext, entry.getValue());
            IAspectProperties properties = aspect.getStaticProperties(partType, target, state).clone();
            int applied = applyPropertiesByType(source, properties, aspect, propertyType -> snapshot.isEnabled(
                    PartConfigEntry.idAspectProperty(entry.getKey(), propertyType.getTranslationKey())));
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
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(LOGGER)) {
            properties.deserialize(TagValueInput.create(scopedCollector,
                    valueDeseralizationContext.holderLookupProvider(), tag));
        }
        return properties;
    }

    /**
     * @param valueDeseralizationContext A value deserialization context.
     * @param properties Aspect properties.
     * @return The serialized aspect properties.
     */
    public static CompoundTag writeProperties(ValueDeseralizationContext valueDeseralizationContext,
                                              IAspectProperties properties) {
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(LOGGER)) {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(scopedCollector,
                    valueDeseralizationContext.holderLookupProvider());
            properties.serialize(valueOutput);
            return valueOutput.buildResult();
        }
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
        return applyPropertiesByType(source, properties, aspect, propertyType -> true);
    }

    /**
     * @param source The properties to copy from.
     * @param properties The properties to copy into.
     * @param aspect The aspect that the target properties belong to.
     * @param filter Which of the source properties may be copied.
     * @return The number of copied properties.
     */
    @SuppressWarnings({"unchecked", "deprecation"})
    public static int applyPropertiesByType(IAspectProperties source, IAspectProperties properties, IAspect<?, ?> aspect,
                                            Predicate<IAspectPropertyTypeInstance> filter) {
        Collection<IAspectPropertyTypeInstance> sourceTypes = source.getTypes();
        int applied = 0;
        for (IAspectPropertyTypeInstance propertyType : aspect.getPropertyTypes()) {
            if (sourceTypes.contains(propertyType) && filter.test(propertyType)) {
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
        Map<String, Set<Integer>> keptSlots = Maps.newLinkedHashMap();
        int required = 0;
        for (Map.Entry<String, List<PartConfigSnapshot.VariableCard>> entry : cardsByInventory.entrySet()) {
            SimpleInventory inventory = resolveInventory(partType, state, entry.getKey());
            if (inventory == null) {
                result.addCardsSkipped(entry.getValue().size());
                continue;
            }
            inventories.put(entry.getKey(), inventory);
            for (PartConfigSnapshot.VariableCard card : entry.getValue()) {
                if (card.slot() >= inventory.getContainerSize()) {
                    result.addCardsSkipped(1);
                } else if (isSameVariable(inventory.getItem(card.slot()), card.itemStack())) {
                    // The part already holds this very variable, so pasting it would only cost the player a card
                    keptSlots.computeIfAbsent(entry.getKey(), name -> Sets.newHashSet()).add(card.slot());
                } else {
                    required++;
                }
            }
        }
        if (required == 0 && keptSlots.isEmpty()) {
            return;
        }

        // Consume the required blank variable cards
        if (required > 0 && !player.isCreative()) {
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
            Set<Integer> kept = keptSlots.getOrDefault(entry.getKey(), Set.of());
            boolean changed = false;

            // Give the cards that are currently present back to the player
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack current = inventory.getItem(slot);
                if (!current.isEmpty() && !kept.contains(slot)) {
                    giveOrDrop(player, current);
                    inventory.setItem(slot, ItemStack.EMPTY);
                    changed = true;
                }
            }

            for (PartConfigSnapshot.VariableCard card : cardsByInventory.get(entry.getKey())) {
                if (card.slot() < inventory.getContainerSize() && !kept.contains(card.slot())) {
                    inventory.setItem(card.slot(), copyVariable(valueDeseralizationContext, card.itemStack()));
                    result.addCardsPasted(1);
                    changed = true;
                }
            }

            if (changed) {
                saveInventory(partType, state, target, entry.getKey(), inventory, player);
            }
        }
    }

    /**
     * Two variable cards that only differ in the identifier of their variable produce the same value,
     * so replacing one by the other would change nothing about the part that holds it.
     *
     * @param itemStack A variable card, which can be empty.
     * @param other Another variable card.
     * @return If both cards hold the same variable.
     */
    public static boolean isSameVariable(ItemStack itemStack, ItemStack other) {
        if (!itemStack.is(RegistryEntries.ITEM_VARIABLE.get()) || !other.is(RegistryEntries.ITEM_VARIABLE.get())) {
            return false;
        }
        CompoundTag tag = itemStack.get(RegistryEntries.DATACOMPONENT_VARIABLE_FACADE.get());
        CompoundTag otherTag = other.get(RegistryEntries.DATACOMPONENT_VARIABLE_FACADE.get());
        if (tag == null || otherTag == null) {
            return false;
        }
        tag = tag.copy();
        tag.remove(VariableFacadeHandlerRegistry.KEY_ID);
        otherTag = otherTag.copy();
        otherTag.remove(VariableFacadeHandlerRegistry.KEY_ID);
        return tag.equals(otherTag);
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
                partTypeWriter.updateActivation(target, (IPartStateWriter) state, player, false);
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
            LabelsWorldStorage labels = LabelsWorldStorage.Access.getInstance(IntegratedDynamics._instance).get();
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
     * @param player A player.
     * @return The total offset enhancement value in the inventory of the given player.
     */
    public static int countOffsetEnhancements(Player player) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            count += getOffsetEnhancementValue(player.getInventory().getItem(slot))
                    * player.getInventory().getItem(slot).getCount();
        }
        return count;
    }

    /**
     * Remove offset enhancements from the inventory of the given player,
     * starting at the smallest ones so that as little value as possible is wasted.
     *
     * @param player A player.
     * @param value The offset enhancement value to remove.
     * @return The removed value, which can be higher than requested when enhancements did not add up exactly.
     */
    public static int consumeOffsetEnhancements(Player player, int value) {
        List<Integer> slots = Lists.newArrayList();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (getOffsetEnhancementValue(player.getInventory().getItem(slot)) > 0) {
                slots.add(slot);
            }
        }
        slots.sort(Comparator.comparingInt(slot -> getOffsetEnhancementValue(player.getInventory().getItem(slot))));

        int consumed = 0;
        for (int slot : slots) {
            ItemStack itemStack = player.getInventory().getItem(slot);
            int enhancementValue = getOffsetEnhancementValue(itemStack);
            while (consumed < value && !itemStack.isEmpty()) {
                itemStack.shrink(1);
                consumed += enhancementValue;
            }
            if (itemStack.isEmpty()) {
                player.getInventory().setItem(slot, ItemStack.EMPTY);
            }
            if (consumed >= value) {
                break;
            }
        }
        return consumed;
    }

    /**
     * @param itemStack An item stack.
     * @return The offset that the given stack enhances a part by, or zero if it is not an offset enhancement.
     */
    public static int getOffsetEnhancementValue(ItemStack itemStack) {
        return itemStack.is(RegistryEntries.ITEM_ENHANCEMENT_OFFSET.get())
                ? RegistryEntries.ITEM_ENHANCEMENT_OFFSET.get().getEnhancementValue(itemStack) : 0;
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

}
