package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.EndNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Test the different variable types.
 * @author rubensworks
 */
public class TestVariables {

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
        assertThat("serializing false returns false", bFalse.getType().serialize(bFalse.getValue()), is(new ByteNBT((byte)0)));
        assertThat("serializing true returns true", bTrue.getType().serialize(bTrue.getValue()), is(new ByteNBT((byte)1)));

        assertThat("deserializing false returns false", bFalse.getType().deserialize(new ByteNBT((byte)0)), is(bFalse.getValue()));
        assertThat("deserializing true returns true", bTrue.getType().deserialize(new ByteNBT((byte)1)), is(bTrue.getValue()));

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

        assertThat("serializing 10 returns 10", i10.getType().serialize(i10.getValue()), is(new IntNBT(10)));
        assertThat("serializing -10 returns -10", im10.getType().serialize(im10.getValue()), is(new IntNBT(-10)));
        assertThat("serializing 0 returns 0", i0.getType().serialize(i0.getValue()), is(new IntNBT(0)));

        assertThat("deserializing 10 returns 10", i10.getType().deserialize(new IntNBT(10)), is(i10.getValue()));
        assertThat("deserializing -10 returns -10", im10.getType().deserialize(new IntNBT(-10)), is(im10.getValue()));
        assertThat("deserializing 0 returns 0", i0.getType().deserialize(new IntNBT(0)), is(i0.getValue()));

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

        assertThat("serializing 10.1 returns 10.1", d10.getType().serialize(d10.getValue()), is(new DoubleNBT(10.1)));
        assertThat("serializing -10.1 returns -10.1", dm10.getType().serialize(dm10.getValue()), is(new DoubleNBT(-10.1)));
        assertThat("serializing 0.1 returns 0.1", d0.getType().serialize(d0.getValue()), is(new DoubleNBT(0.1)));

        assertThat("deserializing 10.1 returns 10.1", d10.getType().deserialize(new DoubleNBT(10.1)), is(d10.getValue()));
        assertThat("deserializing -10.1 returns -10.1", dm10.getType().deserialize(new DoubleNBT(-10.1)), is(dm10.getValue()));
        assertThat("deserializing 0.1 returns 0.1", d0.getType().deserialize(new DoubleNBT(0.1)), is(d0.getValue()));

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

        assertThat("serializing 10 returns 10", s10.getType().serialize(s10.getValue()), is(new StringNBT("10")));
        assertThat("serializing -10 returns -10", sm10.getType().serialize(sm10.getValue()), is(new StringNBT("-10")));
        assertThat("serializing 0 returns 0", s0.getType().serialize(s0.getValue()), is(new StringNBT("0")));

        assertThat("deserializing 10 returns 10", s10.getType().deserialize(new StringNBT("10")), is(s10.getValue()));
        assertThat("deserializing -10 returns -10", sm10.getType().deserialize(new StringNBT("-10")), is(sm10.getValue()));
        assertThat("deserializing 0 returns 0", s0.getType().deserialize(new StringNBT("0")), is(s0.getValue()));

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
        CompoundNBT tagEmptySerialized = new CompoundNBT();
        tagEmptySerialized.putString("valueType", "integrateddynamics:any");
        tagEmptySerialized.put("values", new ListNBT());
        CompoundNBT tagEmpty = new CompoundNBT();
        tagEmpty.putString("proxyName", "integrateddynamics:materialized");
        tagEmpty.put("serialized", tagEmptySerialized);

        // Make string list
        CompoundNBT tagStringSerialized = new CompoundNBT();
        tagStringSerialized.putString("valueType", "integrateddynamics:string");
        ListNBT listString = new ListNBT();
        listString.add(new StringNBT("a"));
        listString.add(new StringNBT("b"));
        tagStringSerialized.put("values", listString);
        CompoundNBT tagString = new CompoundNBT();
        tagString.putString("proxyName", "integrateddynamics:materialized");
        tagString.put("serialized", tagStringSerialized);
        
