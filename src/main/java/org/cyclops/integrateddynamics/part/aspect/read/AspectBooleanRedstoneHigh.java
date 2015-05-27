package org.cyclops.integrateddynamics.part.aspect.read;

/**
 * Aspect that can detect when a redstone level is maximum.
 * @author rubensworks
 */
public class AspectBooleanRedstoneHigh extends AspectBooleanRedstoneBase {
    @Override
    protected boolean getValue(int redstoneLevel) {
        return redstoneLevel == 15;
    }
}
