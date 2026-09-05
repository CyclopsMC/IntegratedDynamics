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
     * Update interval, priority, channel, target side override and target offset.
     */
    PART_SETTINGS,
    /**
     * The statically configured properties (settings) of aspects.
     */
    ASPECT_PROPERTIES,
    /**
     * The variable cards inside the part.
     */
    VARIABLE_CARDS;

    /**
     * All sections, as copied by the Wrench.
     */
    public static final Set<PartConfigSection> ALL = Sets.immutableEnumSet(EnumSet.allOf(PartConfigSection.class));

    public String getTranslationKey() {
        return "item.integrateddynamics.wrench.mode.config.section." + name().toLowerCase(Locale.ENGLISH);
    }

}
