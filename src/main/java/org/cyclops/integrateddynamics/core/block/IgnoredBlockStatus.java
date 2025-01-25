package org.cyclops.integrateddynamics.core.block;

import com.google.common.base.Predicates;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Locale;

/**
 * A block that is not visible to the player.
 * Just used for providing models, until a better way for doing this comes around.
 * @author rubensworks
 */
public class IgnoredBlockStatus extends IgnoredBlock {

    public static final EnumProperty<Status> STATUS = EnumProperty.create("status", Status.class, Predicates.alwaysTrue());

    public IgnoredBlockStatus(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(STATUS, Status.INACTIVE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STATUS);
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
