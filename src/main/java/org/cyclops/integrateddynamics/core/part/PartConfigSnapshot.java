package org.cyclops.integrateddynamics.core.part;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * An immutable snapshot of the configuration of a part, which can be pasted onto another part.
 *
 * Only things that a player can configure are stored,
 * so no part id, enabled state, error messages or active aspect.
 *
 * Only values that differ from the defaults of the copied part are stored,
 * so that pasting only overwrites what was deliberately configured.
 *
 * @param version The version of this snapshot format.
 * @param sourcePartType The unique name of the part type this snapshot was taken from.
 * @param partSettings The non-default general part settings.
 * @param aspectProperties The serialized non-default aspect properties, by aspect unique name.
 * @param variableCards All variables, of every section.
 * @param extraData The state that the part type itself stored, by section.
 * @param disabledEntries The entries that the player switched off, which are kept but not pasted.
 * @author rubensworks
 */
public record PartConfigSnapshot(int version,
                                 ResourceLocation sourcePartType,
                                 Optional<PartSettings> partSettings,
                                 Map<ResourceLocation, CompoundTag> aspectProperties,
                                 List<VariableCard> variableCards,
                                 Map<PartConfigSection, CompoundTag> extraData,
                                 List<String> disabledEntries) {

    /**
     * A snapshot in which nothing is switched off yet.
     */
    public PartConfigSnapshot(int version, ResourceLocation sourcePartType, Optional<PartSettings> partSettings,
                              Map<ResourceLocation, CompoundTag> aspectProperties, List<VariableCard> variableCards,
                              Map<PartConfigSection, CompoundTag> extraData) {
        this(version, sourcePartType, partSettings, aspectProperties, variableCards, extraData, List.of());
    }

    public static final int VERSION = 1;

    public static final String SETTING_UPDATE_INTERVAL = "update_interval";
    public static final String SETTING_PRIORITY = "priority";
    public static final String SETTING_CHANNEL = "channel";
    public static final String SETTING_TARGET_SIDE = "target_side";
    public static final String SETTING_TARGET_OFFSET = "target_offset";
    public static final String SETTING_MAX_OFFSET = "max_offset";

    /**
     * The inventory name under which the active variable inventory of a part is stored.
     * This can not clash with named inventories, as those are derived from resource locations.
     */
    public static final String INVENTORY_NAME_ACTIVE = "$active";

    public static final Codec<PartSettings> CODEC_PART_SETTINGS = RecordCodecBuilder.create(builder -> builder
            .group(
                    Codec.INT.optionalFieldOf("updateInterval").forGetter(PartSettings::updateInterval),
                    Codec.INT.optionalFieldOf("priority").forGetter(PartSettings::priority),
                    Codec.INT.optionalFieldOf("channel").forGetter(PartSettings::channel),
                    Direction.CODEC.optionalFieldOf("targetSide").forGetter(PartSettings::targetSide),
                    Vec3i.CODEC.optionalFieldOf("targetOffset").forGetter(PartSettings::targetOffset),
                    Codec.INT.optionalFieldOf("maxOffset").forGetter(PartSettings::maxOffset)
            )
            .apply(builder, PartSettings::new));

    // Lazy, so that snapshots without variable cards can be (de)serialized without the item registry being present
    public static final Codec<VariableCard> CODEC_VARIABLE_CARD = Codec.lazyInitialized(
            () -> RecordCodecBuilder.create(builder -> builder
                    .group(
                            Codec.STRING.fieldOf("inventoryName").forGetter(VariableCard::inventoryName),
                            Codec.INT.fieldOf("slot").forGetter(VariableCard::slot),
                            ItemStack.CODEC.fieldOf("itemStack").forGetter(VariableCard::itemStack)
                    )
                    .apply(builder, VariableCard::new)));

    public static final Codec<PartConfigSnapshot> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(
                    Codec.INT.fieldOf("version").forGetter(PartConfigSnapshot::version),
                    ResourceLocation.CODEC.fieldOf("sourcePartType").forGetter(PartConfigSnapshot::sourcePartType),
                    CODEC_PART_SETTINGS.optionalFieldOf("partSettings").forGetter(PartConfigSnapshot::partSettings),
                    Codec.unboundedMap(ResourceLocation.CODEC, CompoundTag.CODEC)
                            .optionalFieldOf("aspectProperties", Map.of()).forGetter(PartConfigSnapshot::aspectProperties),
                    CODEC_VARIABLE_CARD.listOf()
                            .optionalFieldOf("variableCards", List.of()).forGetter(PartConfigSnapshot::variableCards),
                    Codec.unboundedMap(PartConfigSection.CODEC, CompoundTag.CODEC)
                            .optionalFieldOf("extraData", Map.of()).forGetter(PartConfigSnapshot::extraData),
                    Codec.STRING.listOf()
                            .optionalFieldOf("disabledEntries", List.of()).forGetter(PartConfigSnapshot::disabledEntries)
            )
            .apply(builder, PartConfigSnapshot::new));

    /**
     * @param sections The sections that will be pasted.
     * @return The variables that this snapshot holds for the given sections.
     */
    public List<VariableCard> getVariableCards(Set<PartConfigSection> sections) {
        return variableCards().stream()
                .filter(card -> sections.contains(PartConfigSection.forInventoryName(card.inventoryName())))
                .filter(card -> isEnabled(PartConfigEntry.idVariableCard(card.inventoryName(), card.slot())))
                .toList();
    }

    /**
     * Part types can store anything that this snapshot does not know about itself,
     * such as the state that an addon adds to its own part types.
     *
     * @param section A config section.
     * @return What the part type stored for the given section, which is empty if it stored nothing.
     */
    public CompoundTag getExtraData(PartConfigSection section) {
        return extraData().getOrDefault(section, new CompoundTag());
    }

    /**
     * The offset enhancements that a part holds can not be taken out again without breaking the part,
     * so pasting them has to consume enhancements from the player, just like the variable cards do.
     *
     * @param sections The sections that will be pasted.
     * @return The offset enhancement value that pasting those sections needs at most.
     */
    public int getRequiredMaxOffset(Set<PartConfigSection> sections) {
        if (!sections.contains(PartConfigSection.PART_SETTINGS)) {
            return 0;
        }
        return isEnabled(PartConfigEntry.idPartSetting(SETTING_MAX_OFFSET))
                ? partSettings().flatMap(PartSettings::maxOffset).orElse(0) : 0;
    }

    /**
     * @param sections The sections that will be pasted.
     * @return The number of blank Variable Cards that pasting those sections needs at most.
     */
    public int getRequiredBlankVariables(Set<PartConfigSection> sections) {
        return getVariableCards(sections).size();
    }

    /**
     * @param id The identifier of an entry.
     * @return If that entry is pasted, as opposed to being switched off by the player.
     */
    public boolean isEnabled(String id) {
        return !disabledEntries().contains(id);
    }

    /**
     * @param id The identifier of an entry.
     * @param enabled If that entry should be pasted.
     * @return A copy of this snapshot in which that entry is switched on or off.
     */
    public PartConfigSnapshot withEntryEnabled(String id, boolean enabled) {
        if (isEnabled(id) == enabled) {
            return this;
        }
        List<String> disabled = Lists.newArrayList(disabledEntries());
        if (enabled) {
            disabled.remove(id);
        } else {
            disabled.add(id);
        }
        return new PartConfigSnapshot(version(), sourcePartType(), partSettings(), aspectProperties(),
                variableCards(), extraData(), disabled);
    }

    /**
     * The entries are what the player switches on and off,
     * so this lists everything that this snapshot holds, whether it is switched on or not.
     *
     * @return Everything inside this snapshot, in the order that it is shown in.
     */
    public List<PartConfigEntry> getEntries(ValueDeseralizationContext valueDeseralizationContext) {
        List<PartConfigEntry> entries = Lists.newArrayList();
        IPartType<?, ?> partType = PartTypes.REGISTRY.getPartType(sourcePartType());
        Component groupSettings = Component.translatable(PartConfigSection.PART_SETTINGS.getTranslationKey());

        partSettings().ifPresent(settings -> {
            addPartSetting(entries, groupSettings, SETTING_UPDATE_INTERVAL,
                    settings.updateInterval().map(value -> Component.literal(String.valueOf(value))));
            addPartSetting(entries, groupSettings, SETTING_PRIORITY,
                    settings.priority().map(value -> Component.literal(String.valueOf(value))));
            addPartSetting(entries, groupSettings, SETTING_CHANNEL,
                    settings.channel().map(value -> Component.literal(String.valueOf(value))));
            addPartSetting(entries, groupSettings, SETTING_TARGET_SIDE,
                    settings.targetSide().map(value -> Component.literal(value.getSerializedName())));
            addPartSetting(entries, groupSettings, SETTING_TARGET_OFFSET,
                    settings.targetOffset().map(value -> Component.literal(value.toShortString())));
            addPartSetting(entries, groupSettings, SETTING_MAX_OFFSET,
                    settings.maxOffset().map(value -> Component.literal(String.valueOf(value))));
        });

        for (Map.Entry<ResourceLocation, CompoundTag> aspectEntry : aspectProperties().entrySet()) {
            IAspect<?, ?> aspect = Aspects.REGISTRY.getAspect(aspectEntry.getKey());
            Component group = aspect == null
                    ? Component.literal(aspectEntry.getKey().toString())
                    : Component.translatable(aspect.getTranslationKey());
            ListTag properties = aspectEntry.getValue().getList("map", Tag.TAG_COMPOUND);
            for (int i = 0; i < properties.size(); i++) {
                CompoundTag property = properties.getCompound(i);
                String label = property.getString("label");
                entries.add(new PartConfigEntry(
                        PartConfigEntry.idAspectProperty(aspectEntry.getKey(), label),
                        group, Component.translatable(label),
                        readPropertyValue(valueDeseralizationContext, property), PartConfigSection.ASPECT));
            }
        }

        for (VariableCard card : variableCards()) {
            PartConfigSection section = PartConfigSection.forInventoryName(card.inventoryName());
            entries.add(new PartConfigEntry(
                    PartConfigEntry.idVariableCard(card.inventoryName(), card.slot()),
                    Component.translatable("item.integrateddynamics.wrench.mode.config.entry.variable_cards"),
                    card.itemStack().getHoverName(), Component.empty(), section));
        }

        if (partType != null) {
            for (PartConfigSection section : extraData().keySet()) {
                entries.addAll(partType.getConfigExtraEntries(valueDeseralizationContext, this, section));
            }
        }

        return entries;
    }

    protected void addPartSetting(List<PartConfigEntry> entries, Component group, String setting,
                                  Optional<Component> value) {
        value.ifPresent(shown -> entries.add(new PartConfigEntry(PartConfigEntry.idPartSetting(setting), group,
                Component.translatable("item.integrateddynamics.wrench.mode.config.entry." + setting),
                shown, PartConfigSection.PART_SETTINGS)));
    }

    /**
     * @param valueDeseralizationContext A value deserialization context.
     * @param property One stored aspect property.
     * @return The value of that property, in the same compact shape that the part gui shows it in.
     */
    protected static Component readPropertyValue(ValueDeseralizationContext valueDeseralizationContext,
                                                 CompoundTag property) {
        IValueType valueType = ValueTypes.REGISTRY.getValueType(ResourceLocation.parse(property.getString("key")));
        if (valueType == null) {
            return Component.empty();
        }
        IValue value = ValueHelpers.deserializeRaw(valueDeseralizationContext, valueType, property.get("value"));
        if (value == null) {
            return Component.empty();
        }
        MutableComponent shown = valueType.toCompactString(value);
        return shown == null ? Component.empty() : shown.withStyle(valueType.getDisplayColorFormat());
    }

    /**
     * @param section A config section.
     * @return If this snapshot holds anything for the given section.
     */
    public boolean hasSection(PartConfigSection section) {
        // Only what is switched on is going to be pasted, so only that counts as being held for a section
        if (!getVariableCards(Set.of(section)).isEmpty()) {
            return true;
        }
        // What a part type stored itself is left to that part type, which decides per entry when it pastes
        if (!getExtraData(section).isEmpty()) {
            return true;
        }
        return switch (section) {
            case PART_SETTINGS -> partSettings().map(this::hasEnabledPartSetting).orElse(false);
            case ASPECT -> hasEnabledAspectProperty();
        };
    }

    protected boolean hasEnabledPartSetting(PartSettings settings) {
        return (settings.updateInterval().isPresent() && isEnabled(PartConfigEntry.idPartSetting(SETTING_UPDATE_INTERVAL)))
                || (settings.priority().isPresent() && isEnabled(PartConfigEntry.idPartSetting(SETTING_PRIORITY)))
                || (settings.channel().isPresent() && isEnabled(PartConfigEntry.idPartSetting(SETTING_CHANNEL)))
                || (settings.targetSide().isPresent() && isEnabled(PartConfigEntry.idPartSetting(SETTING_TARGET_SIDE)))
                || (settings.targetOffset().isPresent() && isEnabled(PartConfigEntry.idPartSetting(SETTING_TARGET_OFFSET)))
                || (settings.maxOffset().isPresent() && isEnabled(PartConfigEntry.idPartSetting(SETTING_MAX_OFFSET)));
    }

    protected boolean hasEnabledAspectProperty() {
        for (Map.Entry<ResourceLocation, CompoundTag> aspectEntry : aspectProperties().entrySet()) {
            ListTag properties = aspectEntry.getValue().getList("map", Tag.TAG_COMPOUND);
            for (int i = 0; i < properties.size(); i++) {
                if (isEnabled(PartConfigEntry.idAspectProperty(aspectEntry.getKey(),
                        properties.getCompound(i).getString("label")))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return All sections that this snapshot holds something for.
     */
    public Set<PartConfigSection> getSections() {
        Set<PartConfigSection> sections = EnumSet.noneOf(PartConfigSection.class);
        for (PartConfigSection section : PartConfigSection.values()) {
            if (hasSection(section)) {
                sections.add(section);
            }
        }
        return sections;
    }

    /**
     * @return If this snapshot holds nothing at all.
     */
    public boolean isEmpty() {
        return getSections().isEmpty();
    }

    /**
     * @param provider A holder lookup provider, used to serialize the variable cards.
     * @return The NBT representation of this snapshot.
     */
    public CompoundTag toNBT(HolderLookup.Provider provider) {
        return (CompoundTag) CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }

    /**
     * @param provider A holder lookup provider, used to deserialize the variable cards.
     * @param tag An NBT representation of a snapshot.
     * @return The snapshot, or empty if it could not be read.
     */
    public static Optional<PartConfigSnapshot> fromNBT(HolderLookup.Provider provider, CompoundTag tag) {
        return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag)
                .resultOrPartial(error -> IntegratedDynamics.clog(org.apache.logging.log4j.Level.ERROR,
                        String.format("Could not read a part configuration snapshot: %s", error)));
    }

    /**
     * The general settings of a part.
     * @param updateInterval The tick interval at which the part updates.
     * @param priority The priority of the part in its network.
     * @param channel The channel of the part in its network.
     * @param targetSide The overridden side of the target block, if any.
     * @param targetOffset The target position offset.
     * @param maxOffset The maximum offset that offset enhancements raised the part to.
     */
    public record PartSettings(Optional<Integer> updateInterval, Optional<Integer> priority, Optional<Integer> channel,
                               Optional<Direction> targetSide, Optional<Vec3i> targetOffset,
                               Optional<Integer> maxOffset) {

        /**
         * @return If no setting at all is stored.
         */
        public boolean isEmpty() {
            return updateInterval().isEmpty() && priority().isEmpty() && channel().isEmpty()
                    && targetSide().isEmpty() && targetOffset().isEmpty() && maxOffset().isEmpty();
        }
    }

    /**
     * A variable card inside one of the inventories of a part.
     * @param inventoryName The name of the named inventory,
     *                      or {@link #INVENTORY_NAME_ACTIVE} for the active variable inventory.
     * @param slot The slot inside that inventory.
     * @param itemStack The card.
     */
    public record VariableCard(String inventoryName, int slot, ItemStack itemStack) {
    }

}
