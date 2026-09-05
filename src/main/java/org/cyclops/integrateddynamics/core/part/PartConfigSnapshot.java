package org.cyclops.integrateddynamics.core.part;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.IntegratedDynamics;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * An immutable snapshot of the configuration of a part, which can be pasted onto another part.
 *
 * Only things that a player can configure are stored,
 * so no part id, max offset, enabled state, error messages or active aspect.
 *
 * Only values that differ from the defaults of the copied part are stored,
 * so that pasting only overwrites what was deliberately configured.
 *
 * @param version The version of this snapshot format.
 * @param sourcePartType The unique name of the part type this snapshot was taken from.
 * @param partSettings The non-default general part settings.
 * @param aspectProperties The serialized non-default aspect properties, by aspect unique name.
 * @param variableCards All variable cards.
 * @author rubensworks
 */
public record PartConfigSnapshot(int version,
                                 ResourceLocation sourcePartType,
                                 Optional<PartSettings> partSettings,
                                 Map<ResourceLocation, CompoundTag> aspectProperties,
                                 List<VariableCard> variableCards) {

    public static final int VERSION = 1;

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
                    Vec3i.CODEC.optionalFieldOf("targetOffset").forGetter(PartSettings::targetOffset)
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
                            .optionalFieldOf("variableCards", List.of()).forGetter(PartConfigSnapshot::variableCards)
            )
            .apply(builder, PartConfigSnapshot::new));

    /**
     * @return The number of blank Variable Cards that pasting this snapshot needs at most.
     */
    public int getRequiredBlankVariables() {
        return variableCards().size();
    }

    /**
     * @param section A config section.
     * @return If this snapshot holds anything for the given section.
     */
    public boolean hasSection(PartConfigSection section) {
        return switch (section) {
            case PART_SETTINGS -> partSettings().isPresent();
            case ASPECT_PROPERTIES -> !aspectProperties().isEmpty();
            case VARIABLE_CARDS -> !variableCards().isEmpty();
        };
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
     */
    public record PartSettings(Optional<Integer> updateInterval, Optional<Integer> priority, Optional<Integer> channel,
                               Optional<Direction> targetSide, Optional<Vec3i> targetOffset) {

        /**
         * @return If no setting at all is stored.
         */
        public boolean isEmpty() {
            return updateInterval().isEmpty() && priority().isEmpty() && channel().isEmpty()
                    && targetSide().isEmpty() && targetOffset().isEmpty();
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
