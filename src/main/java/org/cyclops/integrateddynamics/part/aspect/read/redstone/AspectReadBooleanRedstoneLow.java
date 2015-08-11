package org.cyclops.integrateddynamics.part.aspect.read.redstone;

/**
 * Aspect that can detect when a redstone level is zero.
 * @author rubensworks
 */
public class AspectReadBooleanRedstoneLow extends AspectReadBooleanRedstoneBase {
    @Override
    protected boolean getValue(int redstoneLevel) {
        return redstoneLevel == 0;
    }

    @Override
    protected String getUnlocalizedBooleanRedstoneType() {
        return "low";
    }
}
