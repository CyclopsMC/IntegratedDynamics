package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.CompoundTag;
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

import static org.cyclops.integrateddynamics.core.test.TestHelpers.deserialize;
import static org.cyclops.integrateddynamics.core.test.TestHelpers.serialize;
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
        CompoundTag tag0 = new CompoundTag();
        tag0.putByte("v", (byte) 0);
        CompoundTag tag1 = new CompoundTag();
        tag1.putByte("v", (byte) 1);

        assertThat("serializing false returns false", serialize(o -> bFalse.getType().serialize(o, bFalse.getValue()), context.holderLookupProvider()), is(tag0));
        assertThat("serializing true returns true", serialize(o -> bTrue.getType().serialize(o, bTrue.getValue()), context.holderLookupProvider()), is(tag1));

        assertThat("deserializing false returns false", deserialize(tag0, bFalse.getType()::deserialize, context.holderLookupProvider()), is(bFalse.getValue()));
        assertThat("deserializing true returns true", deserialize(tag1, bTrue.getType()::deserialize, context.holderLookupProvider()), is(bTrue.getValue()));

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

        CompoundTag tag10 = new CompoundTag();
        tag10.putInt("v", 10);
        CompoundTag tagm10 = new CompoundTag();
        tagm10.putInt("v", -10);
        CompoundTag tag0 = new CompoundTag();
        tag0.putInt("v", 0);

        assertThat("serializing 10 returns 10", serialize(o -> i10.getType().serialize(o, i10.getValue()), context.holderLookupProvider()), is(tag10));
        assertThat("serializing -10 returns -10", serialize(o -> im10.getType().serialize(o, im10.getValue()), context.holderLookupProvider()), is(tagm10));
        assertThat("serializing 0 returns 0", serialize(o -> i0.getType().serialize(o, i0.getValue()), context.holderLookupProvider()), is(tag0));

        assertThat("deserializing 10 returns 10", deserialize(tag10, i10.getType()::deserialize, context.holderLookupProvider()), is(i10.getValue()));
        assertThat("deserializing -10 returns -10", deserialize(tagm10, im10.getType()::deserialize, context.holderLookupProvider()), is(im10.getValue()));
        assertThat("deserializing 0 returns 0", deserialize(tag0, i0.getType()::deserialize, context.holderLookupProvider()), is(i0.getValue()));

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

        CompoundTag tag10 = new CompoundTag();
        tag10.putDouble("v", 10.1);
        CompoundTag tagm10 = new CompoundTag();
        tagm10.putDouble("v", -10.1);
        CompoundTag tag0 = new CompoundTag();
        tag0.putDouble("v", 0.1);

        assertThat("serializing 10.1 returns 10.1", serialize(o -> d10.getType().serialize(o, d10.getValue()), context.holderLookupProvider()), is(tag10));
        assertThat("serializing -10.1 returns -10.1", serialize(o -> dm10.getType().serialize(o, dm10.getValue()), context.holderLookupProvider()), is(tagm10));
        assertThat("serializing 0.1 returns 0.1", serialize(o -> d0.getType().serialize(o, d0.getValue()), context.holderLookupProvider()), is(tag0));

        assertThat("deserializing 10.1 returns 10.1", deserialize(tag10, d10.getType()::deserialize, context.holderLookupProvider()), is(d10.getValue()));
        assertThat("deserializing -10.1 returns -10.1", deserialize(tagm10, dm10.getType()::deserialize, context.holderLookupProvider()), is(dm10.getValue()));
        assertThat("deserializing 0.1 returns 0.1", deserialize(tag0, d0.getType()::deserialize, context.holderLookupProvider()), is(d0.getValue()));

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

        CompoundTag tag10 = new CompoundTag();
        tag10.putString("v", "10");
        CompoundTag tagm10 = new CompoundTag();
        tagm10.putString("v", "-10");
        CompoundTag tag0 = new CompoundTag();
        tag0.putString("v", "0");

        assertThat("serializing 10 returns 10", serialize(o -> s10.getType().serialize(o, s10.getValue()), context.holderLookupProvider()), is(tag10));
        assertThat("serializing -10 returns -10", serialize(o -> sm10.getType().serialize(o, sm10.getValue()), context.holderLookupProvider()), is(tagm10));
        assertThat("serializing 0 returns 0", serialize(o -> s0.getType().serialize(o, s0.getValue()), context.holderLookupProvider()), is(tag0));

        assertThat("deserializing 10 returns 10", deserialize(tag10, s10.getType()::deserialize, context.holderLookupProvider()), is(s10.getValue()));
        assertThat("deserializing -10 returns -10", deserialize(tagm10, sm10.getType()::deserialize, context.holderLookupProvider()), is(sm10.getValue()));
        assertThat("deserializing 0 returns 0", deserialize(tag0, s0.getType()::deserialize, context.holderLookupProvider()), is(s0.getValue()));

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
        CompoundTag listString1 = new CompoundTag();
        listString1.putString("v", "a");
        CompoundTag listString2 = new CompoundTag();
        listString2.putString("v", "b");
        listString.add(listString1);
        listString.add(listString2);
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
        listStringNestedSub1.add(listString1);
        listStringNestedSub1.add(listString2);
        tagStringNestedSub1Serialized.put("values", listStringNestedSub1);
        CompoundTag tagStringNestedSub1 = new CompoundTag();
        tagStringNestedSub1.putString("proxyName", "integrateddynamics:materialized");
        tagStringNestedSub1.put("serialized", tagStringNestedSub1Serialized);
        // --> 2
        CompoundTag tagStringNestedSub2Serialized = new CompoundTag();
        tagStringNestedSub2Serialized.putString("valueType", "integrateddynamics:string");
        ListTag listStringNestedSub2 = new ListTag();
        CompoundTag listString3 = new CompoundTag();
        listString3.putString("v", "c");
        CompoundTag listString4 = new CompoundTag();
        listString4.putString("v", "d");
        listStringNestedSub2.add(listString3);
        listStringNestedSub2.add(listString4);
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
        valueHeterogeneous1.putInt("v", 42);
        valueHeterogeneous2.putString("valueType", "integrateddynamics:string");
        valueHeterogeneous2.putString("v", "hello");
        listHeterogeneous.add(valueHeterogeneous1);
        listHeterogeneous.add(valueHeterogeneous2);
        tagHeterogeneousSerialized.put("values", listHeterogeneous);
        CompoundTag tagHeterogeneous = new CompoundTag();
        tagHeterogeneous.putString("proxyName", "integrateddynamics:materialized");
        tagHeterogeneous.put("serialized", tagHeterogeneousSerialized);

        assertThat("serializing empty list",
                serialize(o -> l0.getType().serialize(o, l0.getValue()), context.holderLookupProvider()), is(tagEmpty));
        assertThat("serializing string list",
                serialize(o -> l2.getType().serialize(o, l2.getValue()), context.holderLookupProvider()), is(tagString));
        assertThat("serializing nested list",
                serialize(o -> l2.getType().serialize(o, l2_2.getValue()), context.holderLookupProvider()), is(tagStringNested));
        assertThat("serializing heterogeneous list",
                serialize(o -> l2h.getType().serialize(o, l2h.getValue()), context.holderLookupProvider()), is(tagHeterogeneous));

        assertThat("deserializing empty list",
                deserialize(tagEmpty, l0.getType()::deserialize, context.holderLookupProvider()), is(l0.getValue()));
        assertThat("deserializing string list",
                deserialize(tagString, l2.getType()::deserialize, context.holderLookupProvider()), is(l2.getValue()));
        assertThat("deserializing nested list",
                deserialize(tagStringNested, l2_2.getType()::deserialize, context.holderLookupProvider()), is(l2_2.getValue()));
        assertThat("deserializing heterogeneous list",
                deserialize(tagHeterogeneous, l2h.getType()::deserialize, context.holderLookupProvider()), is(l2h.getValue()));
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

        assertThat("serializing null value returns empty NBT tag", serialize(o -> snull.getType().serialize(o, snull.getValue()), context.holderLookupProvider()), is(new CompoundTag()));
        assertThat("serializing tag returns tag", serialize(o -> tagVariable.getType().serialize(o, tagVariable.getValue()), context.holderLookupProvider()), is(tagWrapped));
        assertThat("serializing string tag returns tag", serialize(o -> tagVariable.getType().serialize(o, strTagVariable.getValue()), context.holderLookupProvider()), is(strTagWrapped));

        assertThat("deserializing null value returns empty NBT tag", deserialize(new CompoundTag(), snull.getType()::deserialize, context.holderLookupProvider()), is(snull.getValue()));
        assertThat("deserializing tag returns tag", deserialize(tagWrapped, tagVariable.getType()::deserialize, context.holderLookupProvider()), is(tagVariable.getValue()));
        assertThat("deserializing string tag returns tag", deserialize(strTagWrapped, strTagVariable.getType()::deserialize, context.holderLookupProvider()), is(strTagVariable.getValue()));

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
