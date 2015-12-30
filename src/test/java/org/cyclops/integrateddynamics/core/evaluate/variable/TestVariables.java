package org.cyclops.integrateddynamics.core.evaluate.variable;

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
    public void testBooleanType() {
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
        assertThat("serializing false returns false", bFalse.getType().serialize(bFalse.getValue()), is("false"));
        assertThat("serializing true returns true", bTrue.getType().serialize(bTrue.getValue()), is("true"));

        assertThat("deserializing false returns false", bFalse.getType().deserialize("false"), is(bFalse.getValue()));
        assertThat("deserializing true returns true", bTrue.getType().deserialize("true"), is(bTrue.getValue()));
    }

    @Test
    public void testIntegerType() {
        DummyVariableInteger i0 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0));
        assertThat("0 value is 0", i0.getValue().getRawValue(), is(0));

        DummyVariableInteger im10 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(-10));
        assertThat("-10 value is -10", im10.getValue().getRawValue(), is(-10));

        DummyVariableInteger i10 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(10));
        assertThat("10 value is 10", i10.getValue().getRawValue(), is(10));

        assertThat("serializing 10 returns 10", i10.getType().serialize(i10.getValue()), is("10"));
        assertThat("serializing -10 returns -10", im10.getType().serialize(im10.getValue()), is("-10"));
        assertThat("serializing 0 returns 0", i0.getType().serialize(i0.getValue()), is("0"));

        assertThat("deserializing 10 returns 10", i10.getType().deserialize("10"), is(i10.getValue()));
        assertThat("deserializing -10 returns -10", im10.getType().deserialize("-10"), is(im10.getValue()));
        assertThat("deserializing 0 returns 0", i0.getType().deserialize("0"), is(i0.getValue()));
    }

    @Test
    public void testDoubleType() {
        DummyVariableDouble d0 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(0.1));
        assertThat("0.1 value is 0.1", d0.getValue().getRawValue(), is(0.1));

        DummyVariableDouble dm10 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(-10.1));
        assertThat("-10.1 value is -10.1", dm10.getValue().getRawValue(), is(-10.1));

        DummyVariableDouble d10 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(10.1));
        assertThat("10.1 value is 10.1", d10.getValue().getRawValue(), is(10.1));

        assertThat("serializing 10.1 returns 10.1", d10.getType().serialize(d10.getValue()), is("10.1"));
        assertThat("serializing -10.1 returns -10.1", dm10.getType().serialize(dm10.getValue()), is("-10.1"));
        assertThat("serializing 0.1 returns 0.1", d0.getType().serialize(d0.getValue()), is("0.1"));

        assertThat("deserializing 10.1 returns 10.1", d10.getType().deserialize("10.1"), is(d10.getValue()));
        assertThat("deserializing -10.1 returns -10.1", dm10.getType().deserialize("-10.1"), is(dm10.getValue()));
        assertThat("deserializing 0.1 returns 0.1", d0.getType().deserialize("0.1"), is(d0.getValue()));
    }

    @Test
    public void testStringType() {
        DummyVariableString s0 = new DummyVariableString(ValueTypeString.ValueString.of("0"));
        assertThat("0 value is 0", s0.getValue().getRawValue(), is("0"));

        DummyVariableString sm10 = new DummyVariableString(ValueTypeString.ValueString.of("-10"));
        assertThat("-10 value is -10", sm10.getValue().getRawValue(), is("-10"));

        DummyVariableString s10 = new DummyVariableString(ValueTypeString.ValueString.of("10"));
        assertThat("10 value is 10", s10.getValue().getRawValue(), is("10"));

        assertThat("serializing 10 returns 10", s10.getType().serialize(s10.getValue()), is("10"));
        assertThat("serializing -10 returns -10", sm10.getType().serialize(sm10.getValue()), is("-10"));
        assertThat("serializing 0 returns 0", s0.getType().serialize(s0.getValue()), is("0"));

        assertThat("deserializing 10 returns 10", s10.getType().deserialize("10"), is(s10.getValue()));
        assertThat("deserializing -10 returns -10", sm10.getType().deserialize("-10"), is(sm10.getValue()));
        assertThat("deserializing 0 returns 0", s0.getType().deserialize("0"), is(s0.getValue()));
    }

    @Test
    public void testListTypeMaterialized() {
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

        assertThat("serializing empty list",
                l0.getType().serialize(l0.getValue()), is("materialized;valuetype.valuetypes.integrateddynamics.any.name"));
        assertThat("serializing string list",
                l2.getType().serialize(l2.getValue()), is("materialized;valuetype.valuetypes.integrateddynamics.string.name\\;a\\;b"));
        assertThat("serializing nested list",
                l2.getType().serialize(l2_2.getValue()), is("materialized;valuetype.valuetypes.integrateddynamics.object.list.name\\;materialized\\\\;valuetype.valuetypes.integrateddynamics.string.name\\\\\\;a\\\\\\;b\\;materialized\\\\;valuetype.valuetypes.integrateddynamics.string.name\\\\\\;c\\\\\\;d"));

        assertThat("deserializing empty list",
                l0.getType().deserialize(l0.getType().serialize(l0.getValue())), is(l0.getValue()));
        assertThat("deserializing string list",
                l2.getType().deserialize(l2.getType().serialize(l2.getValue())), is(l2.getValue()));
        assertThat("deserializing nested list",
                l2_2.getType().deserialize(l2_2.getType().serialize(l2_2.getValue())), is(l2_2.getValue()));
    }

}
