package org.cyclops.integrateddynamics.part.aspect.read.minecraft;

import org.cyclops.integrateddynamics.api.part.PartTarget;

import java.util.Random;

/**
 * Aspect that can retrieve a random value.
 * @author rubensworks
 */
public class AspectReadIntegerMinecraftRandom extends AspectReadIntegerMinecraftBase {

    private final Random random = new Random();

    @Override
    protected String getUnlocalizedIntegerMinecraftType() {
        return "random";
    }

    @Override
    protected int getValue(PartTarget target) {
        return random.nextInt();
    }

}
