package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import it.unimi.dsi.fastutil.ints.IntLists;
import org.cyclops.cyclopscore.helper.CyclopsCoreInstance;
import org.cyclops.integrateddynamics.ModBaseMocked;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueDeseralizationContextMocked;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyFactories;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests for completing methods and showing the call at the cursor in the text of expressions.
 * @author rubensworks
 */
public class TestExpressionCompletion {

    static { CyclopsCoreInstance.MOD = new ModBaseMocked(); }

    private static final Map<Integer, IValueType<?>> REF_TYPES = Map.of(5, ValueTypes.STRING);

    @Before
    public void before() {
        ValueTypeListProxyFactories.load();
    }

    private static ExpressionParser.Context context() {
        return new ExpressionParser.Context(ValueDeseralizationContextMocked.get(), REF_TYPES::get, label -> IntLists.EMPTY_LIST,
                type -> true, operator -> operator != Operators.STRING_ENDS_WITH);
    }

    // The cursor is at the end, or at the |
    private static ExpressionParser.Completion complete(String text) {
        int cursor = text.indexOf('|');
        if (cursor < 0) {
            return ExpressionParser.complete(text, text.length(), context());
        }
        return ExpressionParser.complete(text.substring(0, cursor) + text.substring(cursor + 1), cursor, context());
    }

    @Test
    public void testMethods() {
        SortedMap<String, IOperator> methods = OperatorNames.getMethods(ValueTypes.STRING);
        assertThat(methods.get("length"), is(Operators.STRING_LENGTH));
        assertThat(methods.get("contains"), is(Operators.STRING_CONTAINS));
        // Of the categories of the type
        assertThat(methods.get("name"), is(Operators.NAMED_NAME));
        assertThat(methods.get("isNull"), is(Operators.NULLABLE_ISNULL));
        // The receiver tells shared names apart
        assertThat(OperatorNames.getMethods(ValueTypes.LIST).get("length"), is(Operators.LIST_LENGTH));
        assertThat(OperatorNames.getMethods(ValueTypes.INTEGER).get("add"), is(Operators.ARITHMETIC_ADDITION));
        // Values of category types only have the methods of that category, which are certain to apply
        assertFalse(OperatorNames.getMethods(ValueTypes.CATEGORY_ANY).containsKey("length"));
        assertTrue(OperatorNames.getMethods(ValueTypes.CATEGORY_ANY).containsKey("isNull"));
        // Ordered by name, ignoring case
        List<String> names = new ArrayList<>(OperatorNames.getMethods(ValueTypes.OBJECT_ITEMSTACK).keySet());
        List<String> sorted = new ArrayList<>(names);
        sorted.sort(String.CASE_INSENSITIVE_ORDER);
        assertThat(names, is(sorted));
        // Each name is read back as its operator
        for (IValueType<?> type : ValueTypes.REGISTRY.getValueTypes()) {
            OperatorNames.getMethods(type).forEach((name, operator) ->
                    assertThat(type + "." + name, OperatorNames.resolve(name, type), is(operator)));
        }
    }

    private static void assertMethods(String text, IValueType<?> receiverType, int start, String prefix) {
        assertThat(text, complete(text).methods(), is(new ExpressionParser.Methods(receiverType, start, prefix)));
    }

    @Test
    public void testCompleteMethods() {
        assertMethods("(s: string) => s.", ValueTypes.STRING, 17, "");
        assertMethods("(s: string) => s.len", ValueTypes.STRING, 17, "len");
        assertMethods("(s: string) => s.len|gth() + 1", ValueTypes.STRING, 17, "len");
        // After calls, literals, references and brackets
        assertMethods("(item: itemstack) => item.size().", ValueTypes.INTEGER, 33, "");
        assertMethods("(s: string) => s.concat(s).l", ValueTypes.STRING, 27, "l");
        assertMethods("\"abc\".", ValueTypes.STRING, 6, "");
        assertMethods("#5.", ValueTypes.STRING, 3, "");
        assertMethods("(1 + 2).", ValueTypes.INTEGER, 8, "");
        assertMethods("[1, 2].", ValueTypes.LIST, 7, "");
        // Of the value right before the point
        assertMethods("(b: boolean, s: string) => b ? 1 : 2 > s.", ValueTypes.STRING, 41, "");
        // Not for the point of a decimal number, in strings, after a space, or after errors
        assertThat(complete("2 > 1.").methods(), nullValue());
        assertThat(complete("\"a.").methods(), nullValue());
        assertThat(complete("(s: string) => s.len ").methods(), nullValue());
        assertThat(complete("(s: string) => nope.").methods(), nullValue());
        assertThat(complete("(s: string) => s").methods(), nullValue());
    }

    private static void assertCall(String text, IOperator operator, String name, boolean method, int input) {
        assertThat(text, complete(text).call(), is(new ExpressionParser.Call(operator, name, method, input)));
    }

    @Test
    public void testCompleteCalls() {
        assertCall("(s: string) => s.contains(", Operators.STRING_CONTAINS, "contains", true, 1);
        assertCall("(s: string) => contains(s, ", Operators.STRING_CONTAINS, "contains", false, 1);
        assertCall("(s: string) => contains(s", Operators.STRING_CONTAINS, "contains", false, 0);
        assertCall("stringContains(", Operators.STRING_CONTAINS, "stringContains", false, 0);
        assertCall("(s: string) => s.contains(\"a\"|) && true", Operators.STRING_CONTAINS, "contains", true, 1);
        // The innermost call
        assertCall("(s: string) => s.contains(s.concat(", Operators.STRING_CONCAT, "concat", true, 1);
        assertCall("(s: string) => s.contains(s.concat(s), ", Operators.STRING_CONTAINS, "contains", true, 2);
        assertCall("(s: string) => s.contains(1 + 2 > 3 ? \"a\" : ", Operators.STRING_CONTAINS, "contains", true, 1);
        // Also while typing a method
        ExpressionParser.Completion completion = complete("(s: string) => s.contains(s.");
        assertThat(completion.methods(), is(new ExpressionParser.Methods(ValueTypes.STRING, 28, "")));
        assertThat(completion.call(), is(new ExpressionParser.Call(Operators.STRING_CONTAINS, "contains", true, 1)));
        // Not after the call, for shared names before the first argument, for parameters, unusable operators and after errors
        assertThat(complete("(s: string) => s.length()").call(), nullValue());
        assertThat(complete("(s: string) => s.length() + 1").call(), nullValue());
        assertThat(complete("contains(").call(), nullValue());
        assertThat(complete("(f: operator) => f(").call(), nullValue());
        assertThat(complete("(s: string) => s.endsWith(").call(), nullValue());
        assertThat(complete("(s: string) => s.contains(nope, ").call(), nullValue());
        assertThat(complete("(s: string) => s.contains(\"a").call(), nullValue());
        assertThat(ExpressionParser.complete("abc", 5, context()), is(ExpressionParser.Completion.NONE));
    }

}
