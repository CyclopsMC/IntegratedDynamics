package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import org.cyclops.cyclopscore.helper.CyclopsCoreInstance;
import org.cyclops.integrateddynamics.ModBaseMocked;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeCategory;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for the names of operators in printed expressions.
 * @author rubensworks
 */
public class TestOperatorNames {

    static { CyclopsCoreInstance.MOD = new ModBaseMocked(); }

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private static List<IValueType<?>> types(IValueType<?>... types) {
        return Arrays.asList(types);
    }

    @Test
    public void testScopedNamesByArgumentTypes() {
        assertThat(OperatorNames.getName(Operators.STRING_LENGTH, types(ValueTypes.STRING)), is("length"));
        assertThat(OperatorNames.getName(Operators.LIST_LENGTH, types(ValueTypes.LIST)), is("length"));
        // Without types to tell them apart, the operator that comes first in the order of types has the scoped name
        assertThat(OperatorNames.getName(Operators.STRING_LENGTH, types((IValueType<?>) null)), is("length"));
        assertThat(OperatorNames.getName(Operators.STRING_LENGTH, types(ValueTypes.CATEGORY_ANY)), is("length"));
        assertThat(OperatorNames.getName(Operators.LIST_LENGTH, types(ValueTypes.CATEGORY_ANY)), is("listLength"));
        assertThat(OperatorNames.getName(Operators.LIST_LENGTH, null), is("listLength"));
        // Unique scoped names need no type
        assertThat(OperatorNames.getName(Operators.OPERATOR_REDUCE, null), is("reduce"));
        // Even with a type error
        assertThat(OperatorNames.getName(Operators.OPERATOR_REDUCE, types(ValueTypes.INTEGER, null, null)), is("reduce"));
    }

    @Test
    public void testResolve() {
        assertThat(OperatorNames.resolve("length", types(ValueTypes.STRING)), is(Operators.STRING_LENGTH));
        assertThat(OperatorNames.resolve("length", types(ValueTypes.LIST)), is(Operators.LIST_LENGTH));
        assertThat(OperatorNames.resolve("stringLength", null), is(Operators.STRING_LENGTH));
        assertThat(OperatorNames.resolve("reduce", null), is(Operators.OPERATOR_REDUCE));
        assertThat(OperatorNames.resolve("doesNotExist", types(ValueTypes.STRING)), nullValue());
    }

    /**
     * Shared names and + are chosen between in the same way: by the number of arguments,
     * then by the most arguments whose type the inputs take, then by the order of types.
     */
    @Test
    public void testResolveSharedNamesAndSymbols() {
        IValueType<?> any = ValueTypes.CATEGORY_ANY;
        // Any argument can tell, not only the first
        assertThat(OperatorNames.resolve("concat", types(any, ValueTypes.STRING)), is(Operators.STRING_CONCAT));
        assertThat(OperatorNames.resolve("concat", types(any, ValueTypes.LIST)), is(Operators.LIST_CONCAT));
        assertThat(OperatorNames.getInfixOperator("+", any, ValueTypes.STRING), is(Operators.STRING_CONCAT));
        assertThat(OperatorNames.getInfixOperator("+", ValueTypes.STRING, any), is(Operators.STRING_CONCAT));
        assertThat(OperatorNames.getInfixOperator("+", any, ValueTypes.INTEGER), is(Operators.ARITHMETIC_ADDITION));
        assertThat(OperatorNames.resolve("contains", types(any, ValueTypes.INTEGER)), is(Operators.LIST_CONTAINS));
        // The number of arguments, as type(nbt, key) and type(entity) share a name
        assertThat(OperatorNames.resolve("type", types(any)), is(Operators.OBJECT_ENTITY_TYPE));
        assertThat(OperatorNames.resolve("type", types(any, any)), is(Operators.NBT_COMPOUND_VALUE_TYPE));
        // Without types to tell, numbers come before strings before lists, in the order of the type selector
        assertThat(OperatorNames.getInfixOperator("+", any, any), is(Operators.ARITHMETIC_ADDITION));
        assertThat(OperatorNames.getInfixOperator("+", null, null), is(Operators.ARITHMETIC_ADDITION));
        assertThat(OperatorNames.resolve("concat", types(any, any)), is(Operators.STRING_CONCAT));
        assertThat(OperatorNames.resolve("length", types(any)), is(Operators.STRING_LENGTH));
        assertThat(OperatorNames.resolve("length", null), is(Operators.STRING_LENGTH));
        assertThat(OperatorNames.resolve("size", types(any)), is(Operators.NBT_COMPOUND_SIZE));
        // Categories such as Number can hold the type of an input
        assertThat(OperatorNames.resolve("concat", types(ValueTypes.CATEGORY_NUMBER, any)), is(Operators.STRING_CONCAT));
        // An exact type fits better than a category, so one string makes + a concat
        assertThat(OperatorNames.getInfixOperator("+", ValueTypes.INTEGER, ValueTypes.STRING), is(Operators.STRING_CONCAT));
        assertThat(OperatorNames.getInfixOperator("+", ValueTypes.STRING, ValueTypes.INTEGER), is(Operators.STRING_CONCAT));
        // When no operator takes the types, the order still gives one, whose type error tells what does not fit
        assertThat(OperatorNames.resolve("length", types(ValueTypes.INTEGER)), is(Operators.STRING_LENGTH));
    }

