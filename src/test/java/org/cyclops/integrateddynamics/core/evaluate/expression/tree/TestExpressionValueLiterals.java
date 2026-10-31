package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.cyclops.cyclopscore.helper.CyclopsCoreInstance;
import org.cyclops.integrateddynamics.ModBaseMocked;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.operator.CurriedOperator;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueDeseralizationContextMocked;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeEntity;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeDouble;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyFactories;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeLong;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.evaluate.variable.Variable;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for how values are written in the text form of expressions, and that each reads back into the same value.
 * Values that need the game's registries, such as items, are tested in TestExpressionValueLiteralsInGame.
 * @author rubensworks
 */
public class TestExpressionValueLiterals {

    static { CyclopsCoreInstance.MOD = new ModBaseMocked(); }

    @BeforeClass
    public static void beforeClass() {
        ValueTypeListProxyFactories.load();
    }

    private static ExpressionParser.Context context() {
        return new ExpressionParser.Context(ValueDeseralizationContextMocked.get(), id -> null,
                label -> label.equals("My Card") ? IntList.of(2) : IntLists.EMPTY_LIST, type -> true, operator -> true);
    }

    private static Expression parse(String text) throws ExpressionParseException {
        return ExpressionParser.parse(text, context());
    }

    private static String printExact(Expression expression) {
        return ExpressionPrinter.printExact(expression, ValueDeseralizationContextMocked.get(), id -> null, label -> IntLists.EMPTY_LIST, id -> null);
    }

    /**
     * @param text The text of a value.
     * @return The value, read as the first input of ==.
     */
    private static IValue read(String text) throws ExpressionParseException {
        Expression expression = parse(text + " == ?");
        return ((ExpressionNode.Const) ((ExpressionNode.Operator) expression.root()).children().get(0)).value();
    }

    private static IValue readUnchecked(String text) {
        try {
            return read(text);
        } catch (ExpressionParseException e) {
            throw new AssertionError(e);
        }
    }

    private static void assertSameValue(String text, IValue actual, IValue expected) {
        assertThat(text, actual, is(expected));
        assertThat(text, actual.getType(), is(expected.getType()));
        // Lists of different element types can be equal
        if (expected instanceof ValueTypeList.ValueList<?, ?> expectedList) {
            ValueTypeList.ValueList<?, ?> actualList = (ValueTypeList.ValueList<?, ?>) actual;
            assertThat(text, actualList.getRawValue().getValueType(), is((IValueType) expectedList.getRawValue().getValueType()));
            List<IValue> expectedElements = Lists.newArrayList(expectedList.getRawValue());
            List<IValue> actualElements = Lists.newArrayList(actualList.getRawValue());
            for (int i = 0; i < expectedElements.size(); i++) {
                assertSameValue(text, actualElements.get(i), expectedElements.get(i));
            }
        }
    }

