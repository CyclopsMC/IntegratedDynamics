package org.cyclops.integrateddynamics.core.logicprogrammer.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * A logic programmer event that is posted in the Forge event bus.
 * @author rubensworks
 */
public class LogicProgrammerEvent extends Event {

    private final PlayerEntity player;

    public LogicProgrammerEvent(PlayerEntity player) {
        this.player = player;
    }

    public PlayerEntity getPlayer() {
        return player;
    }
}
