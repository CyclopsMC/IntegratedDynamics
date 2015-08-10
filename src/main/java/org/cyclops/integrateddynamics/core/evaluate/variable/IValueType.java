package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.helper.L10NHelpers;

import java.util.Comparator;
import java.util.List;

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
     * Add tooltip lines for this aspect when hovered in a gui.
     * @param lines The list to add lines to.
     * @param appendOptionalInfo If shift-to-show info should be added.
     */
    public void loadTooltip(List<String> lines, boolean appendOptionalInfo);

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
     * @return The color that is used to identify this value type using MC formatting codes.
     */
    public String getDisplayColorFormat();

    /**
     * @return If the given value type can be used with this value type.
     */
    public boolean correspondsTo(IValueType valueType);

    /**
     * Serialize the given value.
     * @param value The value to serialize.
     * @return The serialized value.
     */
    public String serialize(V value);

    /**
     * Check if the given value can be deserialized.
     * @param value The value to deserialize.
     * @return An error or null.
     */
    public L10NHelpers.UnlocalizedString canDeserialize(String value);

    /**
     * Deserialize the given value.
     * @param value The value to deserialize.
     * @return The deserialized value.
     */
    public V deserialize(String value);

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
