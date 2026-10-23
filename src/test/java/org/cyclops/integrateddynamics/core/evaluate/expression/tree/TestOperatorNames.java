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

    @Test
    public void testScopedNamesByFirstArgumentType() {
        assertThat(OperatorNames.getName(Operators.STRING_LENGTH, ValueTypes.STRING), is("length"));
        assertThat(OperatorNames.getName(Operators.LIST_LENGTH, ValueTypes.LIST), is("length"));
        // Without a type to tell them apart, shared names fall back to the global name
        assertThat(OperatorNames.getName(Operators.STRING_LENGTH, null), is("stringLength"));
        assertThat(OperatorNames.getName(Operators.STRING_LENGTH, ValueTypes.CATEGORY_ANY), is("stringLength"));
        // Unique scoped names need no type
        assertThat(OperatorNames.getName(Operators.OPERATOR_REDUCE, null), is("reduce"));
        // Even with a type error
        assertThat(OperatorNames.getName(Operators.OPERATOR_REDUCE, ValueTypes.INTEGER), is("reduce"));
    }

    @Test
    public void testResolve() {
        assertThat(OperatorNames.resolve("length", ValueTypes.STRING), is(Operators.STRING_LENGTH));
        assertThat(OperatorNames.resolve("length", ValueTypes.LIST), is(Operators.LIST_LENGTH));
        assertThat(OperatorNames.resolve("length", null), nullValue());
        assertThat(OperatorNames.resolve("stringLength", null), is(Operators.STRING_LENGTH));
        assertThat(OperatorNames.resolve("reduce", null), is(Operators.OPERATOR_REDUCE));
        assertThat(OperatorNames.resolve("doesNotExist", ValueTypes.STRING), nullValue());
    }

    @Test
    public void testSymbols() {
        assertThat(OperatorNames.getInfixSymbol(Operators.ARITHMETIC_ADDITION), is("+"));
        assertThat(OperatorNames.getInfixSymbol(Operators.STRING_CONCAT), is("+"));
        assertThat(OperatorNames.getInfixOperator("+", ValueTypes.STRING, ValueTypes.CATEGORY_ANY), is(Operators.STRING_CONCAT));
        assertThat(OperatorNames.getInfixOperator("+", ValueTypes.INTEGER, ValueTypes.CATEGORY_ANY), is(Operators.ARITHMETIC_ADDITION));
        assertThat(OperatorNames.getPrefixSymbol(Operators.LOGICAL_NOT), is("!"));
        assertThat(OperatorNames.getValueName(Operators.ARITHMETIC_ADDITION), is("+"));
        // (+) is read as addition
        assertThat(OperatorNames.getValueName(Operators.STRING_CONCAT), is("stringConcat"));
        assertThat(OperatorNames.getValueName(Operators.STRING_LENGTH), is("stringLength"));
        assertThat(OperatorNames.getValueName(Operators.OPERATOR_REDUCE), is("reduce"));
    }

    /**
     * Every operator gets a name without special characters that leads back to that operator,
     * whether or not the type of its first argument is known.
     */
    @Test
    public void testAllNamesResolveBack() {
        for (IOperator operator : Operators.REGISTRY.getOperators()) {
            for (IValueType<?> argumentType : getArgumentTypes(operator)) {
                String name = OperatorNames.getName(operator, argumentType);
                String context = operator.getUniqueName() + " with " + (argumentType == null ? "unknown" : argumentType.getTypeName()) + " as " + name;
                assertThat(context, IDENTIFIER.matcher(name).matches(), is(true));
                assertThat(context, OperatorNames.resolve(name, argumentType), is(operator));
            }
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
