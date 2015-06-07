package org.cyclops.integrateddynamics.core.evaluate.variable;

import java.util.Comparator;

/**
 * Type of variable
 * @author rubensworks
 */
public interface IValueType<V extends IValue> {

    /**
     * Create an immutable default value.
     * @return The default value of this type.
     */
    public V getDefault();

    /**
     * @return The unique name of this type that will also be used for display.
     */
    public String getUnlocalizedName();

    /**
     * @param value The value
     * @return A short string representation used in guis to show the value.
     */
    public String toCompactString(V value);

    /**
     * @return The color that is used to identify this value type.
     */
    public int getDisplayColor();

    /**
     * Use this comparator for any comparisons with value types.
     */
    public static class ValueTypeComparator implements Comparator<IValueType> {

        private static ValueTypeComparator INSTANCE = null;

        private ValueTypeComparator() {

        }

        public static ValueTypeComparator getInstance() {
            if(INSTANCE == null) INSTANCE = new ValueTypeComparator();
            return INSTANCE;
        }

        @Override
        public int compare(IValueType o1, IValueType o2) {
            return o1.getUnlocalizedName().compareTo(o2.getUnlocalizedName());
        }
    }

}
