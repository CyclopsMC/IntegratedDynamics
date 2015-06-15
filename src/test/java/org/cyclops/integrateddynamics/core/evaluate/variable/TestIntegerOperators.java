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

    @Test
    public void testIntegerAdd() throws EvaluationException {
        IValue res1 = Operators.INTEGER_ADDITION.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10 + 10 = 20", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(20));

        IValue res2 = Operators.INTEGER_ADDITION.evaluate(new IVariable[]{i0, i10});
        assertThat("0 + 10 = 10", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(10));

        IValue res3 = Operators.INTEGER_ADDITION.evaluate(new IVariable[]{i10, i0});
        assertThat("10 + 0 = 10", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(10));

        IValue res4 = Operators.INTEGER_ADDITION.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 + 10 = 0", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(0));

        IValue res5 = Operators.INTEGER_ADDITION.evaluate(new IVariable[]{i10, im10});
        assertThat("10 + -10 = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(0));
    }

    @Test
    public void testIntegerMinus() throws EvaluationException {
        IValue res1 = Operators.INTEGER_SUBTRACTION.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10 - 10 = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));

        IValue res2 = Operators.INTEGER_SUBTRACTION.evaluate(new IVariable[]{i0, i10});
        assertThat("0 - 10 = -10", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(-10));

        IValue res3 = Operators.INTEGER_SUBTRACTION.evaluate(new IVariable[]{i10, i0});
        assertThat("10 - 0 = 10", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(10));

        IValue res4 = Operators.INTEGER_SUBTRACTION.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 - 10 = -20", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(-20));

        IValue res5 = Operators.INTEGER_SUBTRACTION.evaluate(new IVariable[]{i10, im10});
        assertThat("10 - -10 = 20", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(20));
    }

    @Test
    public void testIntegerMultiply() throws EvaluationException {
        IValue res1 = Operators.INTEGER_MULTIPLICATION.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10 * 10 = 100", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(100));

        IValue res2 = Operators.INTEGER_MULTIPLICATION.evaluate(new IVariable[]{i10, i0});
        assertThat("10 * 0 = 0", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(0));

        IValue res3 = Operators.INTEGER_MULTIPLICATION.evaluate(new IVariable[]{i10, i1});
        assertThat("10 * 1 = 10", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(10));

        IValue res4 = Operators.INTEGER_MULTIPLICATION.evaluate(new IVariable[]{i0, i10});
        assertThat("0 * 10 = 0", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(0));

        IValue res5 = Operators.INTEGER_MULTIPLICATION.evaluate(new IVariable[]{i1, i10});
        assertThat("1 * 10 = 10", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(10));

        IValue res6 = Operators.INTEGER_MULTIPLICATION.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 * 10 = -100", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(-100));
    }

    @Test
    public void testIntegerDivide() throws EvaluationException {
        IValue res1 = Operators.INTEGER_DIVISION.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10 / 10 = 1", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(1));

        IValue res2 = Operators.INTEGER_DIVISION.evaluate(new IVariable[]{i0, i10});
        assertThat("0 / 10 = 0", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(0));

        IValue res3 = Operators.INTEGER_DIVISION.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 / 10 = -1", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(-1));

        IValue res4 = Operators.INTEGER_DIVISION.evaluate(new IVariable[]{i10, im10});
        assertThat("10 / -10 = -1", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(-1));

        IValue res5 = Operators.INTEGER_DIVISION.evaluate(new IVariable[]{i10, i15});
        assertThat("10 / 15 = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(0));

        IValue res6 = Operators.INTEGER_DIVISION.evaluate(new IVariable[]{i15, i10});
        assertThat("15 / 10 = 1", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(1));
    }

    @Test(expected = EvaluationException.class)
    public void testIntegerDivideByZero() throws EvaluationException {
        Operators.INTEGER_DIVISION.evaluate(new IVariable[]{i10, i0});
    }

    @Test
    public void testIntegerModulo() throws EvaluationException {
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
    public void testIntegerModuloByZero() throws EvaluationException {
        Operators.INTEGER_MODULUS.evaluate(new IVariable[]{i10, i0});
    }

    @Test
    public void testIntegerMax() throws EvaluationException {
        IValue res1 = Operators.INTEGER_MAX.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("max(10, 10) = 10", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(10));

        IValue res2 = Operators.INTEGER_MAX.evaluate(new IVariable[]{i0, i10});
        assertThat("max(0, 10) = 10", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(10));

        IValue res3 = Operators.INTEGER_MAX.evaluate(new IVariable[]{i10, i0});
        assertThat("max(10, 0) = 10", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(10));

        IValue res4 = Operators.INTEGER_MAX.evaluate(new IVariable[]{i0, im10});
        assertThat("max(0, -10) = 0", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(0));

        IValue res5 = Operators.INTEGER_MAX.evaluate(new IVariable[]{im10, i0});
        assertThat("max(-10, 0) = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(0));
    }

    @Test
    public void testIntegerMin() throws EvaluationException {
        IValue res1 = Operators.INTEGER_MIN.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("max(10, 10) = 10", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(10));

        IValue res2 = Operators.INTEGER_MIN.evaluate(new IVariable[]{i0, i10});
        assertThat("max(0, 10) = 10", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(0));

        IValue res3 = Operators.INTEGER_MIN.evaluate(new IVariable[]{i10, i0});
        assertThat("max(10, 0) = 10", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(0));

        IValue res4 = Operators.INTEGER_MIN.evaluate(new IVariable[]{i0, im10});
        assertThat("max(0, -10) = 0", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(-10));

        IValue res5 = Operators.INTEGER_MIN.evaluate(new IVariable[]{im10, i0});
        assertThat("max(-10, 0) = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(-10));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeAddLarge() throws EvaluationException {
        Operators.INTEGER_ADDITION.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeAddSmall() throws EvaluationException {
        Operators.INTEGER_ADDITION.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeAdd() throws EvaluationException {
        Operators.INTEGER_ADDITION.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMinusLarge() throws EvaluationException {
        Operators.INTEGER_SUBTRACTION.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMinusSmall() throws EvaluationException {
        Operators.INTEGER_SUBTRACTION.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeMinus() throws EvaluationException {
        Operators.INTEGER_SUBTRACTION.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMultiplyLarge() throws EvaluationException {
        Operators.INTEGER_MULTIPLICATION.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMultiplySmall() throws EvaluationException {
        Operators.INTEGER_MULTIPLICATION.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeMultiply() throws EvaluationException {
        Operators.INTEGER_MULTIPLICATION.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDivideLarge() throws EvaluationException {
        Operators.INTEGER_DIVISION.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDivideSmall() throws EvaluationException {
        Operators.INTEGER_DIVISION.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeDivide() throws EvaluationException {
        Operators.INTEGER_DIVISION.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
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

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMaxLarge() throws EvaluationException {
        Operators.INTEGER_MAX.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMaxSmall() throws EvaluationException {
        Operators.INTEGER_MAX.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeMax() throws EvaluationException {
        Operators.INTEGER_MAX.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMinLarge() throws EvaluationException {
        Operators.INTEGER_MIN.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMinSmall() throws EvaluationException {
        Operators.INTEGER_MIN.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeMin() throws EvaluationException {
        Operators.INTEGER_MIN.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

}
