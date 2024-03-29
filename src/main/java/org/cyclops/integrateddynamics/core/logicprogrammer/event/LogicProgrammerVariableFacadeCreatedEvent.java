package org.cyclops.integrateddynamics.core.logicprogrammer.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;

import javax.annotation.Nullable;

/**
 * A logic programmer event that is posted in the Forge event bus when a new variable is created.
 * @author rubensworks
 */
public class LogicProgrammerVariableFacadeCreatedEvent extends LogicProgrammerEvent {

    private final IVariableFacade variableFacade;
    @Nullable
    private final BlockState blockState;

    public LogicProgrammerVariableFacadeCreatedEvent(Player player, IVariableFacade variableFacade, BlockState blockState) {
        super(player);
        this.variableFacade = variableFacade;
        this.blockState = blockState;
    }

    public IVariableFacade getVariableFacade() {
        return variableFacade;
    }

    @Nullable
    public BlockState getBlockState() {
        return blockState;
    }
}
