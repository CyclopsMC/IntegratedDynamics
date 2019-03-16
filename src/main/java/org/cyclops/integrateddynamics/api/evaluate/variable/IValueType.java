package org.cyclops.integrateddynamics.api.evaluate.variable;

import com.google.gson.JsonObject;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.advancement.criterion.ValuePredicate;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.logicprogrammer.IValueTypeLogicProgrammerElement;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/**
 * Type of value
 * @author rubensworks
 */
public interface IValueType<V extends IValue> {

    /**
     * @return If this type is a category and should be handled accordingly.
     */
    public boolean isCategory();

    /**
     * @return If this type is an object type, otherwise it is a raw type.
     */
    public boolean isObject();

    /**
     * Create an immutable default value.
     * @return The default value of this type.
     */
    public V getDefault();

    /**
     * @return The name of this type without any prefixes.
     */
    public String getTypeName();

    /**
     * @return The unique name of this type that will also be used for display.
     */
    public String getTranslationKey();

    /**
     * Add tooltip lines for this aspect when hovered in a gui.
     * @param lines The list to add lines to.
     * @param appendOptionalInfo If shift-to-show info should be added.
     * @param value The value to show the tooltip for.
     */
    public void loadTooltip(List<String> lines, boolean appendOptionalInfo, @Nullable V value);

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
     * Check if the given type corresponds with this type.
     * To check bidirectional, use {@link org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers#correspondsTo(IValueType, IValueType)}.
     * @param valueType The value type to check correspondence with.
     * @return If the given value type can be used with this value type.
     */
    public boolean correspondsTo(IValueType<?> valueType);

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
     * Materialize the given value so that it can exist without any external references.
     * @param value The value to materialize.
     * @return The materialized value.
     * @throws EvaluationException if materialization fails because of a variable evaluation.
     */
    public V materialize(V value) throws EvaluationException;

    /**
     * @return A new logic programmer element for this value type.
     */
    public IValueTypeLogicProgrammerElement createLogicProgrammerElement();

    /**
     * Deserialize the given JSON element to a value predicate.
     * @param element The JSON element.
     * @param value The value.
     * @return The value predicate.
     */
    default public ValuePredicate<V> deserializeValuePredicate(JsonObject element, @Nullable IValue value) {
        return new ValuePredicate<>(this, value);
    }

    /**
     * Use this comparator for any comparisons with value types.
     */
    public static class ValueTypeComparator implements Comparator<IValueType<?>> {

        private static ValueTypeComparator INSTANCE = null;

        private ValueTypeComparator() {

        }

        public static ValueTypeComparator getInstance() {
            if(INSTANCE == null) INSTANCE = new ValueTypeComparator();
            return INSTANCE;
        }

        @Override
        public int compare(IValueType<?> o1, IValueType<?> o2) {
            return o1.getTranslationKey().compareTo(o2.getTranslationKey());
        }
    }

}