    @Test
    public void testSymbols() {
        assertThat(OperatorNames.getInfixSymbol(Operators.ARITHMETIC_ADDITION), is("+"));
        assertThat(OperatorNames.getInfixSymbol(Operators.STRING_CONCAT), is("+"));
        assertThat(OperatorNames.getInfixOperator("+", ValueTypes.STRING, ValueTypes.CATEGORY_ANY), is(Operators.STRING_CONCAT));
        assertThat(OperatorNames.getInfixOperator("+", ValueTypes.INTEGER, ValueTypes.CATEGORY_ANY), is(Operators.ARITHMETIC_ADDITION));
        assertThat(OperatorNames.getPrefixSymbol(Operators.LOGICAL_NOT), is("!"));
        assertThat(OperatorNames.getValueName(Operators.ARITHMETIC_ADDITION), is("+"));
        // (+) is read as addition, and (concat) as concat of strings, which comes before lists
        assertThat(OperatorNames.getValueName(Operators.STRING_CONCAT), is("concat"));
        assertThat(OperatorNames.getValueName(Operators.LIST_CONCAT), is("listConcat"));
        // (length) is read as the first in the order of types
        assertThat(OperatorNames.getValueName(Operators.STRING_LENGTH), is("length"));
        assertThat(OperatorNames.getValueName(Operators.LIST_LENGTH), is("listLength"));
        assertThat(OperatorNames.getValueName(Operators.OPERATOR_REDUCE), is("reduce"));
    }

    /**
     * Every operator gets a name without special characters that leads back to that operator,
     * whether or not the type of its first argument is known, and as a value.
     */
    @Test
    public void testAllNamesResolveBack() {
        for (IOperator operator : Operators.REGISTRY.getOperators()) {
            for (IValueType<?> argumentType : getArgumentTypes(operator)) {
                List<IValueType<?>> argumentTypes = new ArrayList<>(Collections.nCopies(operator.getRequiredInputLength(), null));
                if (!argumentTypes.isEmpty()) {
                    argumentTypes.set(0, argumentType);
                }
                String name = OperatorNames.getName(operator, argumentTypes);
                String context = operator.getUniqueName() + " with " + (argumentType == null ? "unknown" : argumentType.getTypeName()) + " as " + name;
                assertThat(context, IDENTIFIER.matcher(name).matches(), is(true));
                assertThat(context, OperatorNames.resolve(name, argumentTypes), is(operator));
            }
            String valueName = OperatorNames.getName(operator, null);
            assertThat(operator.getUniqueName() + " as a value", OperatorNames.resolve(valueName, null), is(operator));
        }
    }

    private static List<IValueType<?>> getArgumentTypes(IOperator operator) {
        List<IValueType<?>> types = new ArrayList<>();
        types.add(null);
        IValueType<?> firstInputType = operator.getInputTypes()[0];
        types.add(firstInputType);
        if (firstInputType instanceof IValueTypeCategory<?>) {
            for (IValueType<?> valueType : ValueTypes.REGISTRY.getValueTypes()) {
                if (!(valueType instanceof IValueTypeCategory<?>) && firstInputType.correspondsTo(valueType)) {
                    types.add(valueType);
                }
            }
        }
        return types;
    }

}
