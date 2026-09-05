package org.cyclops.integrateddynamics.core.part;

import com.google.common.collect.Sets;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * The separate sections of a part configuration that can be copied and pasted.
 * @author rubensworks
 */
public enum PartConfigSection {

    /**
     * Update interval, priority, channel, target side, target offset, and the offset variables.
     */
    PART_SETTINGS,
    /**
     * The active variable, the statically configured aspect properties, and the aspect setting variables.
     */
    ASPECT;

    /**
     * All sections, which together form the whole configuration of a part.
     */
    public static final Set<PartConfigSection> ALL = Sets.immutableEnumSet(EnumSet.allOf(PartConfigSection.class));

    /**
     * @param inventoryName The name of a variable inventory inside a part.
     * @return The section that the variables in that inventory belong to.
     */
    public static PartConfigSection forInventoryName(String inventoryName) {
        if (PartConfigSnapshot.INVENTORY_NAME_ACTIVE.equals(inventoryName)
                || inventoryName.startsWith(PartStateAspectVariablesHandler.INVENTORY_NAME_PREFIX)) {
            return ASPECT;
        }
        // The offset variables, and any inventory that an addon adds, are part-level state
        return PART_SETTINGS;
    }

    public String getTranslationKey() {
        return "item.integrateddynamics.wrench.mode.config.section." + name().toLowerCase(Locale.ENGLISH);
    }

}
