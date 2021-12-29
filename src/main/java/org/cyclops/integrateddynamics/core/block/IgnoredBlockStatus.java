package org.cyclops.integrateddynamics.core.block;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Collection;
import java.util.Locale;

/**
 * A block that is not visible to the player.
 * Just used for providing models, until a better way for doing this comes around.
 * @author rubensworks
 */
public class IgnoredBlockStatus extends IgnoredBlock {

    public static final PropertyStatus STATUS = PropertyStatus.<Status>create("status");

    public IgnoredBlockStatus() {
        super();

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(STATUS, Status.INACTIVE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STATUS);
    }

    public static class PropertyStatus extends EnumProperty<Status> {

        protected PropertyStatus(String name, Collection<Status> values) {
            super(name, Status.class, values);
        }

        /**
         * Create a new PropertyStatus with all Enum constants of the given class.
         * @param name The property name.
         * @return The property
         */
        public static PropertyStatus create(String name) {
            return create(name, Predicates.alwaysTrue());
        }

        /**
         * Create a new PropertyStatus with all Enum constants of the given class.
         * @param name The property name.
         * @param filter The filter for checking property values.
         * @return The property
         */
        public static PropertyStatus create(String name, Predicate<Status> filter) {
            return create(name, Collections2.filter(Lists.newArrayList(Status.class.getEnumConstants()), filter));
        }

        /**
         * Create a new PropertyStatus with all Enum constants of the given class.
         * @param name The property name.
         * @param values The possible property values.
         * @return The property
         */
        public static PropertyStatus create(String name, Collection<Status> values) {
            return new PropertyStatus(name, values);
        }

    }

    public enum Status implements StringRepresentable {

        ACTIVE,
        INACTIVE,
        ERROR;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ENGLISH);
        }
    }

}
