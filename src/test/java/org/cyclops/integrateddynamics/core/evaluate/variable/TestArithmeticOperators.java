package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.core.evaluate.EvaluationException;
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
public class TestArithmeticOperators {

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
     * ----------------------------------- ADD -----------------------------------
     */

    @Test
    public void testArithmeticAdd() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10 + 10 = 20", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(20));

        IValue res2 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{i0, i10});
        assertThat("0 + 10 = 10", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(10));

        IValue res3 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{i10, i0});
        assertThat("10 + 0 = 10", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(10));

        IValue res4 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 + 10 = 0", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(0));

        IValue res5 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{i10, im10});
        assertThat("10 + -10 = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(0));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeAddLarge() throws EvaluationException {
        Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeAddSmall() throws EvaluationException {
        Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeAdd() throws EvaluationException {
        Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- MINUS -----------------------------------
     */

    @Test
    public void testArithmeticMinus() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10 - 10 = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));

        IValue res2 = Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{i0, i10});
        assertThat("0 - 10 = -10", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(-10));

        IValue res3 = Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{i10, i0});
        assertThat("10 - 0 = 10", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(10));

        IValue res4 = Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 - 10 = -20", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(-20));

        IValue res5 = Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{i10, im10});
        assertThat("10 - -10 = 20", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(20));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMinusLarge() throws EvaluationException {
        Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMinusSmall() throws EvaluationException {
        Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeMinus() throws EvaluationException {
        Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- MULTIPLY -----------------------------------
     */

    @Test
    public void testArithmeticMultiply() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10 * 10 = 100", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(100));

        IValue res2 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{i10, i0});
        assertThat("10 * 0 = 0", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(0));

        IValue res3 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{i10, i1});
        assertThat("10 * 1 = 10", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(10));

        IValue res4 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{i0, i10});
        assertThat("0 * 10 = 0", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(0));

        IValue res5 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{i1, i10});
        assertThat("1 * 10 = 10", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(10));

        IValue res6 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 * 10 = -100", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(-100));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMultiplyLarge() throws EvaluationException {
        Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMultiplySmall() throws EvaluationException {
        Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeMultiply() throws EvaluationException {
        Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- DIVIDE -----------------------------------
     */

    @Test
    public void testArithmeticDivide() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10 / 10 = 1", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(1));

        IValue res2 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{i0, i10});
        assertThat("0 / 10 = 0", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(0));

        IValue res3 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 / 10 = -1", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(-1));

        IValue res4 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{i10, im10});
        assertThat("10 / -10 = -1", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(-1));

        IValue res5 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{i10, i15});
        assertThat("10 / 15 = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(0));

        IValue res6 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{i15, i10});
        assertThat("15 / 10 = 1", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(1));
    }

    @Test(expected = EvaluationException.class)
    public void testArithmeticDivideByZero() throws EvaluationException {
        Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{i10, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDivideLarge() throws EvaluationException {
        Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDivideSmall() throws EvaluationException {
        Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeDivide() throws EvaluationException {
        Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- MODULO -----------------------------------
     */

    @Test
    public void testArithmeticModulo() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_MODULUS.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10 % 10 = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));

        IValue res2 = Operators.ARITHMETIC_MODULUS.evaluate(new IVariable[]{i0, i10});
        assertThat("0 % 10 = 0", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(0));

        IValue res3 = Operators.ARITHMETIC_MODULUS.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 % 10 = 0", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(0));

        IValue res4 = Operators.ARITHMETIC_MODULUS.evaluate(new IVariable[]{i10, im10});
        assertThat("10 % -10 = 0", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(0));

        IValue res5 = Operators.ARITHMETIC_MODULUS.evaluate(new IVariable[]{i10, i15});
        assertThat("10 % 15 = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(10));

        IValue res6 = Operators.ARITHMETIC_MODULUS.evaluate(new IVariable[]{i15, i10});
        assertThat("15 % 10 = 1", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(5));
    }

    @Test(expected = EvaluationException.class)
    public void testArithmeticModuloByZero() throws EvaluationException {
        Operators.ARITHMETIC_MODULUS.evaluate(new IVariable[]{i10, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeModuloLarge() throws EvaluationException {
        Operators.ARITHMETIC_MODULUS.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeModuloSmall() throws EvaluationException {
        Operators.ARITHMETIC_MODULUS.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeModulo() throws EvaluationException {
        Operators.ARITHMETIC_MODULUS.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- MAX -----------------------------------
     */

    @Test
    public void testArithmeticMax() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("max(10, 10) = 10", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(10));

        IValue res2 = Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{i0, i10});
        assertThat("max(0, 10) = 10", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(10));

        IValue res3 = Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{i10, i0});
        assertThat("max(10, 0) = 10", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(10));

        IValue res4 = Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{i0, im10});
        assertThat("max(0, -10) = 0", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(0));

        IValue res5 = Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{im10, i0});
        assertThat("max(-10, 0) = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(0));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMaxLarge() throws EvaluationException {
        Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMaxSmall() throws EvaluationException {
        Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeMax() throws EvaluationException {
        Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- MIN -----------------------------------
     */

    @Test
    public void testArithmeticMin() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("max(10, 10) = 10", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(10));

        IValue res2 = Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{i0, i10});
        assertThat("max(0, 10) = 10", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(0));

        IValue res3 = Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{i10, i0});
        assertThat("max(10, 0) = 10", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(0));

        IValue res4 = Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{i0, im10});
        assertThat("max(0, -10) = 0", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(-10));

        IValue res5 = Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{im10, i0});
        assertThat("max(-10, 0) = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(-10));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMinLarge() throws EvaluationException {
        Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMinSmall() throws EvaluationException {
        Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeMin() throws EvaluationException {
        Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- INCREMENT -----------------------------------
     */

    @Test
    public void testArithmeticIncrement() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_INCREMENT.evaluate(new IVariable[]{i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10++ = 11", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(11));

        IValue res2 = Operators.ARITHMETIC_INCREMENT.evaluate(new IVariable[]{i0});
        assertThat("0++ = 1", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(1));

        IValue res3 = Operators.ARITHMETIC_INCREMENT.evaluate(new IVariable[]{im10});
        assertThat("-10++ = -9", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(-9));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIncrementLarge() throws EvaluationException {
        Operators.ARITHMETIC_INCREMENT.evaluate(new IVariable[]{i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIncrementSmall() throws EvaluationException {
        Operators.ARITHMETIC_INCREMENT.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeIncrement() throws EvaluationException {
        Operators.ARITHMETIC_INCREMENT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- DECREMENT -----------------------------------
     */

    @Test
    public void testArithmeticDecrement() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_DECREMENT.evaluate(new IVariable[]{i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10-- = 9", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(9));

        IValue res2 = Operators.ARITHMETIC_DECREMENT.evaluate(new IVariable[]{i0});
        assertThat("0-- = -1", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(-1));

        IValue res3 = Operators.ARITHMETIC_DECREMENT.evaluate(new IVariable[]{im10});
        assertThat("-10-- = -11", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(-11));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDecrementLarge() throws EvaluationException {
        Operators.ARITHMETIC_DECREMENT.evaluate(new IVariable[]{i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDecrementSmall() throws EvaluationException {
        Operators.ARITHMETIC_DECREMENT.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeDecrement() throws EvaluationException {
        Operators.ARITHMETIC_DECREMENT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

}
