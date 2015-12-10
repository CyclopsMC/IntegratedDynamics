package org.cyclops.integrateddynamics.part.aspect.read.minecraft;

import net.minecraft.server.MinecraftServer;
import org.cyclops.integrateddynamics.api.part.PartTarget;

/**
 * Aspect that can check the current amount of players in the server.
 * @author rubensworks
 */
public class AspectReadIntegerMinecraftPlayerCount extends AspectReadIntegerMinecraftBase {

    @Override
    protected String getUnlocalizedIntegerMinecraftType() {
        return "playercount";
    }

    @Override
    protected int getValue(PartTarget target) {
        return MinecraftServer.getServer().getCurrentPlayerCount();
    }

}
