package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test the different integer operators.
 * @author rubensworks
 */
public class TestIntegerOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableInteger i0;
    private DummyVariableInteger i1;
    private DummyVariableInteger im10;
    private DummyVariableInteger i10;
    private DummyVariableInteger i15;

    @Before
    public void before() {
        i0   = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0  ));
        i1   = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1  ));
        im10 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(-10));
        i10  = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(10 ));
        i15  = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(15 ));
    }

    /**
     * ----------------------------------- MODULO -----------------------------------
     */

    @Test
    public void testArithmeticModulo() throws EvaluationException {
        IValue res1 = Operators.INTEGER_MODULUS.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10 % 10 = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));

        IValue res2 = Operators.INTEGER_MODULUS.evaluate(new IVariable[]{i0, i10});
        assertThat("0 % 10 = 0", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(0));

        IValue res3 = Operators.INTEGER_MODULUS.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 % 10 = 0", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(0));

        IValue res4 = Operators.INTEGER_MODULUS.evaluate(new IVariable[]{i10, im10});
        assertThat("10 % -10 = 0", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(0));

        IValue res5 = Operators.INTEGER_MODULUS.evaluate(new IVariable[]{i10, i15});
        assertThat("10 % 15 = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(10));

        IValue res6 = Operators.INTEGER_MODULUS.evaluate(new IVariable[]{i15, i10});
        assertThat("15 % 10 = 1", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(5));
    }

    @Test(expected = EvaluationException.class)
    public void testArithmeticModuloByZero() throws EvaluationException {
        Operators.INTEGER_MODULUS.evaluate(new IVariable[]{i10, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeModuloLarge() throws EvaluationException {
        Operators.INTEGER_MODULUS.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeModuloSmall() throws EvaluationException {
        Operators.INTEGER_MODULUS.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeModulo() throws EvaluationException {
        Operators.INTEGER_MODULUS.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- INCREMENT -----------------------------------
     */

    @Test
    public void testArithmeticIncrement() throws EvaluationException {
        IValue res1 = Operators.INTEGER_INCREMENT.evaluate(new IVariable[]{i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10++ = 11", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(11));

        IValue res2 = Operators.INTEGER_INCREMENT.evaluate(new IVariable[]{i0});
        assertThat("0++ = 1", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(1));

        IValue res3 = Operators.INTEGER_INCREMENT.evaluate(new IVariable[]{im10});
        assertThat("-10++ = -9", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(-9));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIncrementLarge() throws EvaluationException {
        Operators.INTEGER_INCREMENT.evaluate(new IVariable[]{i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIncrementSmall() throws EvaluationException {
        Operators.INTEGER_INCREMENT.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeIncrement() throws EvaluationException {
        Operators.INTEGER_INCREMENT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- DECREMENT -----------------------------------
     */

    @Test
    public void testArithmeticDecrement() throws EvaluationException {
        IValue res1 = Operators.INTEGER_DECREMENT.evaluate(new IVariable[]{i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10-- = 9", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(9));

        IValue res2 = Operators.INTEGER_DECREMENT.evaluate(new IVariable[]{i0});
        assertThat("0-- = -1", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(-1));

        IValue res3 = Operators.INTEGER_DECREMENT.evaluate(new IVariable[]{im10});
        assertThat("-10-- = -11", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(-11));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDecrementLarge() throws EvaluationException {
        Operators.INTEGER_DECREMENT.evaluate(new IVariable[]{i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDecrementSmall() throws EvaluationException {
        Operators.INTEGER_DECREMENT.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeDecrement() throws EvaluationException {
        Operators.INTEGER_DECREMENT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

}
