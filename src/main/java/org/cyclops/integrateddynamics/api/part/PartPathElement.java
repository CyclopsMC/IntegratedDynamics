package org.cyclops.integrateddynamics.api.part;

import net.minecraft.core.BlockPos;
import net.minecraft.util.ProblemReporter;

/**
 * @author rubensworks
 */
public record PartPathElement(BlockPos pos) implements ProblemReporter.PathElement {
    @Override
    public String get() {
        return "part@" + this.pos;
    }
}