    /**
     * Write the value in an expression, read that back into the same expression, and write it the same way again.
     * @param value A value.
     * @return How the value is written.
     */
    private static String assertRoundTrip(IValue value) {
        Expression expression = new Expression(new ExpressionNode.Operator(Operators.RELATIONAL_EQUALS,
                List.of(new ExpressionNode.Const(value), ExpressionNode.EMPTY)));
        String text = printExact(expression);
        Expression parsed;
        try {
            parsed = parse(text);
        } catch (ExpressionParseException e) {
            throw new AssertionError("Could not parse " + text + " at " + e.getPosition() + ": " + e.getComponent().getString(), e);
        }
        assertThat(text, parsed, is(expression));
        assertSameValue(text, ((ExpressionNode.Const) ((ExpressionNode.Operator) parsed.root()).children().get(0)).value(), value);
        assertThat(printExact(parsed), is(text));
        return ExpressionPrinter.printLiteral(value, ValueDeseralizationContextMocked.get());
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

    private static ValueTypeString.ValueString s(String value) {
        return ValueTypeString.ValueString.of(value);
    }

    private static ValueTypeInteger.ValueInteger i(int value) {
        return ValueTypeInteger.ValueInteger.of(value);
    }

    private static ValueTypeList.ValueList list(IValueType<?> type, IValue... values) {
        return ValueTypeList.ValueList.ofList((IValueType) type, List.of(values));
    }

    // --------------- Round trips ---------------

    @Test
    public void testRoundTripBooleans() {
        assertThat(assertRoundTrip(ValueTypeBoolean.ValueBoolean.of(true)), is("true"));
        assertThat(assertRoundTrip(ValueTypeBoolean.ValueBoolean.of(false)), is("false"));
    }

    @Test
    public void testRoundTripNumbers() {
        assertThat(assertRoundTrip(i(0)), is("0"));
        assertThat(assertRoundTrip(i(-7)), is("-7"));
        assertRoundTrip(i(Integer.MIN_VALUE));
        assertRoundTrip(i(Integer.MAX_VALUE));
        assertThat(assertRoundTrip(ValueTypeLong.ValueLong.of(5)), is("5L"));
        assertRoundTrip(ValueTypeLong.ValueLong.of(Long.MIN_VALUE));
        assertRoundTrip(ValueTypeLong.ValueLong.of(Long.MAX_VALUE));
        assertThat(assertRoundTrip(ValueTypeDouble.ValueDouble.of(1.5)), is("1.5"));
        assertThat(assertRoundTrip(ValueTypeDouble.ValueDouble.of(2)), is("2.0"));
        assertRoundTrip(ValueTypeDouble.ValueDouble.of(-0.0));
        assertRoundTrip(ValueTypeDouble.ValueDouble.of(1e300));
        assertRoundTrip(ValueTypeDouble.ValueDouble.of(Double.MIN_VALUE));
        assertRoundTrip(ValueTypeDouble.ValueDouble.of(-2.5E-10));
        // NaN is never equal to itself
        assertThat(ExpressionPrinter.printLiteral(ValueTypeDouble.ValueDouble.of(Double.NaN), ValueDeseralizationContextMocked.get()), is("NaN"));
        assertThat(Double.isNaN(((ValueTypeDouble.ValueDouble) readUnchecked("NaN")).getRawValue()), is(true));
        assertThat(assertRoundTrip(ValueTypeDouble.ValueDouble.of(Double.POSITIVE_INFINITY)), is("Infinity"));
        assertThat(assertRoundTrip(ValueTypeDouble.ValueDouble.of(Double.NEGATIVE_INFINITY)), is("-Infinity"));
    }

    @Test
    public void testRoundTripStrings() {
        assertThat(assertRoundTrip(s("")), is("\"\""));
        assertThat(assertRoundTrip(s("apple")), is("\"apple\""));
        assertThat(assertRoundTrip(s("line\nbreak, \"quotes\", back\\slash, tab\t, return\r")),
                is("\"line\\nbreak, \\\"quotes\\\", back\\\\slash, tab\\t, return\\r\""));
        assertRoundTrip(s("unicode: é ✓ 你好 😀"));
        // Pasting drops control characters and the section sign, and curly quotes end a string, so these are escaped
        assertThat(assertRoundTrip(s("\u0001\u007F§c“curly”")), is("\"\\u0001\\u007f\\u00a7c\\u201ccurly\\u201d\""));
    }

    @Test
    public void testRoundTripOperators() {
        assertThat(assertRoundTrip(ValueTypeOperator.ValueOperator.of(Operators.ARITHMETIC_ADDITION)), is("(+)"));
        assertThat(assertRoundTrip(ValueTypeOperator.ValueOperator.of(Operators.LOGICAL_NOT)), is("(!)"));
        assertThat(assertRoundTrip(ValueTypeOperator.ValueOperator.of(Operators.STRING_LENGTH)), is("(length)"));
        // Operators that are not registered, such as curried ones, are written with their type
        assertThat(assertRoundTrip(ValueTypeOperator.ValueOperator.of(new CurriedOperator(Operators.ARITHMETIC_ADDITION,
                new Variable<>(i(1))))), startsWith("@operator{"));
    }

    @Test
    public void testRoundTripNbt() {
        CompoundTag compound = new CompoundTag();
        compound.put("byte", ByteTag.valueOf((byte) 1));
        compound.put("short", ShortTag.valueOf((short) 2));
        compound.put("int", IntTag.valueOf(3));
        compound.put("long", LongTag.valueOf(4));
        compound.put("float", FloatTag.valueOf(5.5F));
        compound.put("double", DoubleTag.valueOf(6.5));
        compound.put("string", StringTag.valueOf("a \"quoted\" name"));
        compound.put("bytes", new ByteArrayTag(new byte[]{1, 2}));
        compound.put("ints", new IntArrayTag(new int[]{3, 4}));
        compound.put("longs", new LongArrayTag(new long[]{5, 6}));
        ListTag list = new ListTag();
        list.add(StringTag.valueOf("x"));
        list.add(StringTag.valueOf("y"));
        compound.put("list", list);
        CompoundTag nested = new CompoundTag();
        nested.putBoolean("flag", true);
        compound.put("nested", nested);
        assertThat(assertRoundTrip(ValueTypeNbt.ValueNbt.of(compound)), startsWith("@nbt{"));

        // Written as the tag itself, as in commands
        CompoundTag simple = new CompoundTag();
        simple.putByte("a", (byte) 1);
        assertThat(assertRoundTrip(ValueTypeNbt.ValueNbt.of(simple)), is("@nbt{a:1b}"));
        assertThat(assertRoundTrip(ValueTypeNbt.ValueNbt.of(new CompoundTag())), is("@nbt{}"));
        // Tags other than compounds, with a space where the type name would continue
        assertThat(assertRoundTrip(ValueTypeNbt.ValueNbt.of(IntTag.valueOf(5))), is("@nbt 5"));
        assertThat(assertRoundTrip(ValueTypeNbt.ValueNbt.of(StringTag.valueOf("text"))), is("@nbt\"text\""));
        assertThat(assertRoundTrip(ValueTypeNbt.ValueNbt.of(list)), is("@nbt[\"x\",\"y\"]"));
        assertThat(assertRoundTrip(ValueTypeNbt.ValueNbt.of(LongTag.valueOf(-3))), is("@nbt -3L"));
        // No NBT at all
        assertThat(assertRoundTrip(ValueTypeNbt.ValueNbt.of()), is("@nbt()"));
    }

    @Test
    public void testRoundTripEntities() {
        UUID uuid = UUID.fromString("0b6c3b6e-8a31-4d1c-9d5e-5d8f2a7c1e33");
        assertThat(assertRoundTrip(ValueObjectTypeEntity.ValueEntity.of(uuid)), is("@entity\"0b6c3b6e-8a31-4d1c-9d5e-5d8f2a7c1e33\""));
        assertThat(assertRoundTrip(ValueObjectTypeEntity.ValueEntity.of((UUID) null)), is("@entity\"\""));
    }

    @Test
    public void testRoundTripLists() {
        assertThat(assertRoundTrip(list(ValueTypes.STRING, s("apple"), s("pear"))), is("[\"apple\", \"pear\"]"));
        assertThat(assertRoundTrip(list(ValueTypes.INTEGER, i(1), i(-2), i(3))), is("[1, -2, 3]"));
        assertThat(assertRoundTrip(list(ValueTypes.LONG, ValueTypeLong.ValueLong.of(1))), is("[1L]"));
        assertThat(assertRoundTrip(list(ValueTypes.DOUBLE, ValueTypeDouble.ValueDouble.of(0.5), ValueTypeDouble.ValueDouble.of(Double.POSITIVE_INFINITY))), is("[0.5, Infinity]"));
        assertThat(assertRoundTrip(list(ValueTypes.BOOLEAN, ValueTypeBoolean.ValueBoolean.of(true))), is("[true]"));
        assertThat(assertRoundTrip(list(ValueTypes.OPERATOR, ValueTypeOperator.ValueOperator.of(Operators.ARITHMETIC_ADDITION),
                ValueTypeOperator.ValueOperator.of(Operators.STRING_LENGTH))), is("[(+), (length)]"));
        CompoundTag tag = new CompoundTag();
        tag.putInt("a", 1);
        assertThat(assertRoundTrip(list(ValueTypes.NBT, ValueTypeNbt.ValueNbt.of(tag), ValueTypeNbt.ValueNbt.of())), is("[@nbt{a:1}, @nbt()]"));
        // Nested lists, also with different element types
        assertThat(assertRoundTrip(list(ValueTypes.LIST, list(ValueTypes.INTEGER, i(1), i(2)), list(ValueTypes.STRING, s("a")))),
                is("[[1, 2], [\"a\"]]"));
        assertThat(assertRoundTrip(list(ValueTypes.LIST, list(ValueTypes.LIST, list(ValueTypes.INTEGER, i(1))))), is("[[[1]]]"));
        // Values of different types make a list of Any
        assertThat(assertRoundTrip(list(ValueTypes.CATEGORY_ANY, s("a"), i(1), ValueTypeLong.ValueLong.of(2))), is("[\"a\", 1, 2L]"));
        assertThat(assertRoundTrip(list(ValueTypes.CATEGORY_ANY)), is("[]"));
        // Lists whose type the values do not give are written with their type
        assertThat(assertRoundTrip(list(ValueTypes.STRING)), startsWith("@list{"));
        assertThat(assertRoundTrip(list(ValueTypes.CATEGORY_ANY, s("a"), s("b"))), startsWith("@list{"));
        assertThat(assertRoundTrip(list(ValueTypes.CATEGORY_NUMBER, i(1), i(2))), startsWith("@list{"));
        // Such a list within a list literal
        assertThat(assertRoundTrip(list(ValueTypes.LIST, list(ValueTypes.STRING), list(ValueTypes.STRING, s("a")))), startsWith("[@list{"));
    }

    // --------------- Reading ---------------

    @Test
    public void testReadLists() throws ExpressionParseException {
        assertSameValue("strings", read("[\"apple\", \"pear\"]"), list(ValueTypes.STRING, s("apple"), s("pear")));
        assertSameValue("nested", read("[[1, 2], [3]]"), list(ValueTypes.LIST, list(ValueTypes.INTEGER, i(1), i(2)), list(ValueTypes.INTEGER, i(3))));
        assertSameValue("mixed", read("[1, 2.5]"), list(ValueTypes.CATEGORY_ANY, i(1), ValueTypeDouble.ValueDouble.of(2.5)));
        assertSameValue("empty", read("[ ]"), list(ValueTypes.CATEGORY_ANY));
        assertSameValue("negative", read("[-1, -Infinity]"), list(ValueTypes.CATEGORY_ANY, i(-1), ValueTypeDouble.ValueDouble.of(Double.NEGATIVE_INFINITY)));
        // A comma after the last value, as in text copied from elsewhere
        assertSameValue("trailing comma", read("[1, 2,]"), list(ValueTypes.INTEGER, i(1), i(2)));
        assertSameValue("spread over lines", read("[\n  \"a\",\n  \"b\"\n]"), list(ValueTypes.STRING, s("a"), s("b")));
        // A list as the input of an operator
        assertThat(parse("l => l == [1]").root(), is(new ExpressionNode.Operator(Operators.RELATIONAL_EQUALS,
                List.of(new ExpressionNode.Param(0), new ExpressionNode.Const(list(ValueTypes.INTEGER, i(1)))))));
    }

    @Test
    public void testReadListErrors() {
        assertError("x => [x] == ?", L10NValues.EXPRESSION_PARSE_LIST_ELEMENT, 6);
        assertError("[1, 1 + 2] == ?", L10NValues.EXPRESSION_PARSE_LIST_ELEMENT, 4);
        assertError("[?] == ?", L10NValues.EXPRESSION_PARSE_LIST_ELEMENT, 1);
        assertError("[#3] == ?", L10NValues.EXPRESSION_PARSE_LIST_ELEMENT, 1);
        assertError("[1, 2 3] == ?", L10NValues.EXPRESSION_PARSE_EXPECTED, 6);
        assertError("[1, 2", L10NValues.EXPRESSION_PARSE_UNEXPECTED_END, 5);
        assertError("[,] == ?", L10NValues.EXPRESSION_PARSE_EXPECTED_VALUE, 1);
    }

    @Test
    public void testReadNbt() throws ExpressionParseException {
        CompoundTag tag = new CompoundTag();
        tag.putByte("a", (byte) 1);
        assertSameValue("compound", read("@nbt {a: 1b}"), ValueTypeNbt.ValueNbt.of(tag));
        assertSameValue("none", read("@nbt ()"), ValueTypeNbt.ValueNbt.of());
        assertSameValue("empty", read("@nbt{}"), ValueTypeNbt.ValueNbt.of(new CompoundTag()));
        assertSameValue("string", read("@nbt \"text\""), ValueTypeNbt.ValueNbt.of(StringTag.valueOf("text")));
    }

    @Test
    public void testReadTypedValueErrors() {
        // Values that would be read as something else than what was typed
        assertError("@list [\"a\", \"b\"] == ?", L10NValues.EXPRESSION_PARSE_VALUE_CHANGED, 6);
        assertError("@list {valueType: \"integrateddynamics:string\", values: [\"a\"]} == ?", L10NValues.EXPRESSION_PARSE_VALUE_CHANGED, 6);
        assertError("@entity \"not-a-uuid\" == ?", L10NValues.EXPRESSION_PARSE_VALUE_CHANGED, 8);
        // Values that cannot be read at all
        assertError("@operator {} == ?", L10NValues.EXPRESSION_PARSE_BAD_VALUE, 10);
        assertError("@nbt {a: } == ?", L10NValues.EXPRESSION_PARSE_BAD_VALUE, 9);
    }

    @Test
    public void testIsKept() throws Exception {
        CompoundTag typed = (CompoundTag) parseTag("{id: \"diamond\", tag: {a: [1, 2]}}");
        assertThat(ExpressionParser.isKept(typed, parseTag("{id: \"minecraft:diamond\", count: 1, tag: {a: [1, 2], b: 3}}")), is(true));
        assertThat(ExpressionParser.isKept(typed, parseTag("{id: \"minecraft:diamond\", tag: {a: [1]}}")), is(false));
        assertThat(ExpressionParser.isKept(typed, parseTag("{id: \"minecraft:air\", tag: {a: [1, 2]}}")), is(false));
        assertThat(ExpressionParser.isKept(typed, parseTag("{tag: {a: [1, 2]}}")), is(false));
        assertThat(ExpressionParser.isKept(parseTag("{}"), parseTag("{a: 1}")), is(true));
    }

    @Test
    public void testIsKeptNumbers() throws Exception {
        // Another number type with the same value, as for amounts that are longs
        assertThat(ExpressionParser.isKept(parseTag("1000"), parseTag("1000L")), is(true));
        assertThat(ExpressionParser.isKept(parseTag("1b"), parseTag("1")), is(true));
        assertThat(ExpressionParser.isKept(parseTag("2"), parseTag("2.0d")), is(true));
        assertThat(ExpressionParser.isKept(parseTag("1.1f"), parseTag(Double.toString((double) 1.1f) + "d")), is(true));
        assertThat(ExpressionParser.isKept(parseTag("{amount: 5}"), parseTag("{amount: 5L, id: \"a\"}")), is(true));
        // But not another value
        assertThat(ExpressionParser.isKept(parseTag("1.5"), parseTag("1")), is(false));
        assertThat(ExpressionParser.isKept(parseTag("300"), parseTag("44b")), is(false));
        assertThat(ExpressionParser.isKept(parseTag("1.1d"), parseTag("1.1f")), is(false));
        assertThat(ExpressionParser.isKept(parseTag("5000000000L"), parseTag("705032704")), is(false));
        assertThat(ExpressionParser.isKept(parseTag("\"1\""), parseTag("1")), is(false));
        // Lists and arrays of the same numbers
        assertThat(ExpressionParser.isKept(parseTag("[1, 2]"), parseTag("[I; 1, 2]")), is(true));
        assertThat(ExpressionParser.isKept(parseTag("[1, 2]"), parseTag("[L; 1L, 2L]")), is(true));
        assertThat(ExpressionParser.isKept(parseTag("[1, 2]"), parseTag("[I; 1, 3]")), is(false));
        assertThat(ExpressionParser.isKept(parseTag("[I; 1, 2]"), parseTag("[1, 2, 3]")), is(false));
    }

    private static Tag parseTag(String text) throws Exception {
        return new net.minecraft.nbt.TagParser(new com.mojang.brigadier.StringReader(text)).readValue();
    }

    // --------------- Pasted text ---------------

    @Test
    public void testPastedText() throws ExpressionParseException {
        // Non-breaking, zero-width and other spaces from web pages and documents
        assertThat(parse("1 +  2​==﻿3"), is(parse("1 + 2 == 3")));
        // Curly quotes, which word processors put in
        assertSameValue("curly quotes", read("“apple”"), s("apple"));
        assertSameValue("low quotes", read("„apple“"), s("apple"));
        assertThat(parse("#“My Card” == ?").root(), is(parse("#\"My Card\" == ?").root()));
        // Windows line breaks
        assertThat(parse("1 +\r\n2 == 3"), is(parse("1 + 2 == 3")));
    }

    @Test
    public void testTooLong() {
        assertError("1 + " + " ".repeat(ExpressionParser.MAX_LENGTH), L10NValues.EXPRESSION_PARSE_TOO_LONG, ExpressionParser.MAX_LENGTH);
    }

}
