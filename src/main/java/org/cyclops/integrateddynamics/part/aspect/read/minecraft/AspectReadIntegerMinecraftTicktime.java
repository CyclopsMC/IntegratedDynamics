package org.cyclops.integrateddynamics.part.aspect.read.minecraft;

import com.google.common.math.DoubleMath;
import net.minecraft.server.MinecraftServer;
import org.cyclops.integrateddynamics.api.part.PartTarget;

/**
 * Aspect that can check the average tick time on the server.
 * @author rubensworks
 */
public class AspectReadIntegerMinecraftTicktime extends AspectReadIntegerMinecraftBase {

    @Override
    protected String getUnlocalizedIntegerMinecraftType() {
        return "ticktime";
    }

    @Override
    protected int getValue(PartTarget target) {
        return (int) DoubleMath.mean(MinecraftServer.getServer().tickTimeArray);
    }

}
