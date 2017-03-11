package org.cyclops.integrateddynamics.core.logicprogrammer.event;

import net.minecraft.entity.player.EntityPlayer;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;

/**
 * A logic programmer event that is posted in the Forge event bus when a new variable is created.
 * @author rubensworks
 */
public class LogicProgrammerVariableFacadeCreatedEvent extends LogicProgrammerEvent {

    private final IVariableFacade variableFacade;

    public LogicProgrammerVariableFacadeCreatedEvent(EntityPlayer player, IVariableFacade variableFacade) {
        super(player);
        this.variableFacade = variableFacade;
    }

    public IVariableFacade getVariableFacade() {
        return variableFacade;
    }
}
