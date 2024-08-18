package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.cyclops.cyclopscore.helper.CyclopsCoreInstance;
import org.cyclops.integrateddynamics.ModBaseMocked;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Test the different variable types.
 * @author rubensworks
 */
public class TestVariables {

    static { CyclopsCoreInstance.MOD = new ModBaseMocked(); }
    public static final ValueDeseralizationContext context = ValueDeseralizationContextMocked.get();

    @Test
    public void testBooleanType() throws EvaluationException {
        DummyVariableBoolean bTrue = new DummyVariableBoolean(ValueTypeBoolean.ValueBoolean.of(true));
        assertThat("true value is true", bTrue.getValue().getRawValue(), is(true));
        assertThat("true value is not false", bTrue.getValue().getRawValue(), not(false));

        DummyVariableBoolean bFalse = new DummyVariableBoolean(ValueTypeBoolean.ValueBoolean.of(false));
        assertThat("false value is false", bFalse.getValue().getRawValue(), is(false));
        assertThat("false value is not true", bFalse.getValue().getRawValue(), not(true));

        bTrue.setValue(ValueTypeBoolean.ValueBoolean.of(false));
        assertThat("false value is false", bTrue.getValue().getRawValue(), is(false));
        assertThat("false value is not true", bTrue.getValue().getRawValue(), not(true));

        bFalse.setValue(ValueTypeBoolean.ValueBoolean.of(true));
        assertThat("true value is true", bFalse.getValue().getRawValue(), is(true));
        assertThat("true value is not false", bFalse.getValue().getRawValue(), not(false));

        bFalse.setValue(ValueTypeBoolean.ValueBoolean.of(false));
        bTrue.setValue(ValueTypeBoolean.ValueBoolean.of(true));
        assertThat("serializing false returns false", bFalse.getType().serialize(context, bFalse.getValue()), is(ByteTag.valueOf((byte)0)));
        assertThat("serializing true returns true", bTrue.getType().serialize(context, bTrue.getValue()), is(ByteTag.valueOf((byte)1)));

        assertThat("deserializing false returns false", bFalse.getType().deserialize(context, ByteTag.valueOf((byte)0)), is(bFalse.getValue()));
        assertThat("deserializing true returns true", bTrue.getType().deserialize(context, ByteTag.valueOf((byte)1)), is(bTrue.getValue()));

        bFalse.setValue(ValueTypeBoolean.ValueBoolean.of(false));
        bTrue.setValue(ValueTypeBoolean.ValueBoolean.of(true));
        assertThat("serializing false returns false", bFalse.getType().toString(bFalse.getValue()), is("false"));
        assertThat("serializing true returns true", bTrue.getType().toString(bTrue.getValue()), is("true"));

        assertThat("deserializing false returns false", bFalse.getType().parseString("false"), is(bFalse.getValue()));
        assertThat("deserializing true returns true", bTrue.getType().parseString("true"), is(bTrue.getValue()));
    }

