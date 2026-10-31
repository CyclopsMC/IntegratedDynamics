package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import it.unimi.dsi.fastutil.ints.IntLists;
import org.cyclops.cyclopscore.helper.CyclopsCoreInstance;
import org.cyclops.integrateddynamics.ModBaseMocked;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueDeseralizationContextMocked;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeAddonMocked;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyFactories;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionEditor;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
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
        // Each name is read back as its operator, with only the receiver known
        for (IValueType<?> type : ValueTypes.REGISTRY.getValueTypes()) {
            OperatorNames.getMethods(type).forEach((name, operator) -> {
                List<IValueType<?>> argumentTypes = new ArrayList<>(Collections.nCopies(operator.getRequiredInputLength(), null));
                argumentTypes.set(0, type);
                assertThat(type + "." + name, OperatorNames.resolve(name, argumentTypes), is(operator));
            });
        }
    }

    private static void assertMethods(String text, IValueType<?> receiverType, int start, String prefix) {
        assertThat(text, complete(text).methods(), is(new ExpressionParser.Methods(receiverType, start, prefix)));
    }

    @Test
    public void testCompleteMethods() {
        assertMethods("(s: String) => s.", ValueTypes.STRING, 17, "");
        assertMethods("(s: String) => s.len", ValueTypes.STRING, 17, "len");
        assertMethods("(s: String) => s.len|gth() + 1", ValueTypes.STRING, 17, "len");
        // After calls, literals, references and brackets
        assertMethods("(item: Item) => item.size().", ValueTypes.INTEGER, 28, "");
        assertMethods("(s: String) => s.concat(s).l", ValueTypes.STRING, 27, "l");
        assertMethods("\"abc\".", ValueTypes.STRING, 6, "");
        assertMethods("#5.", ValueTypes.STRING, 3, "");
        assertMethods("(1 + 2).", ValueTypes.INTEGER, 8, "");
        assertMethods("[1, 2].", ValueTypes.LIST, 7, "");
        // Of the value right before the point
        assertMethods("(b: Boolean, s: String) => b ? 1 : 2 > s.", ValueTypes.STRING, 41, "");
        // Not for the point of a decimal number, in strings, after a space, or after errors
        assertThat(complete("2 > 1.").methods(), nullValue());
        assertThat(complete("\"a.").methods(), nullValue());
        assertThat(complete("(s: String) => s.len ").methods(), nullValue());
        assertThat(complete("(s: String) => nope.").methods(), nullValue());
        assertThat(complete("(s: String) => s").methods(), nullValue());
    }

    private static void assertCall(String text, IOperator operator, String name, boolean method, int input) {
        assertThat(text, complete(text).call(), is(new ExpressionParser.Call(operator, name, method, input)));
    }

    @Test
    public void testCompleteCalls() {
        assertCall("(s: String) => s.contains(", Operators.STRING_CONTAINS, "contains", true, 1);
        assertCall("(s: String) => contains(s, ", Operators.STRING_CONTAINS, "contains", false, 1);
        assertCall("(s: String) => contains(s", Operators.STRING_CONTAINS, "contains", false, 0);
        assertCall("stringContains(", Operators.STRING_CONTAINS, "stringContains", false, 0);
        assertCall("(s: String) => s.contains(\"a\"|) && true", Operators.STRING_CONTAINS, "contains", true, 1);
        // The innermost call
        assertCall("(s: String) => s.contains(s.concat(", Operators.STRING_CONCAT, "concat", true, 1);
        assertCall("(s: String) => s.contains(s.concat(s), ", Operators.STRING_CONTAINS, "contains", true, 2);
        assertCall("(s: String) => s.contains(1 + 2 > 3 ? \"a\" : ", Operators.STRING_CONTAINS, "contains", true, 1);
        // Also while typing a method
        ExpressionParser.Completion completion = complete("(s: String) => s.contains(s.");
        assertThat(completion.methods(), is(new ExpressionParser.Methods(ValueTypes.STRING, 28, "")));
        assertThat(completion.call(), is(new ExpressionParser.Call(Operators.STRING_CONTAINS, "contains", true, 1)));
        // A shared name before the first argument is the first in the order of types
        assertCall("contains(", Operators.STRING_CONTAINS, "contains", false, 0);
        // Not after the call, for parameters, unusable operators and after errors
        assertThat(complete("(s: String) => s.length()").call(), nullValue());
        assertThat(complete("(s: String) => s.length() + 1").call(), nullValue());
        assertThat(complete("(f: Operator) => f(").call(), nullValue());
        assertThat(complete("(s: String) => s.endsWith(").call(), nullValue());
        assertThat(complete("(s: String) => s.contains(nope, ").call(), nullValue());
        assertThat(complete("(s: String) => s.contains(\"a").call(), nullValue());
        assertThat(ExpressionParser.complete("abc", 5, context()), is(ExpressionParser.Completion.NONE));
    }

    private static void assertTypes(String text, int start, String prefix, boolean parameter) {
        assertThat(text, complete(text).types(), is(new ExpressionParser.Types(start, prefix, parameter)));
    }

    @Test
    public void testCompleteTypes() {
        // Of parameters
        assertTypes("(x: ", 4, "", true);
        assertTypes("(x:", 3, "", true);
        assertTypes("(x: int", 4, "int", true);
        assertTypes("(x: in|teger) => x", 4, "in", true);
        assertTypes("(x: Integer, y, z: str", 19, "str", true);
        assertTypes("(x: testaddon:", 4, "testaddon:", true);
        assertTypes("(x: testaddon:chem", 4, "testaddon:chem", true);
        // Of values
        assertTypes("@", 1, "", false);
        assertTypes("x + @ite", 5, "ite", false);
        assertTypes("@testaddon:chem", 1, "testaddon:chem", false);
        // Not after a type, a space or the parameters, for names, in other brackets, or in strings
        assertThat(complete("(x: int ").types(), nullValue());
        assertThat(complete("(x: Integer)").types(), nullValue());
        assertThat(complete("(x: Integer) => x").types(), nullValue());
        assertThat(complete("(x, y").types(), nullValue());
        assertThat(complete("(x int").types(), nullValue());
        assertThat(complete("(b ? 1 : 2").types(), nullValue());
        assertThat(complete("\"a@it").types(), nullValue());
        assertThat(complete("#5 @").types(), is(new ExpressionParser.Types(4, "", false)));
    }

    @Test
    public void testTypes() {
        ExpressionParser.Context context = new ExpressionParser.Context(ValueDeseralizationContextMocked.get(), id -> null,
                label -> IntLists.EMPTY_LIST, ExpressionEditor::isParameterType, operator -> true);
        IValueType<?> chemical = ValueTypeAddonMocked.CHEMICAL;
        assertThat(ExpressionParser.getTypes(new ExpressionParser.Types(0, "int", true), context), is(List.of(ValueTypes.INTEGER)));
        assertThat(ExpressionParser.getTypes(new ExpressionParser.Types(0, "Int", true), context), is(List.of(ValueTypes.INTEGER)));
        assertThat(ExpressionParser.getTypes(new ExpressionParser.Types(0, "nu", true), context), is(List.of(ValueTypes.CATEGORY_NUMBER)));
        // Items and fluids by their name and their id
        assertThat(ExpressionParser.getTypes(new ExpressionParser.Types(0, "ite", true), context), is(List.of(ValueTypes.OBJECT_ITEMSTACK)));
        assertThat(ExpressionParser.getTypes(new ExpressionParser.Types(0, "itemst", true), context), is(List.of(ValueTypes.OBJECT_ITEMSTACK)));
        assertThat(ExpressionParser.getTypes(new ExpressionParser.Types(0, "flu", false), context), is(List.of(ValueTypes.OBJECT_FLUIDSTACK)));
        // Also by the name without the namespace of an add-on, which comes after those of Integrated Dynamics
        assertThat(ExpressionParser.getTypes(new ExpressionParser.Types(0, "chem", true), context), is(List.of(chemical)));
        assertThat(ExpressionParser.getTypes(new ExpressionParser.Types(0, "testaddon:c", true), context), is(List.of(chemical)));
        List<IValueType<?>> all = ExpressionParser.getTypes(new ExpressionParser.Types(0, "", true), context);
        assertThat(all.get(0), is(ValueTypes.CATEGORY_ANY));
        assertTrue(all.indexOf(chemical) > all.indexOf(ValueTypes.STRING));
        assertFalse(all.contains(ValueTypes.CATEGORY_NAMED));
        // Values have no categories
        List<IValueType<?>> values = ExpressionParser.getTypes(new ExpressionParser.Types(0, "", false), context);
        assertTrue(values.contains(ValueTypes.OBJECT_ITEMSTACK));
        assertFalse(values.contains(ValueTypes.CATEGORY_ANY));
        assertFalse(values.contains(ValueTypes.CATEGORY_NUMBER));
    }

}
