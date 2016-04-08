package org.cyclops.integrateddynamics.core.block;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.block.properties.PropertyHelper;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;

import java.util.Collection;
import java.util.Locale;

/**
 * A block that is not visible to the player.
 * Just used for providing models, until a better way for doing this comes around.
 * @author rubensworks
 */
public class IgnoredBlockStatus extends IgnoredBlock {
    @BlockProperty(excludeFromMeta = true)
    public static final PropertyStatus STATUS = PropertyStatus.<Status>create("status", Status.class);

    /**
     * Make a new blockState instance.
     *
     * @param eConfig  Config for this blockState.
     */
    public IgnoredBlockStatus(ExtendedConfig eConfig) {
        super(eConfig);
    }

    public static class PropertyStatus extends PropertyHelper<Status> {

        private final ImmutableSet<Status> allowedValues;

        protected PropertyStatus(String name, Collection<Status> values) {
            super(name, Status.class);
            this.allowedValues = ImmutableSet.copyOf(values);
        }

        public Collection<Status> getAllowedValues()
        {
            return this.allowedValues;
        }

        @Override
        public Optional<Status> parseValue(String value) {
            return Optional.fromNullable(Status.valueOf(value.toUpperCase(Locale.ENGLISH)));
        }

        @Override
        public String getName(Status value) {
            return value.toString().toLowerCase(Locale.ENGLISH);
        }

        /**
         * Create a new PropertyStatus with all Enum constants of the given class.
         * @param name The property name.
         * @param clazz The property class.
         * @return The property
         */
        public static PropertyStatus create(String name, Class<Status> clazz) {
            return create(name, clazz, Predicates.<Status>alwaysTrue());
        }

        /**
         * Create a new PropertyStatus with all Enum constants of the given class.
         * @param name The property name.
         * @param clazz The property class.
         * @param filter The filter for checking property values.
         * @return The property
         */
        public static PropertyStatus create(String name, Class<Status> clazz, Predicate<Status> filter) {
            return create(name, clazz, Collections2.filter(Lists.newArrayList(clazz.getEnumConstants()), filter));
        }

        /**
         * Create a new PropertyStatus with all Enum constants of the given class.
         * @param name The property name.
         * @param clazz The property class.
         * @param values The possible property values.
         * @return The property
         */
        public static PropertyStatus create(String name, Class<Status> clazz, Collection<Status> values) {
            return new PropertyStatus(name, values);
        }

    }

    public enum Status {

        ACTIVE,
        INACTIVE,
        ERROR

    }

}