    @Test
    public void testIntegerType() throws EvaluationException {
        DummyVariableInteger i0 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0));
        assertThat("0 value is 0", i0.getValue().getRawValue(), is(0));

        DummyVariableInteger im10 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(-10));
        assertThat("-10 value is -10", im10.getValue().getRawValue(), is(-10));

        DummyVariableInteger i10 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(10));
        assertThat("10 value is 10", i10.getValue().getRawValue(), is(10));

        assertThat("serializing 10 returns 10", i10.getType().serialize(context, i10.getValue()), is(IntTag.valueOf(10)));
        assertThat("serializing -10 returns -10", im10.getType().serialize(context, im10.getValue()), is(IntTag.valueOf(-10)));
        assertThat("serializing 0 returns 0", i0.getType().serialize(context, i0.getValue()), is(IntTag.valueOf(0)));

        assertThat("deserializing 10 returns 10", i10.getType().deserialize(context, IntTag.valueOf(10)), is(i10.getValue()));
        assertThat("deserializing -10 returns -10", im10.getType().deserialize(context, IntTag.valueOf(-10)), is(im10.getValue()));
        assertThat("deserializing 0 returns 0", i0.getType().deserialize(context, IntTag.valueOf(0)), is(i0.getValue()));

        assertThat("serializing 10 returns 10", i10.getType().toString(i10.getValue()), is("10"));
        assertThat("serializing -10 returns -10", im10.getType().toString(im10.getValue()), is("-10"));
        assertThat("serializing 0 returns 0", i0.getType().toString(i0.getValue()), is("0"));

        assertThat("deserializing 10 returns 10", i10.getType().parseString("10"), is(i10.getValue()));
        assertThat("deserializing -10 returns -10", im10.getType().parseString("-10"), is(im10.getValue()));
        assertThat("deserializing 0 returns 0", i0.getType().parseString("0"), is(i0.getValue()));
    }

    @Test
    public void testDoubleType() throws EvaluationException {
        DummyVariableDouble d0 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(0.1));
        assertThat("0.1 value is 0.1", d0.getValue().getRawValue(), is(0.1));

        DummyVariableDouble dm10 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(-10.1));
        assertThat("-10.1 value is -10.1", dm10.getValue().getRawValue(), is(-10.1));

        DummyVariableDouble d10 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(10.1));
        assertThat("10.1 value is 10.1", d10.getValue().getRawValue(), is(10.1));

        assertThat("serializing 10.1 returns 10.1", d10.getType().serialize(context, d10.getValue()), is(DoubleTag.valueOf(10.1)));
        assertThat("serializing -10.1 returns -10.1", dm10.getType().serialize(context, dm10.getValue()), is(DoubleTag.valueOf(-10.1)));
        assertThat("serializing 0.1 returns 0.1", d0.getType().serialize(context, d0.getValue()), is(DoubleTag.valueOf(0.1)));

        assertThat("deserializing 10.1 returns 10.1", d10.getType().deserialize(context, DoubleTag.valueOf(10.1)), is(d10.getValue()));
        assertThat("deserializing -10.1 returns -10.1", dm10.getType().deserialize(context, DoubleTag.valueOf(-10.1)), is(dm10.getValue()));
        assertThat("deserializing 0.1 returns 0.1", d0.getType().deserialize(context, DoubleTag.valueOf(0.1)), is(d0.getValue()));

        assertThat("serializing 10.1 returns 10.1", d10.getType().toString(d10.getValue()), is("10.1"));
        assertThat("serializing -10.1 returns -10.1", dm10.getType().toString(dm10.getValue()), is("-10.1"));
        assertThat("serializing 0.1 returns 0.1", d0.getType().toString(d0.getValue()), is("0.1"));

        assertThat("deserializing 10.1 returns 10.1", d10.getType().parseString("10.1"), is(d10.getValue()));
        assertThat("deserializing -10.1 returns -10.1", dm10.getType().parseString("-10.1"), is(dm10.getValue()));
        assertThat("deserializing 0.1 returns 0.1", d0.getType().parseString("0.1"), is(d0.getValue()));
    }

    @Test
    public void testStringType() throws EvaluationException {
        DummyVariableString s0 = new DummyVariableString(ValueTypeString.ValueString.of("0"));
        assertThat("0 value is 0", s0.getValue().getRawValue(), is("0"));

        DummyVariableString sm10 = new DummyVariableString(ValueTypeString.ValueString.of("-10"));
        assertThat("-10 value is -10", sm10.getValue().getRawValue(), is("-10"));

        DummyVariableString s10 = new DummyVariableString(ValueTypeString.ValueString.of("10"));
        assertThat("10 value is 10", s10.getValue().getRawValue(), is("10"));

        assertThat("serializing 10 returns 10", s10.getType().serialize(context, s10.getValue()), is(StringTag.valueOf("10")));
        assertThat("serializing -10 returns -10", sm10.getType().serialize(context, sm10.getValue()), is(StringTag.valueOf("-10")));
        assertThat("serializing 0 returns 0", s0.getType().serialize(context, s0.getValue()), is(StringTag.valueOf("0")));

        assertThat("deserializing 10 returns 10", s10.getType().deserialize(context, StringTag.valueOf("10")), is(s10.getValue()));
        assertThat("deserializing -10 returns -10", sm10.getType().deserialize(context, StringTag.valueOf("-10")), is(sm10.getValue()));
        assertThat("deserializing 0 returns 0", s0.getType().deserialize(context, StringTag.valueOf("0")), is(s0.getValue()));

        assertThat("serializing 10 returns 10", s10.getType().toString(s10.getValue()), is("10"));
        assertThat("serializing -10 returns -10", sm10.getType().toString(sm10.getValue()), is("-10"));
        assertThat("serializing 0 returns 0", s0.getType().toString(s0.getValue()), is("0"));

        assertThat("deserializing 10 returns 10", s10.getType().parseString("10"), is(s10.getValue()));
        assertThat("deserializing -10 returns -10", sm10.getType().parseString("-10"), is(sm10.getValue()));
        assertThat("deserializing 0 returns 0", s0.getType().parseString("0"), is(s0.getValue()));
    }

    @Test
    public void testListTypeMaterialized() throws EvaluationException {
        ValueTypeListProxyFactories.load();

        DummyVariableList l0 = new DummyVariableList(ValueTypeList.ValueList.ofAll());
        assertThat("empty boolean list has length zero", l0.getValue().getRawValue().getLength(), is(0));
        assertThat("empty boolean list has boolean type", l0.getValue().getRawValue().getValueType(), CoreMatchers.<IValueType>is(ValueTypes.CATEGORY_ANY));

        DummyVariableList l2 = new DummyVariableList(ValueTypeList.ValueList.ofAll(
                ValueTypeString.ValueString.of("a"), ValueTypeString.ValueString.of("b"))
        );
        assertThat("string list has length two", l2.getValue().getRawValue().getLength(), is(2));
        assertThat("string list has string type", l2.getValue().getRawValue().getValueType(), CoreMatchers.<IValueType>is(ValueTypes.STRING));

        DummyVariableList l2_2 = new DummyVariableList(ValueTypeList.ValueList.ofAll(
                ValueTypeList.ValueList.ofAll(ValueTypeString.ValueString.of("a"), ValueTypeString.ValueString.of("b")),
                ValueTypeList.ValueList.ofAll(ValueTypeString.ValueString.of("c"), ValueTypeString.ValueString.of("d"))
        ));
        assertThat("nested list has length two", l2_2.getValue().getRawValue().getLength(), is(2));
        assertThat("nestedlist has list type", l2_2.getValue().getRawValue().getValueType(), CoreMatchers.<IValueType>is(ValueTypes.LIST));

        DummyVariableList l2h = new DummyVariableList(ValueTypeList.ValueList.ofAll(ValueTypes.CATEGORY_ANY,
                ValueTypeInteger.ValueInteger.of(42),
                ValueTypeString.ValueString.of("hello")
        ));
        assertThat("heterogeneous list has length two", l2h.getValue().getRawValue().getLength(), is(2));
        assertThat("heterogeneous list has any type", l2h.getValue().getRawValue().getValueType(), CoreMatchers.<IValueType>is(ValueTypes.CATEGORY_ANY));

        // Make empty list
        CompoundTag tagEmptySerialized = new CompoundTag();
        tagEmptySerialized.putString("valueType", "integrateddynamics:any");
        tagEmptySerialized.put("values", new ListTag());
        CompoundTag tagEmpty = new CompoundTag();
        tagEmpty.putString("proxyName", "integrateddynamics:materialized");
        tagEmpty.put("serialized", tagEmptySerialized);

        // Make string list
        CompoundTag tagStringSerialized = new CompoundTag();
        tagStringSerialized.putString("valueType", "integrateddynamics:string");
        ListTag listString = new ListTag();
        listString.add(StringTag.valueOf("a"));
        listString.add(StringTag.valueOf("b"));
        tagStringSerialized.put("values", listString);
        CompoundTag tagString = new CompoundTag();
        tagString.putString("proxyName", "integrateddynamics:materialized");
        tagString.put("serialized", tagStringSerialized);

        // Make nested list
        CompoundTag tagStringNestedSerialized = new CompoundTag();
        tagStringNestedSerialized.putString("valueType", "integrateddynamics:list");
        // --> 1
        CompoundTag tagStringNestedSub1Serialized = new CompoundTag();
        tagStringNestedSub1Serialized.putString("valueType", "integrateddynamics:string");
        ListTag listStringNestedSub1 = new ListTag();
        listStringNestedSub1.add(StringTag.valueOf("a"));
        listStringNestedSub1.add(StringTag.valueOf("b"));
        tagStringNestedSub1Serialized.put("values", listStringNestedSub1);
        CompoundTag tagStringNestedSub1 = new CompoundTag();
        tagStringNestedSub1.putString("proxyName", "integrateddynamics:materialized");
        tagStringNestedSub1.put("serialized", tagStringNestedSub1Serialized);
        // --> 2
        CompoundTag tagStringNestedSub2Serialized = new CompoundTag();
        tagStringNestedSub2Serialized.putString("valueType", "integrateddynamics:string");
        ListTag listStringNestedSub2 = new ListTag();
        listStringNestedSub2.add(StringTag.valueOf("c"));
        listStringNestedSub2.add(StringTag.valueOf("d"));
        tagStringNestedSub2Serialized.put("values", listStringNestedSub2);
        CompoundTag tagStringNestedSub2 = new CompoundTag();
        tagStringNestedSub2.putString("proxyName", "integrateddynamics:materialized");
        tagStringNestedSub2.put("serialized", tagStringNestedSub2Serialized);
        // <--
        ListTag tagStringNestedSerializedArray = new ListTag();
        tagStringNestedSerializedArray.add(tagStringNestedSub1);
        tagStringNestedSerializedArray.add(tagStringNestedSub2);
        tagStringNestedSerialized.put("values", tagStringNestedSerializedArray);
        CompoundTag tagStringNested = new CompoundTag();
        tagStringNested.putString("proxyName", "integrateddynamics:materialized");
        tagStringNested.put("serialized", tagStringNestedSerialized);

        // Make heterogeneous list
        CompoundTag tagHeterogeneousSerialized = new CompoundTag();
        tagHeterogeneousSerialized.putString("valueType", "integrateddynamics:any");
        ListTag listHeterogeneous = new ListTag();
        CompoundTag valueHeterogeneous1 = new CompoundTag();
        CompoundTag valueHeterogeneous2 = new CompoundTag();
        valueHeterogeneous1.putString("valueType", "integrateddynamics:integer");
        valueHeterogeneous1.putInt("value", 42);
        valueHeterogeneous2.putString("valueType", "integrateddynamics:string");
        valueHeterogeneous2.putString("value", "hello");
        listHeterogeneous.add(valueHeterogeneous1);
        listHeterogeneous.add(valueHeterogeneous2);
        tagHeterogeneousSerialized.put("values", listHeterogeneous);
        CompoundTag tagHeterogeneous = new CompoundTag();
        tagHeterogeneous.putString("proxyName", "integrateddynamics:materialized");
        tagHeterogeneous.put("serialized", tagHeterogeneousSerialized);

        assertThat("serializing empty list",
                l0.getType().serialize(context, l0.getValue()), is(tagEmpty));
        assertThat("serializing string list",
                l2.getType().serialize(context, l2.getValue()), is(tagString));
        assertThat("serializing nested list",
                l2.getType().serialize(context, l2_2.getValue()), is(tagStringNested));
        assertThat("serializing heterogeneous list",
                l2h.getType().serialize(context, l2h.getValue()), is(tagHeterogeneous));

        assertThat("deserializing empty list",
                l0.getType().deserialize(context, tagEmpty), is(l0.getValue()));
        assertThat("deserializing string list",
                l2.getType().deserialize(context, tagString), is(l2.getValue()));
        assertThat("deserializing nested list",
                l2_2.getType().deserialize(context, tagStringNested), is(l2_2.getValue()));
        assertThat("deserializing heterogeneous list",
                l2h.getType().deserialize(context, tagHeterogeneous), is(l2h.getValue()));
    }

    @Test
    public void testNbtType() throws EvaluationException {
        DummyVariableNbt snull = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of());
        assertThat("null value is empty NBT tag", snull.getValue().getRawValue(), is(Optional.empty()));

        CompoundTag tag1 = new CompoundTag();
        tag1.putBoolean("abc", true);
        CompoundTag tag2 = new CompoundTag();
        tag2.putBoolean("abc", true);

        StringTag strTag1 = StringTag.valueOf("abc");
        StringTag strTag2 = StringTag.valueOf("abc");

        CompoundTag tagWrapped = new CompoundTag();
        tagWrapped.put("v", tag1);

        CompoundTag strTagWrapped = new CompoundTag();
        strTagWrapped.put("v", strTag2);

        DummyVariableNbt tagVariable = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(tag1));
        DummyVariableNbt strTagVariable = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(strTag1));

        assertThat("tag value is tag", tagVariable.getValue().getRawValue().get(), is(tag2));
        assertThat("string tag value is tag", strTagVariable.getValue().getRawValue().get(), is(strTag2));

        assertThat("serializing null value returns empty NBT tag", snull.getType().serialize(context, snull.getValue()), is(new CompoundTag()));
        assertThat("serializing tag returns tag", tagVariable.getType().serialize(context, tagVariable.getValue()), is(tagWrapped));
        assertThat("serializing string tag returns tag", tagVariable.getType().serialize(context, strTagVariable.getValue()), is(strTagWrapped));

        assertThat("deserializing null value returns empty NBT tag", snull.getType().deserialize(context, new CompoundTag()), is(snull.getValue()));
        assertThat("deserializing tag returns tag", tagVariable.getType().deserialize(context, tagWrapped), is(tagVariable.getValue()));
        assertThat("deserializing string tag returns tag", strTagVariable.getType().deserialize(context, strTagWrapped), is(strTagVariable.getValue()));

        assertThat("serializing null value returns empty NBT tag", snull.getType().toString(snull.getValue()), is(""));
        assertThat("serializing tag returns tag", tagVariable.getType().toString(tagVariable.getValue()), is("{abc:1b}"));
        assertThat("serializing string tag returns tag", strTagVariable.getType().toString(strTagVariable.getValue()), is("\"abc\""));

        assertThat("deserializing null value returns empty NBT tag", snull.getType().parseString(""), is(snull.getValue()));
        assertThat("deserializing tag returns tag", tagVariable.getType().parseString("{abc:1b}"), is(tagVariable.getValue()));
        assertThat("deserializing string tag returns tag", strTagVariable.getType().parseString("\"abc\""), is(strTagVariable.getValue()));
    }

    @Test(expected = EvaluationException.class)
    public void testNbtTypeInvalidString() throws EvaluationException {
        ValueTypes.NBT.parseString("\"");
    }

}
