package org.cyclops.integrateddynamics.core.part.aspect;

import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.core.part.PartTarget;

import java.util.Comparator;
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
     * Add tooltip lines for this aspect when hovered in a gui.
     * @param lines The list to add lines to.
     * @param appendOptionalInfo If shift-to-show info should be added.
     */
    public void loadTooltip(List<String> lines, boolean appendOptionalInfo);

    /**
     * @return The type of value this aspect can handle.
     */
    public T getValueType();

    /**
     * Called inside part types for updating a part on a block.
     * @param partType The part type.
     * @param target The position that is targeted by the given part.
     * @param state The current state of the given part.
     * @param <P> The part type type.
     * @param <S> The part state.
     */
    public <P extends IPartType<P, S>, S extends IPartState<P>>  void update(P partType, PartTarget target, S state);

    /**
     * Use this comparator for any comparisons with aspects.
     */
    public static class AspectComparator implements Comparator<IAspect> {

        private static AspectComparator INSTANCE = null;

        private AspectComparator() {

        }

        public static AspectComparator getInstance() {
            if(INSTANCE == null) INSTANCE = new AspectComparator();
            return INSTANCE;
        }

        @Override
        public int compare(IAspect o1, IAspect o2) {
            int comp = IValueType.ValueTypeComparator.getInstance().compare(o1.getValueType(), o2.getValueType());
            if(comp == 0) {
                return o1.getUnlocalizedName().compareTo(o2.getUnlocalizedName());
            }
            return comp;
        }
    }

}
