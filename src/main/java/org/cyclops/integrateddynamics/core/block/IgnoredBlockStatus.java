package org.cyclops.integrateddynamics.core.block;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.block.properties.PropertyHelper;
import net.minecraft.block.state.BlockState;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.block.property.BlockPropertyManagerComponent;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;

import java.util.Collection;

/**
 * A block that is not visible to the player.
 * Just used for providing models, until a better way for doing this comes around.
 * @author rubensworks
 */
public class IgnoredBlockStatus extends IgnoredBlock {
    @BlockProperty
    public static final PropertyStatus STATUS = PropertyStatus.create("status", Status.class);

    /**
     * Make a new blockState instance.
     *
     * @param eConfig  Config for this blockState.
     */
    public IgnoredBlockStatus(ExtendedConfig eConfig) {
        super(eConfig);
    }

    @Override protected BlockState createBlockState() {
        return (propertyManager = new BlockPropertyManagerComponent(this) {
            @Override
            protected boolean ignoreMetaOverflow() {
                return true;
            }
        }).createDelegatedBlockState();
    }

    public static class PropertyStatus extends PropertyHelper {

        private final ImmutableSet allowedValues;

        protected PropertyStatus(String name, Collection values) {
            super(name, Status.class);
            this.allowedValues = ImmutableSet.copyOf(values);
        }

        public Collection getAllowedValues()
        {
            return this.allowedValues;
        }

        @Override
        public String getName(Comparable value) {
            return value.toString();
        }

        /**
         * Create a new PropertyStatus with all Enum constants of the given class.
         * @param name The property name.
         * @param clazz The property class.
         * @return The property
         */
        public static PropertyStatus create(String name, Class clazz) {
            return create(name, clazz, Predicates.alwaysTrue());
        }

        /**
         * Create a new PropertyStatus with all Enum constants of the given class.
         * @param name The property name.
         * @param clazz The property class.
         * @param filter The filter for checking property values.
         * @return The property
         */
        public static PropertyStatus create(String name, Class clazz, Predicate filter) {
            return create(name, clazz, Collections2.filter(Lists.newArrayList(clazz.getEnumConstants()), filter));
        }

        /**
         * Create a new PropertyStatus with all Enum constants of the given class.
         * @param name The property name.
         * @param clazz The property class.
         * @param values The possible property values.
         * @return The property
         */
        public static PropertyStatus create(String name, Class clazz, Collection values) {
            return new PropertyStatus(name, values);
        }

    }

    public enum Status {

        ACTIVE,
        INACTIVE,
        ERROR

    }

}
