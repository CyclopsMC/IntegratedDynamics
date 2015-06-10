package org.cyclops.integrateddynamics.part.aspect.read;

/**
 * Aspect that can detect when a redstone level is non-zero.
 * @author rubensworks
 */
public class AspectReadBooleanRedstoneNonLow extends AspectReadBooleanRedstoneBase {
    @Override
    protected boolean getValue(int redstoneLevel) {
        return redstoneLevel > 0;
    }

    @Override
    protected String getUnlocalizedBooleanRedstoneType() {
        return "nonlow";
    }
}
