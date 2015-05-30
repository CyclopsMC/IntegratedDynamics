package org.cyclops.integrateddynamics.core.part.aspect;

import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.part.PartTarget;

import java.util.List;

/**
 * An element that can be used inside parts to access a specific aspect of something to read/write.
 * @author rubensworks
 */
public interface IAspect<V extends IValue, T extends IValueType<V>> {

    /**
     * @return The unique unlocalized name for this aspect.
     */
    public String getUnlocalizedName();

    /**
     * Add tooltip lines for this aspect when hovered in a gui..
     * @param lines The list to add lines to.
     */
    public void loadTooltip(List<String> lines);

    /**
     * @return The type of value this aspect can return.
     */
    public T getValueType();

    /**
     * Creates a new variable for this aspect.
     * @param target The target for this aspect.
     * @return The variable pointing to the given target.
     */
    public IAspectVariable<V> createNewVariable(PartTarget target);

}
