package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.cyclops.cyclopscore.helper.CyclopsCoreInstance;
import org.cyclops.integrateddynamics.ModBaseMocked;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionParameter;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeCategory;
import org.cyclops.integrateddynamics.core.evaluate.operator.CurriedOperator;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueDeseralizationContextMocked;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeDouble;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeLong;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.evaluate.variable.Variable;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionEditor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for parsing the text form of expressions.
 * @author rubensworks
 */
public class TestExpressionParser {

    static { CyclopsCoreInstance.MOD = new ModBaseMocked(); }

    private static final Map<Integer, String> LABELS = Map.of(1, "limit", 2, "My Card", 3, "twice", 4, "twice");
    private static final Map<Integer, IValueType<?>> REF_TYPES = Map.of(1, ValueTypes.INTEGER, 5, ValueTypes.STRING);

    private static ExpressionParser.Context context() {
        return new ExpressionParser.Context(ValueDeseralizationContextMocked.get(), REF_TYPES::get, TestExpressionParser::labelIds,
                type -> true, operator -> true);
    }

    private static IntList labelIds(String label) {
        IntList ids = IntList.of(LABELS.entrySet().stream().filter(e -> e.getValue().equals(label)).mapToInt(Map.Entry::getKey).sorted().toArray());
        return ids.isEmpty() ? IntLists.EMPTY_LIST : ids;
    }

    private static Expression parse(String text) throws ExpressionParseException {
        return ExpressionParser.parse(text, context());
    }

    private static String printExact(Expression expression) {
        return ExpressionPrinter.printExact(expression, ValueDeseralizationContextMocked.get(), LABELS::get,
                TestExpressionParser::labelIds, REF_TYPES::get);
    }

    private static void assertRoundTrip(Expression expression) throws ExpressionParseException {
        String text = printExact(expression);
        Expression parsed;
        try {
            parsed = parse(text);
        } catch (ExpressionParseException e) {
            throw new AssertionError("Could not parse " + text + " at " + e.getPosition() + ": " + e.getMessage(), e);
        }
        assertThat(text, parsed, is(expression));
        assertThat(printExact(parsed), is(text));
    }

    private static void assertError(String text, String key, int position) {
        try {
            parse(text);
            fail("Expected an error for " + text);
        } catch (ExpressionParseException e) {
            assertThat(text, ((TranslatableContents) e.getComponent().getContents()).getKey(), is(key));
            assertThat(text, e.getPosition(), is(position));
        }
    }

    private static ExpressionNode op(IOperator operator, ExpressionNode... children) {
        return new ExpressionNode.Operator(operator, List.of(children));
    }

    private static ExpressionNode c(IValue value) {
        return new ExpressionNode.Const(value);
    }

    private static ExpressionNode i(int value) {
        return c(ValueTypeInteger.ValueInteger.of(value));
    }

    private static ExpressionNode p(int index) {
        return new ExpressionNode.Param(index);
    }

    private static ExpressionParameter param(String name, IValueType<?> type) {
        return new ExpressionParameter(name, type);
    }

    // --------------- Round trips ---------------

    @Test
    public void testRoundTripExpressions() throws ExpressionParseException {
        assertRoundTrip(new Expression(op(Operators.RELATIONAL_GT, op(Operators.STRING_LENGTH, p(0)), i(3)),
                List.of(param("s", ValueTypes.STRING))));
        assertRoundTrip(new Expression(op(Operators.GENERAL_CHOICE,
                op(Operators.RELATIONAL_GT, p(0), p(1)),
                op(Operators.ARITHMETIC_MULTIPLICATION, p(0), p(2)),
                op(Operators.GENERAL_CHOICE, op(Operators.RELATIONAL_LT, p(1), p(2)), p(1), p(2))),
                List.of(param("a", ValueTypes.INTEGER), param("b", ValueTypes.INTEGER), param("c", ValueTypes.CATEGORY_NUMBER))));
        assertRoundTrip(new Expression(op(Operators.OPERATOR_REDUCE, c(ValueTypeOperator.ValueOperator.of(Operators.ARITHMETIC_ADDITION)),
                op(Operators.OPERATOR_MAP, c(ValueTypeOperator.ValueOperator.of(Operators.STRING_LENGTH)), p(0)), i(0)),
                List.of(param("l", ValueTypes.LIST))));
        assertRoundTrip(new Expression(op(Operators.LOGICAL_NOT, op(Operators.LOGICAL_AND, p(0), op(Operators.LOGICAL_NOT, p(0)))),
                List.of(param("x", ValueTypes.CATEGORY_ANY))));
        // Empty slots and incomplete choices
        assertRoundTrip(new Expression(op(Operators.GENERAL_CHOICE, op(Operators.RELATIONAL_GT, ExpressionNode.EMPTY, i(1)),
                ExpressionNode.EMPTY, i(2))));
        // Left and right nesting of the same infix operator
        assertRoundTrip(new Expression(op(Operators.ARITHMETIC_SUBTRACTION, op(Operators.ARITHMETIC_SUBTRACTION, i(1), i(2)),
                op(Operators.ARITHMETIC_SUBTRACTION, i(3), i(-4)))));
    }

