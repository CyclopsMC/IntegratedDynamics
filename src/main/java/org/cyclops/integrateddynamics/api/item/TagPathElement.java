package org.cyclops.integrateddynamics.api.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;

/**
 * @author rubensworks
 */
public record TagPathElement(CompoundTag compoundTag) implements ProblemReporter.PathElement {
    @Override
    public String get() {
        return "tag@" + compoundTag.toString();
    }
}
