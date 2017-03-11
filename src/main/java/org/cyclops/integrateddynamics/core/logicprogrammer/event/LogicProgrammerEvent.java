package org.cyclops.integrateddynamics.core.logicprogrammer.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * A logic programmer event that is posted in the Forge event bus.
 * @author rubensworks
 */
public class LogicProgrammerEvent extends Event {

    private final EntityPlayer player;

    public LogicProgrammerEvent(EntityPlayer player) {
        this.player = player;
    }

    public EntityPlayer getPlayer() {
        return player;
    }
}