    @Test
    public void testRoundTripValues() throws ExpressionParseException {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", "a \"quoted\" name");
        tag.putInt("count", 3);
        List<IValue> values = List.of(
                ValueTypeBoolean.ValueBoolean.of(true),
                ValueTypeInteger.ValueInteger.of(Integer.MIN_VALUE),
                ValueTypeLong.ValueLong.of(Long.MAX_VALUE),
                ValueTypeLong.ValueLong.of(-5),
                ValueTypeDouble.ValueDouble.of(1.5),
                ValueTypeDouble.ValueDouble.of(-2.5E-10),
                ValueTypeDouble.ValueDouble.of(Double.NEGATIVE_INFINITY),
                ValueTypeString.ValueString.of(""),
                ValueTypeString.ValueString.of("line\nbreak, \"quotes\", back\\slash, tab\t and \u0001"),
                ValueTypeNbt.ValueNbt.of(tag),
                ValueTypeOperator.ValueOperator.of(Operators.LOGICAL_NOT),
                ValueTypeOperator.ValueOperator.of(new CurriedOperator(Operators.ARITHMETIC_ADDITION,
                        new Variable<>(ValueTypeInteger.ValueInteger.of(1)))));
        for (IValue value : values) {
            assertRoundTrip(new Expression(op(Operators.RELATIONAL_EQUALS, c(value), ExpressionNode.EMPTY)));
        }
    }

    @Test
    public void testRoundTripRefs() throws ExpressionParseException {
        // A unique label, a label with a space, an ambiguous label and no label
        Expression expression = new Expression(op(Operators.GENERAL_CHOICE,
                op(Operators.RELATIONAL_EQUALS, new ExpressionNode.Ref(1), new ExpressionNode.Ref(2)),
                new ExpressionNode.Ref(3), new ExpressionNode.Ref(9)));
        assertThat(printExact(expression), is("#limit == #\"My Card\" ? #3 : #9"));
        assertRoundTrip(expression);
        // The type of a reference tells shared names apart
        Expression typed = new Expression(op(Operators.STRING_LENGTH, new ExpressionNode.Ref(5)));
        assertThat(printExact(typed), is("#5.length()"));
        assertRoundTrip(typed);
    }

    /**
     * Every operator that can be edited reads back from its text form, with and without a typed first argument.
     */
    @Test
    public void testRoundTripAllOperators() throws ExpressionParseException {
        int checked = 0;
        for (IOperator operator : Operators.REGISTRY.getOperators()) {
            if (!ExpressionEditor.isEditable(operator)) {
                continue;
            }
            checked++;
            assertRoundTrip(new Expression(ExpressionNode.Operator.ofEmpty(operator)));
            IValueType<?> firstInputType = operator.getInputTypes()[0];
            List<ExpressionNode> children = new ArrayList<>(ExpressionNode.Operator.ofEmpty(operator).children());
            children.set(0, p(0));
            if (firstInputType instanceof IValueTypeCategory<?>) {
                continue;
            }
            assertRoundTrip(new Expression(new ExpressionNode.Operator(operator, children), List.of(param("x", firstInputType))));
        }
        assertTrue("Only " + checked + " operators were checked", checked > 250);
    }

    @Test
    public void testDisplayFormParses() throws ExpressionParseException {
        Expression expression = new Expression(op(Operators.OPERATOR_REDUCE, c(ValueTypeOperator.ValueOperator.of(Operators.ARITHMETIC_ADDITION)),
                op(Operators.OPERATOR_MAP, c(ValueTypeOperator.ValueOperator.of(Operators.STRING_LENGTH)), p(0)), i(0)),
                List.of(param("l", ValueTypes.CATEGORY_ANY)));
        assertThat(parse(ExpressionPrinter.print(expression)), is(expression));
    }

    // --------------- Syntax ---------------

    @Test
    public void testPrecedence() throws ExpressionParseException {
        assertThat(parse("1 + 2 * 3 == 7").root(), is(op(Operators.RELATIONAL_EQUALS,
                op(Operators.ARITHMETIC_ADDITION, i(1), op(Operators.ARITHMETIC_MULTIPLICATION, i(2), i(3))), i(7))));
        assertThat(parse("(1 + 2) * 3").root(), is(op(Operators.ARITHMETIC_MULTIPLICATION, op(Operators.ARITHMETIC_ADDITION, i(1), i(2)), i(3))));
        assertThat(parse("10 - 2 - 3").root(), is(op(Operators.ARITHMETIC_SUBTRACTION, op(Operators.ARITHMETIC_SUBTRACTION, i(10), i(2)), i(3))));
        assertThat(parse("x => !x && x || x").root(), is(op(Operators.LOGICAL_OR,
                op(Operators.LOGICAL_AND, op(Operators.LOGICAL_NOT, p(0)), p(0)), p(0))));
        // Choices are right associative and bind weakest
        assertThat(parse("x => x ? 1 : x ? 2 : 3 + 1").root(), is(op(Operators.GENERAL_CHOICE, p(0), i(1),
                op(Operators.GENERAL_CHOICE, p(0), i(2), op(Operators.ARITHMETIC_ADDITION, i(3), i(1))))));
    }

    @Test
    public void testParameters() throws ExpressionParseException {
        assertThat(parse("x => x + 1").parameters(), is(List.of(param("x", ValueTypes.CATEGORY_ANY))));
        assertThat(parse("(x) => x + 1").parameters(), is(List.of(param("x", ValueTypes.CATEGORY_ANY))));
        assertThat(parse("(item: item, n: number) => n + 1").parameters(),
                is(List.of(param("item", ValueTypes.OBJECT_ITEMSTACK), param("n", ValueTypes.CATEGORY_NUMBER))));
        // A parenthesized parameter is not an operator value
        assertThat(parse("(length) => (length) + 1").root(), is(op(Operators.ARITHMETIC_ADDITION, p(0), i(1))));
    }

    @Test
    public void testNamesTellApartByType() throws ExpressionParseException {
        assertThat(parse("(s: string) => length(s)").root(), is(op(Operators.STRING_LENGTH, p(0))));
        assertThat(parse("(l: list) => length(l)").root(), is(op(Operators.LIST_LENGTH, p(0))));
        assertThat(parse("x => listLength(x)").root(), is(op(Operators.LIST_LENGTH, p(0))));
        assertThat(parse("length(\"abc\")").root(), is(op(Operators.STRING_LENGTH, c(ValueTypeString.ValueString.of("abc")))));
    }

    @Test
    public void testLiterals() throws ExpressionParseException {
        assertThat(parse("5 == 5L").root(), is(op(Operators.RELATIONAL_EQUALS, i(5), c(ValueTypeLong.ValueLong.of(5)))));
        assertThat(parse("1.5 == 2d").root(), is(op(Operators.RELATIONAL_EQUALS,
                c(ValueTypeDouble.ValueDouble.of(1.5)), c(ValueTypeDouble.ValueDouble.of(2)))));
        assertThat(parse("1e3 == -0.5").root(), is(op(Operators.RELATIONAL_EQUALS,
                c(ValueTypeDouble.ValueDouble.of(1000)), c(ValueTypeDouble.ValueDouble.of(-0.5)))));
        assertThat(parse("true != false").root(), is(op(Operators.RELATIONAL_NOTEQUALS,
                c(ValueTypeBoolean.ValueBoolean.of(true)), c(ValueTypeBoolean.ValueBoolean.of(false)))));
        assertThat(parse("\"\\u0041\\\\\" == ?").root(), is(op(Operators.RELATIONAL_EQUALS,
                c(ValueTypeString.ValueString.of("A\\")), ExpressionNode.EMPTY)));
        Expression nbt = parse("@nbt {a: 1b} == ?");
        assertTrue(((ExpressionNode.Operator) nbt.root()).children().get(0) instanceof ExpressionNode.Const constant
                && constant.value() instanceof ValueTypeNbt.ValueNbt);
    }

    // --------------- Integrated Scripting syntax ---------------

    @Test
    public void testArrows() throws ExpressionParseException {
        Expression expression = parse("(x: integer) => x * 2");
        assertThat(parse("(x: integer) -> x * 2"), is(expression));
        assertThat(parse("x -> x + 1"), is(parse("x => x + 1")));
        assertThat(printExact(expression), is("(x: integer) => x * 2"));
    }

    @Test
    public void testMethodCalls() throws ExpressionParseException {
        assertThat(parse("(s: string) => s.length()").root(), is(op(Operators.STRING_LENGTH, p(0))));
        // The receiver tells shared names apart
        assertThat(parse("(l: list) => l.length()").root(), is(op(Operators.LIST_LENGTH, p(0))));
        // Arguments follow the receiver, calls chain, and they bind tighter than ! and infix operators
        assertThat(parse("(s: string, t: string) => s.concat(t).length() > 2"),
                is(parse("(s: string, t: string) => length(concat(s, t)) > 2")));
        assertThat(parse("(s: string) => !s.contains(\"a\")"), is(parse("(s: string) => !contains(s, \"a\")")));
        // On literals, references, parenthesized values and global names
        assertThat(parse("\"abc\".length() + #5.length() + (#5).stringLength()"),
                is(parse("length(\"abc\") + length(#5) + length(#5)")));
        // Calls with a scoped name are printed as methods
        assertThat(printExact(parse("(s: string) => length(s)")), is("(s: string) => s.length()"));
        // Examples of the Integrated Scripting book
        assertThat(parse("(item: item) => item.isStackable() && item.size() >= 16"),
                is(parse("(item: item) => isStackable(item) && size(item) >= 16")));
        assertThat(parse("(item: item, block: block) => item.strength(block) > 10"),
                is(parse("(item: item, block: block) => strength(item, block) > 10")));
    }

    @Test
    public void testTypeNames() throws ExpressionParseException {
        // Items and fluids are called as in the game, and their ids are read too
        assertThat(ExpressionParser.getTypeName(ValueTypes.OBJECT_ITEMSTACK), is("item"));
        assertThat(ExpressionParser.getTypeName(ValueTypes.OBJECT_FLUIDSTACK), is("fluid"));
        assertThat(ExpressionParser.getTypeName(ValueTypes.INTEGER), is("integer"));
        for (String name : List.of("item", "itemstack", "integrateddynamics:itemstack")) {
            assertThat(name, ExpressionParser.getValueType(name), is(ValueTypes.OBJECT_ITEMSTACK));
        }
        for (String name : List.of("fluid", "fluidstack", "integrateddynamics:fluidstack")) {
            assertThat(name, ExpressionParser.getValueType(name), is(ValueTypes.OBJECT_FLUIDSTACK));
        }
        assertThat(ExpressionParser.getValueType("integrateddynamics:item"), is((IValueType<?>) null));
        assertPrinted("(i: itemstack, f: fluidstack) => i.size() > f.amount()", "(i: item, f: fluid) => i.size() > f.amount()");
    }

    // Prints as the given text, which reads back into the same expression
    private static void assertPrinted(String text, String printed) throws ExpressionParseException {
        Expression expression = parse(text);
        assertThat(text, printExact(expression), is(printed));
        assertThat(printed, parse(printed), is(expression));
    }

    @Test
    public void testPrintMethods() throws ExpressionParseException {
        assertPrinted("(item: item) => isStackable(item) && size(item) >= 16",
                "(item: item) => item.isStackable() && (item.size() >= 16)");
        assertPrinted("(item: item) => getInteger(nbt(item), \"k\")", "(item: item) => item.nbt().getInteger(\"k\")");
        // On literals, and on symbols in brackets
        assertPrinted("length(\"abc\") + length(#5)", "\"abc\".length() + #5.length()");
        assertPrinted("(s: string, t: string) => length(s + t) > 2", "(s: string, t: string) => (s + t).length() > 2");
        assertPrinted("(b: boolean, c: boolean) => nand(!b, c)", "(b: boolean, c: boolean) => (!b).nand(c)");
        assertPrinted("(s: string) => !contains(s, \"a\")", "(s: string) => !s.contains(\"a\")");
        // Calls with a global name, on operators, on empty slots and on numbers, and choices stay calls
        assertPrinted("x => listLength(x)", "x => listLength(x)");
        assertPrinted("(l: list) => map((listLength), l)", "(l: list) => map((listLength), l)");
        assertPrinted("(f: operator) => isNull(f)", "(f: operator) => isNull(f)");
        assertPrinted("isStackable(?)", "isStackable(?)");
        assertPrinted("max(1, 2)", "max(1, 2)");
        assertPrinted("(n: integer) => max(n, 2)", "(n: integer) => n.max(2)");
        assertPrinted("(b: boolean) => choice(b, 1, ?)", "(b: boolean) => choice(b, 1, ?)");
    }

    @Test
    public void testMethodCallErrors() {
        assertError("(s: string) => s.length", L10NValues.EXPRESSION_PARSE_METHOD, 16);
        assertError("(s: string) => s.", L10NValues.EXPRESSION_PARSE_METHOD, 16);
        assertError("(s: string) => s.(1)", L10NValues.EXPRESSION_PARSE_METHOD, 16);
        assertError("(s: string) => s.lenght()", L10NValues.EXPRESSION_PARSE_UNKNOWN_OPERATOR, 17);
        assertError("(s: string) => s.length(1)", L10NValues.EXPRESSION_PARSE_INPUT_COUNT, 17);
    }

    @Test
    public void testParameterCalls() throws ExpressionParseException {
        assertThat(parse("(f: operator, x: integer) => f(x)").root(), is(op(Operators.OPERATOR_APPLY, p(0), p(1))));
        assertThat(parse("(f: operator) => f()").root(), is(op(Operators.OPERATOR_APPLY_0, p(0))));
        assertThat(parse("(f: operator) => f(1, 2)").root(), is(op(Operators.OPERATOR_APPLY_2, p(0), i(1), i(2))));
        assertThat(parse("(f: operator) => f(1, 2, 3)").root(), is(op(Operators.OPERATOR_APPLY_3, p(0), i(1), i(2), i(3))));
        assertThat(parse("f => f.apply(1)"), is(parse("f => f(1)")));
        // Printed with apply
        assertThat(printExact(parse("f => f(1)")), is("f => apply(f, 1)"));
        assertError("f => f(1, 2, 3, 4)", L10NValues.EXPRESSION_PARSE_PARAMETER_CALL, 5);
    }

    @Test
    public void testStrictEquality() throws ExpressionParseException {
        assertThat(parse("1 === 2"), is(parse("1 == 2")));
        assertThat(parse("1 !== 2"), is(parse("1 != 2")));
        assertThat(parse("(value: integer) => value % 2 !== 0"), is(parse("(value: integer) => value % 2 != 0")));
        assertThat(parse("reduce((===), ?, ?)"), is(parse("reduce((==), ?, ?)")));
        assertThat(printExact(parse("1 === 2")), is("1 == 2"));
    }

    @Test
    public void testSingleQuotes() throws ExpressionParseException {
        assertThat(parse("'it\\'s \"quoted\"' == ?").root(), is(op(Operators.RELATIONAL_EQUALS,
                c(ValueTypeString.ValueString.of("it's \"quoted\"")), ExpressionNode.EMPTY)));
        assertThat(parse("\"it's\" == ?").root(), is(op(Operators.RELATIONAL_EQUALS,
                c(ValueTypeString.ValueString.of("it's")), ExpressionNode.EMPTY)));
        // Curly ones, as text editors write them
        assertThat(parse("\u2018a\u2019 == ?"), is(parse("'a' == ?")));
        assertThat(parse("#'My Card' == 1").root(), is(op(Operators.RELATIONAL_EQUALS, new ExpressionNode.Ref(2), i(1))));
        assertThat(printExact(parse("'a' == ?")), is("\"a\" == ?"));
        assertError("'abc\" == ?", L10NValues.EXPRESSION_PARSE_UNTERMINATED_STRING, 0);
    }

    @Test
    public void testStringConcat() throws ExpressionParseException {
        ExpressionNode a = c(ValueTypeString.ValueString.of("a"));
        ExpressionNode b = c(ValueTypeString.ValueString.of("b"));
        assertThat(parse("\"a\" + \"b\"").root(), is(op(Operators.STRING_CONCAT, a, b)));
        assertThat(parse("(s: string) => s + \"a\" + s").root(), is(op(Operators.STRING_CONCAT, op(Operators.STRING_CONCAT, p(0), a), p(0))));
        assertThat(parse("(s: string) => s.length() + 1").root(), is(op(Operators.ARITHMETIC_ADDITION, op(Operators.STRING_LENGTH, p(0)), i(1))));
        // One string is enough, where the other input then has the wrong type
        assertThat(parse("(s: string) => 1 + s").root(), is(op(Operators.STRING_CONCAT, i(1), p(0))));
        // Unknown types stay addition
        assertThat(parse("x => x + #9").root(), is(op(Operators.ARITHMETIC_ADDITION, p(0), new ExpressionNode.Ref(9))));
        assertThat(parse("reduce((+), ?, ?)").root(), is(op(Operators.OPERATOR_REDUCE,
                c(ValueTypeOperator.ValueOperator.of(Operators.ARITHMETIC_ADDITION)), ExpressionNode.EMPTY, ExpressionNode.EMPTY)));
    }

    @Test
    public void testRoundTripStringConcat() throws ExpressionParseException {
        Expression concat = new Expression(op(Operators.STRING_CONCAT, p(0), c(ValueTypeString.ValueString.of("!"))),
                List.of(param("s", ValueTypes.STRING)));
        assertThat(printExact(concat), is("(s: string) => s + \"!\""));
        assertRoundTrip(concat);
        // Not with + when it would be read as addition, or the other way around
        assertRoundTrip(new Expression(op(Operators.STRING_CONCAT, p(0), p(1)),
                List.of(param("a", ValueTypes.CATEGORY_ANY), param("b", ValueTypes.CATEGORY_ANY))));
        assertRoundTrip(new Expression(op(Operators.ARITHMETIC_ADDITION, p(0), i(1)), List.of(param("s", ValueTypes.STRING))));
        assertRoundTrip(new Expression(op(Operators.OPERATOR_REDUCE, c(ValueTypeOperator.ValueOperator.of(Operators.STRING_CONCAT)),
                p(0), c(ValueTypeString.ValueString.of(""))), List.of(param("l", ValueTypes.LIST))));
    }

    @Test(timeout = 10000)
    public void testLongChains() {
        // Types are not inferred again for each operator of a chain
        assertError("1" + " + 1".repeat(2000), L10NValues.EXPRESSION_PARSE_TOO_DEEP, 0);
        assertError("(s: string) => s" + ".concat(s)".repeat(400), L10NValues.EXPRESSION_PARSE_TOO_DEEP, 16 + 10 * 127 + 7);
    }

    // --------------- Shared names ---------------

    @Test
    public void testSharedNames() throws ExpressionParseException {
        // The types of all arguments decide, also for symbols
        assertThat(parse("x => concat(x, \"whatever\")").root(), is(op(Operators.STRING_CONCAT, p(0), c(ValueTypeString.ValueString.of("whatever")))));
        assertThat(parse("x => x + \"a\"").root(), is(op(Operators.STRING_CONCAT, p(0), c(ValueTypeString.ValueString.of("a")))));
        assertThat(parse("(x: list) => x.length()").root(), is(op(Operators.LIST_LENGTH, p(0))));
        // Otherwise, the order of types: numbers before strings before lists
        assertThat(parse("x => x + x").root(), is(op(Operators.ARITHMETIC_ADDITION, p(0), p(0))));
        assertThat(parse("(x, y) => concat(x, y)").root(), is(op(Operators.STRING_CONCAT, p(0), p(1))));
        assertThat(parse("x => length(x)").root(), is(op(Operators.STRING_LENGTH, p(0))));
        assertThat(parse("x => (length) == x").root(), is(op(Operators.RELATIONAL_EQUALS,
                c(ValueTypeOperator.ValueOperator.of(Operators.STRING_LENGTH)), p(0))));
        // What is not chosen that way is printed with its global name, which reads back the same
        assertRoundTrip(new Expression(op(Operators.LIST_LENGTH, p(0)), List.of(param("x", ValueTypes.CATEGORY_ANY))));
        assertThat(printExact(new Expression(op(Operators.LIST_LENGTH, p(0)), List.of(param("x", ValueTypes.CATEGORY_ANY)))),
                is("x => listLength(x)"));
        assertThat(printExact(new Expression(op(Operators.STRING_LENGTH, p(0)), List.of(param("x", ValueTypes.CATEGORY_ANY)))),
                is("x => x.length()"));
        assertRoundTrip(new Expression(op(Operators.LIST_CONCAT, p(0), p(1)),
                List.of(param("x", ValueTypes.CATEGORY_ANY), param("y", ValueTypes.CATEGORY_ANY))));
    }

    // --------------- Errors ---------------

    private static void assertErrorRange(String text, int position, int end) {
        try {
            parse(text);
            fail("Expected an error for " + text);
        } catch (ExpressionParseException e) {
            assertThat(text, e.getPosition(), is(position));
            assertThat(text, e.getEnd(), is(end));
        }
    }

    @Test
    public void testErrorRanges() {
        // The whole name or type that is not known, so all of it can be marked
        assertErrorRange("x => lenght(x)", 5, 11);
        assertErrorRange("(s: string) => s.lenght()", 17, 23);
        assertErrorRange("y + 1", 0, 1);
        assertErrorRange("(x: thing) => x + 1", 4, 9);
        assertErrorRange("(x: mod:thing) => x + 1", 4, 13);
        assertErrorRange("@thing{} == 1", 0, 6);
        assertErrorRange("#nothing + 1", 0, 8);
        // A single symbol, and the end of the text
        assertErrorRange("(x: item) => x.", 14, 15);
        assertErrorRange("1 + ", 4, 4);
    }

    @Test
    public void testErrors() {
        assertError("x => lenght(x)", L10NValues.EXPRESSION_PARSE_UNKNOWN_OPERATOR, 5);
        assertError("length(\"a\", \"b\")", L10NValues.EXPRESSION_PARSE_INPUT_COUNT, 0);
        assertError("length(\"abc)", L10NValues.EXPRESSION_PARSE_UNTERMINATED_STRING, 7);
        assertError("1 + 2 3", L10NValues.EXPRESSION_PARSE_TRAILING, 6);
        assertError("y + 1", L10NValues.EXPRESSION_PARSE_UNKNOWN_NAME, 0);
        assertError("3000000000 + 1", L10NValues.EXPRESSION_PARSE_INTEGER_TOO_LARGE, 0);
        assertError("x => x", L10NValues.EXPRESSION_PARSE_ROOT, 5);
        assertError("(x, x) => x + x", L10NValues.EXPRESSION_PARSE_DUPLICATE_PARAMETER, 4);
        assertError("true => 1 + 1", L10NValues.EXPRESSION_PARSE_PARAMETER_NAME, 0);
        assertError("(x: thing) => x + 1", L10NValues.EXPRESSION_PARSE_UNKNOWN_TYPE, 4);
        assertError("@thing{} == 1", L10NValues.EXPRESSION_PARSE_UNKNOWN_TYPE, 0);
        assertError("@nbt {a: } == 1", L10NValues.EXPRESSION_PARSE_BAD_VALUE, 9);
        assertError("1 + ", L10NValues.EXPRESSION_PARSE_EXPECTED_VALUE_END, 4);
        assertError("(1 + 2", L10NValues.EXPRESSION_PARSE_UNEXPECTED_END, 6);
        assertError("1 + $", L10NValues.EXPRESSION_PARSE_UNKNOWN_CHARACTER, 4);
        assertError("#nothing + 1", L10NValues.EXPRESSION_PARSE_UNKNOWN_LABEL, 0);
        assertError("#twice + 1", L10NValues.EXPRESSION_PARSE_AMBIGUOUS_LABEL, 0);
        assertError("x => -x", L10NValues.EXPRESSION_PARSE_EXPECTED_VALUE, 5);
    }

    @Test
    public void testOperatorsNotAllowed() {
        ExpressionParser.Context context = new ExpressionParser.Context(ValueDeseralizationContextMocked.get(), id -> null,
                label -> IntLists.EMPTY_LIST, type -> type != ValueTypes.OBJECT_BLOCK, operator -> operator != Operators.ARITHMETIC_ADDITION);
        try {
            ExpressionParser.parse("1 * (2 + 3)", context);
            fail("Expected an error");
        } catch (ExpressionParseException e) {
            assertThat(((TranslatableContents) e.getComponent().getContents()).getKey(), is(L10NValues.EXPRESSION_PARSE_NOT_EDITABLE));
            assertThat(e.getPosition(), is(7));
        }
        try {
            ExpressionParser.parse("(b: block) => b == b", context);
            fail("Expected an error");
        } catch (ExpressionParseException e) {
            assertThat(((TranslatableContents) e.getComponent().getContents()).getKey(), is(L10NValues.EXPRESSION_PARSE_PARAMETER_TYPE));
        }
    }

    @Test
    public void testTooDeep() {
        String text = "1" + " + 1".repeat(70);
        try {
            ExpressionParser.parse(text, context());
            fail("Expected an error");
        } catch (ExpressionParseException e) {
            assertThat(((TranslatableContents) e.getComponent().getContents()).getKey(), is(L10NValues.EXPRESSION_PARSE_TOO_DEEP));
        }
        String parens = "(".repeat(200) + "1 + 1" + ")".repeat(200);
        try {
            ExpressionParser.parse(parens, context());
            fail("Expected an error");
        } catch (ExpressionParseException e) {
            assertThat(((TranslatableContents) e.getComponent().getContents()).getKey(), is(L10NValues.EXPRESSION_PARSE_TOO_DEEP));
        }
    }

    // --------------- Tokens of typed text ---------------

    /**
     * @return Each token of the text as its text and the path of its node, after checking that they make up the text,
     *         and that the expression is the parsed one.
     */
    private static List<String> tokens(String text) throws ExpressionParseException {
        ExpressionParser.Tokenized tokenized = ExpressionParser.tokenize(text, context());
        assertThat(tokenized.expression(), is(parse(text)));
        assertThat(tokenized.tokens().stream().map(ExpressionPrinter.Token::text).collect(Collectors.joining()), is(text));
        return tokenized.tokens().stream()
                .map(token -> token.text() + "=" + (token.path() == null ? "-" : Arrays.toString(token.path()))).toList();
    }

    @Test
    public void testTokenize() throws ExpressionParseException {
        // Typed text keeps its form, and each part belongs to its innermost node, as in printed text
        assertThat(tokens("(x: integer) => x.add(3)"), is(List.of("(x: integer) => =-", "x=[0]", ".add(=[]", "3=[1]", ")=[]")));
        assertThat(tokens("#5.length() > 2"), is(List.of("#5=[0, 0]", ".length()=[0]", " > =[]", "2=[1]")));
        assertThat(tokens("  1 + 2  "), is(List.of("  =-", "1=[0]", " + =[]", "2=[1]", "  =-")));
        assertThat(tokens("x => x ? 1 : 2"), is(List.of("x => =-", "x=[0]", " ? =[]", "1=[1]", " : =[]", "2=[2]")));
        // Brackets belong to the value in them, and the minus to its number
        assertThat(tokens("x => (x + 1) * 2"), is(List.of("x => =-", "(=[0]", "x=[0, 0]", " + =[0]", "1=[0, 1]", ")=[0]", " * =[]", "2=[1]")));
        assertThat(tokens("x => !(x == -1)"), is(List.of("x => =-", "!=[]", "(=[0]", "x=[0, 0]", " == =[0]", "-1=[0, 1]", ")=[0]")));
        // Strings with their quotes, also in single quotes
        assertThat(tokens("'a' + \"b\""), is(List.of("'a'=[0]", " + =[]", "\"b\"=[1]")));
        // A called parameter, which is the first input of apply
        assertThat(tokens("(f: operator) => f(1)"), is(List.of("(f: operator) => =-", "f=[0]", "(=[]", "1=[1]", ")=[]")));
        // Empty slots are one instance, which are told apart by their order
        assertThat(tokens("(?) + ?"), is(List.of("(?)=[0]", " + =[]", "?=[1]")));
        assertThat(tokens("? ? ? : (?)"), is(List.of("?=[0]", " ? =[]", "?=[1]", " : =[]", "(?)=[2]")));
        // Operators as values
        assertThat(tokens("l => reduce((+), l, 0)"), is(List.of("l => =-", "reduce(=[]", "(+)=[0]", ", =[]", "l=[1]", ", =[]", "0=[2]", ")=[]")));
    }

}
