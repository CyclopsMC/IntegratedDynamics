package org.cyclops.integrateddynamics.part.aspect.read;

/**
 * Aspect that can detect when a redstone level is non-zero.
 * @author rubensworks
 */
public class AspectBooleanRedstoneNonLow extends AspectBooleanRedstoneBase {
    @Override
    protected boolean getValue(int redstoneLevel) {
        return redstoneLevel > 0;
    }
}
