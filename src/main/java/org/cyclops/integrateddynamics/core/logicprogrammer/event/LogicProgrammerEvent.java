package org.cyclops.integrateddynamics.core.logicprogrammer.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

/**
 * A logic programmer event that is posted in the Forge event bus.
 * @author rubensworks
 */
public class LogicProgrammerEvent extends Event {

    private final Player player;

    public LogicProgrammerEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
