package org.cyclops.integrateddynamics.core.part;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * One thing inside a {@link PartConfigSnapshot} that the player can switch off before pasting.
 *
 * @param id The identifier of this entry, which is stable across reloads,
 *           as it is what a disabled entry is remembered by.
 * @param group What this entry belongs to, such as the aspect that a property belongs to.
 * @param label The name of this entry.
 * @param value What is stored for it, which is empty when the name says it all.
 * @param icon The item to show in the slot of this entry, which is empty when it has no slot.
 * @param aspect The unique name of the aspect that this entry stands for, which is shown as an item next to its name,
 *               and empty for an entry that is not an aspect itself.
 * @param color The colour that this entry is shown in, which is the display colour of the value type it belongs to,
 *              and empty for entries that belong to no value type.
 * @param section The configuration section that this entry belongs to.
 * @author rubensworks
 */
public record PartConfigEntry(String id, Component group, Component label, Component value, ItemStack icon,
                              Optional<Identifier> aspect, OptionalInt color, PartConfigSection section) {

    /**
     * An entry that is shown without an item next to it, and in no colour of its own.
     */
    public PartConfigEntry(String id, Component group, Component label, Component value, PartConfigSection section) {
        this(id, group, label, value, ItemStack.EMPTY, Optional.empty(), OptionalInt.empty(), section);
    }

    public static final String PREFIX_PART_SETTINGS = "settings";
    public static final String PREFIX_ASPECT = "aspect";
    public static final String PREFIX_VARIABLE_CARD = "card";
    public static final String PREFIX_EXTRA = "extra";

    /**
     * @param setting The name of a general part setting.
     * @return The identifier of that setting.
     */
    public static String idPartSetting(String setting) {
        return PREFIX_PART_SETTINGS + ":" + setting;
    }

    /**
     * @param aspect The unique name of an aspect.
     * @param property The name of one of its properties.
     * @return The identifier of that property.
     */
    public static String idAspectProperty(Identifier aspect, String property) {
        return PREFIX_ASPECT + ":" + aspect + ":" + property;
    }

    /**
     * @param inventoryName The name of a variable inventory inside a part.
     * @param slot A slot in that inventory.
     * @return The identifier of the variable in that slot.
     */
    public static String idVariableCard(String inventoryName, int slot) {
        return PREFIX_VARIABLE_CARD + ":" + inventoryName + ":" + slot;
    }

    /**
     * Part types must build the identifiers of their own entries with this,
     * so that they can not collide with the entries of another part type or of another section.
     *
     * @param section The section that the entry belongs to.
     * @param key A name for the entry that is unique within the part type.
     * @return The identifier of that entry.
     */
    public static String idExtra(PartConfigSection section, String key) {
        return PREFIX_EXTRA + ":" + section.getSerializedName() + ":" + key;
    }

}
