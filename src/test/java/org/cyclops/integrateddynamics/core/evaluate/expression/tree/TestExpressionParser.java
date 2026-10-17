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
import java.util.List;
import java.util.Map;

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
        assertThat(printExact(typed), is("length(#5)"));
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
        assertThat(parse("x -> !x && x || x").root(), is(op(Operators.LOGICAL_OR,
                op(Operators.LOGICAL_AND, op(Operators.LOGICAL_NOT, p(0)), p(0)), p(0))));
        // Choices are right associative and bind weakest
        assertThat(parse("x -> x ? 1 : x ? 2 : 3 + 1").root(), is(op(Operators.GENERAL_CHOICE, p(0), i(1),
                op(Operators.GENERAL_CHOICE, p(0), i(2), op(Operators.ARITHMETIC_ADDITION, i(3), i(1))))));
    }

    @Test
    public void testParameters() throws ExpressionParseException {
        assertThat(parse("x -> x + 1").parameters(), is(List.of(param("x", ValueTypes.CATEGORY_ANY))));
        assertThat(parse("(x) -> x + 1").parameters(), is(List.of(param("x", ValueTypes.CATEGORY_ANY))));
        assertThat(parse("(item: itemstack, n: number) -> n + 1").parameters(),
                is(List.of(param("item", ValueTypes.OBJECT_ITEMSTACK), param("n", ValueTypes.CATEGORY_NUMBER))));
        // A parenthesized parameter is not an operator value
        assertThat(parse("(length) -> (length) + 1").root(), is(op(Operators.ARITHMETIC_ADDITION, p(0), i(1))));
    }

    @Test
    public void testNamesTellApartByType() throws ExpressionParseException {
        assertThat(parse("(s: string) -> length(s)").root(), is(op(Operators.STRING_LENGTH, p(0))));
        assertThat(parse("(l: list) -> length(l)").root(), is(op(Operators.LIST_LENGTH, p(0))));
        assertThat(parse("x -> listLength(x)").root(), is(op(Operators.LIST_LENGTH, p(0))));
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

    // --------------- Errors ---------------

    @Test
    public void testErrors() {
        assertError("x -> lenght(x)", L10NValues.EXPRESSION_PARSE_UNKNOWN_OPERATOR, 5);
        assertError("x -> length(x)", L10NValues.EXPRESSION_PARSE_AMBIGUOUS_OPERATOR, 5);
        assertError("length(\"a\", \"b\")", L10NValues.EXPRESSION_PARSE_INPUT_COUNT, 0);
        assertError("length(\"abc)", L10NValues.EXPRESSION_PARSE_UNTERMINATED_STRING, 7);
        assertError("1 + 2 3", L10NValues.EXPRESSION_PARSE_TRAILING, 6);
        assertError("y + 1", L10NValues.EXPRESSION_PARSE_UNKNOWN_NAME, 0);
        assertError("f -> f(1)", L10NValues.EXPRESSION_PARSE_PARAMETER_CALL, 5);
        assertError("3000000000 + 1", L10NValues.EXPRESSION_PARSE_INTEGER_TOO_LARGE, 0);
        assertError("x -> x", L10NValues.EXPRESSION_PARSE_ROOT, 5);
        assertError("(x, x) -> x + x", L10NValues.EXPRESSION_PARSE_DUPLICATE_PARAMETER, 4);
        assertError("true -> 1 + 1", L10NValues.EXPRESSION_PARSE_PARAMETER_NAME, 0);
        assertError("(x: thing) -> x + 1", L10NValues.EXPRESSION_PARSE_UNKNOWN_TYPE, 4);
        assertError("@thing{} == 1", L10NValues.EXPRESSION_PARSE_UNKNOWN_TYPE, 0);
        assertError("@nbt {a: } == 1", L10NValues.EXPRESSION_PARSE_BAD_VALUE, 9);
        assertError("1 + ", L10NValues.EXPRESSION_PARSE_EXPECTED_VALUE_END, 4);
        assertError("(1 + 2", L10NValues.EXPRESSION_PARSE_UNEXPECTED_END, 6);
        assertError("1 + $", L10NValues.EXPRESSION_PARSE_UNKNOWN_CHARACTER, 4);
        assertError("#nothing + 1", L10NValues.EXPRESSION_PARSE_UNKNOWN_LABEL, 0);
        assertError("#twice + 1", L10NValues.EXPRESSION_PARSE_AMBIGUOUS_LABEL, 0);
        assertError("x -> -x", L10NValues.EXPRESSION_PARSE_EXPECTED_VALUE, 5);
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
            ExpressionParser.parse("(b: block) -> b == b", context);
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

}
