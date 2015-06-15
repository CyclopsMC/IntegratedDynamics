package org.cyclops.integrateddynamics.core.evaluate.variable;

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
    }

    @Test
    public void testIntegerType() {
        DummyVariableInteger i0 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0));
        assertThat("0 value is 0", i0.getValue().getRawValue(), is(0));

        DummyVariableInteger im10 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(-10));
        assertThat("-10 value is -10", im10.getValue().getRawValue(), is(-10));

        DummyVariableInteger i10 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(10));
        assertThat("10 value is 10", i10.getValue().getRawValue(), is(10));
    }

}