        // Make nested list
        CompoundNBT tagStringNestedSerialized = new CompoundNBT();
        tagStringNestedSerialized.putString("valueType", "integrateddynamics:list");
        // --> 1
        CompoundNBT tagStringNestedSub1Serialized = new CompoundNBT();
        tagStringNestedSub1Serialized.putString("valueType", "integrateddynamics:string");
        ListNBT listStringNestedSub1 = new ListNBT();
        listStringNestedSub1.add(new StringNBT("a"));
        listStringNestedSub1.add(new StringNBT("b"));
        tagStringNestedSub1Serialized.put("values", listStringNestedSub1);
        CompoundNBT tagStringNestedSub1 = new CompoundNBT();
        tagStringNestedSub1.putString("proxyName", "integrateddynamics:materialized");
        tagStringNestedSub1.put("serialized", tagStringNestedSub1Serialized);
        // --> 2
        CompoundNBT tagStringNestedSub2Serialized = new CompoundNBT();
        tagStringNestedSub2Serialized.putString("valueType", "integrateddynamics:string");
        ListNBT listStringNestedSub2 = new ListNBT();
        listStringNestedSub2.add(new StringNBT("c"));
        listStringNestedSub2.add(new StringNBT("d"));
        tagStringNestedSub2Serialized.put("values", listStringNestedSub2);
        CompoundNBT tagStringNestedSub2 = new CompoundNBT();
        tagStringNestedSub2.putString("proxyName", "integrateddynamics:materialized");
        tagStringNestedSub2.put("serialized", tagStringNestedSub2Serialized);
        // <--
        ListNBT tagStringNestedSerializedArray = new ListNBT();
        tagStringNestedSerializedArray.add(tagStringNestedSub1);
        tagStringNestedSerializedArray.add(tagStringNestedSub2);
        tagStringNestedSerialized.put("values", tagStringNestedSerializedArray);
        CompoundNBT tagStringNested = new CompoundNBT();
        tagStringNested.putString("proxyName", "integrateddynamics:materialized");
        tagStringNested.put("serialized", tagStringNestedSerialized);

        // Make heterogeneous list
        CompoundNBT tagHeterogeneousSerialized = new CompoundNBT();
        tagHeterogeneousSerialized.putString("valueType", "integrateddynamics:any");
        ListNBT listHeterogeneous = new ListNBT();
        CompoundNBT valueHeterogeneous1 = new CompoundNBT();
        CompoundNBT valueHeterogeneous2 = new CompoundNBT();
        valueHeterogeneous1.putString("valueType", "integrateddynamics:integer");
        valueHeterogeneous1.putInt("value", 42);
        valueHeterogeneous2.putString("valueType", "integrateddynamics:string");
        valueHeterogeneous2.putString("value", "hello");
        listHeterogeneous.add(valueHeterogeneous1);
        listHeterogeneous.add(valueHeterogeneous2);
        tagHeterogeneousSerialized.put("values", listHeterogeneous);
        CompoundNBT tagHeterogeneous = new CompoundNBT();
        tagHeterogeneous.putString("proxyName", "integrateddynamics:materialized");
        tagHeterogeneous.put("serialized", tagHeterogeneousSerialized);

        assertThat("serializing empty list",
                l0.getType().serialize(l0.getValue()), is(tagEmpty));
        assertThat("serializing string list",
                l2.getType().serialize(l2.getValue()), is(tagString));
        assertThat("serializing nested list",
                l2.getType().serialize(l2_2.getValue()), is(tagStringNested));
        assertThat("serializing heterogeneous list",
                l2h.getType().serialize(l2h.getValue()), is(tagHeterogeneous));

        assertThat("deserializing empty list",
                l0.getType().deserialize(tagEmpty), is(l0.getValue()));
        assertThat("deserializing string list",
                l2.getType().deserialize(tagString), is(l2.getValue()));
        assertThat("deserializing nested list",
                l2_2.getType().deserialize(tagStringNested), is(l2_2.getValue()));
        assertThat("deserializing heterogeneous list",
                l2h.getType().deserialize(tagHeterogeneous), is(l2h.getValue()));
    }

    @Test
    public void testNbtType() throws EvaluationException {
        DummyVariableNbt snull = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(null));
        assertThat("null value is empty NBT tag", snull.getValue().getRawValue(), is(new CompoundNBT()));

        CompoundNBT tag1 = new CompoundNBT();
        tag1.putBoolean("abc", true);
        CompoundNBT tag2 = new CompoundNBT();
        tag2.putBoolean("abc", true);
        DummyVariableNbt stag = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(tag1));
        assertThat("tag value is tag", stag.getValue().getRawValue(), is(tag2));

        assertThat("serializing null value returns empty NBT tag", snull.getType().serialize(snull.getValue()), is(new CompoundNBT()));
        assertThat("serializing tag returns tag", stag.getType().serialize(stag.getValue()), is(stag.getValue().getRawValue()));

        assertThat("deserializing null value returns empty NBT tag", snull.getType().deserialize(new CompoundNBT()), is(snull.getValue()));
        assertThat("deserializing tag returns tag", stag.getType().deserialize(stag.getValue().getRawValue()), is(stag.getValue()));

        assertThat("serializing null value returns empty NBT tag", snull.getType().toString(snull.getValue()), is("{}"));
        assertThat("serializing tag returns tag", stag.getType().toString(stag.getValue()), is("{abc:1b}"));

        assertThat("deserializing null value returns empty NBT tag", snull.getType().parseString("{}"), is(snull.getValue()));
        assertThat("deserializing tag returns tag", stag.getType().parseString("{abc:1b}"), is(stag.getValue()));
    }

}
