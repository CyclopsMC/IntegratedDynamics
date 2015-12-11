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
public class TestArithmeticOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableInteger i0;
    private DummyVariableInteger i1;
    private DummyVariableInteger im10;
    private DummyVariableInteger i10;
    private DummyVariableInteger i15;

    private DummyVariableDouble d0;
    private DummyVariableDouble d1;
    private DummyVariableDouble dm10;
    private DummyVariableDouble d10;
    private DummyVariableDouble d15;

    @Before
    public void before() {
        ValueCastMappings.load();

        i0   = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0  ));
        i1   = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1  ));
        im10 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(-10));
        i10  = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(10 ));
        i15  = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(15 ));

        d0   = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(0  ));
        d1   = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(1  ));
        dm10 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(-10));
        d10  = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(10 ));
        d15  = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(15 ));
    }

    /**
     * ----------------------------------- ADD -----------------------------------
     */

    @Test
    public void testArithmeticAddInteger() throws EvaluationException {
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

    @Test
    public void testArithmeticAddDouble() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{d10, d10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("10 + 10 = 20", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(20D));

        IValue res2 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{d0, d10});
        assertThat("0 + 10 = 10", ((ValueTypeDouble.ValueDouble) res2).getRawValue(), is(10D));

        IValue res3 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{d10, d0});
        assertThat("10 + 0 = 10", ((ValueTypeDouble.ValueDouble) res3).getRawValue(), is(10D));

        IValue res4 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{dm10, d10});
        assertThat("-10 + 10 = 0", ((ValueTypeDouble.ValueDouble) res4).getRawValue(), is(0D));

        IValue res5 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{d10, dm10});
        assertThat("10 + -10 = 0", ((ValueTypeDouble.ValueDouble) res5).getRawValue(), is(0D));
    }

    @Test
    public void testArithmeticAddIntegerDouble() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{i10, d10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("10 + 10 = 20", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(20D));

        IValue res2 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{i0, d10});
        assertThat("0 + 10 = 10", ((ValueTypeDouble.ValueDouble) res2).getRawValue(), is(10D));

        IValue res3 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{i10, d0});
        assertThat("10 + 0 = 10", ((ValueTypeDouble.ValueDouble) res3).getRawValue(), is(10D));

        IValue res4 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{im10, d10});
        assertThat("-10 + 10 = 0", ((ValueTypeDouble.ValueDouble) res4).getRawValue(), is(0D));

        IValue res5 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{i10, dm10});
        assertThat("10 + -10 = 0", ((ValueTypeDouble.ValueDouble) res5).getRawValue(), is(0D));
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
    public void testArithmeticMinusInteger() throws EvaluationException {
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

    @Test
    public void testArithmeticMinusDouble() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{d10, d10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("10 - 10 = 0", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(0D));

        IValue res2 = Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{d0, d10});
        assertThat("0 - 10 = -10", ((ValueTypeDouble.ValueDouble) res2).getRawValue(), is(-10D));

        IValue res3 = Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{d10, d0});
        assertThat("10 - 0 = 10", ((ValueTypeDouble.ValueDouble) res3).getRawValue(), is(10D));

        IValue res4 = Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{dm10, d10});
        assertThat("-10 - 10 = -20", ((ValueTypeDouble.ValueDouble) res4).getRawValue(), is(-20D));

        IValue res5 = Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{d10, dm10});
        assertThat("10 - -10 = 20", ((ValueTypeDouble.ValueDouble) res5).getRawValue(), is(20D));
    }

    @Test
    public void testArithmeticMinusIntegerDouble() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{i10, d10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("10 - 10 = 0", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(0D));

        IValue res2 = Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{i0, d10});
        assertThat("0 - 10 = -10", ((ValueTypeDouble.ValueDouble) res2).getRawValue(), is(-10D));

        IValue res3 = Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{i10, d0});
        assertThat("10 - 0 = 10", ((ValueTypeDouble.ValueDouble) res3).getRawValue(), is(10D));

        IValue res4 = Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{im10, d10});
        assertThat("-10 - 10 = -20", ((ValueTypeDouble.ValueDouble) res4).getRawValue(), is(-20D));

        IValue res5 = Operators.ARITHMETIC_SUBTRACTION.evaluate(new IVariable[]{i10, dm10});
        assertThat("10 - -10 = 20", ((ValueTypeDouble.ValueDouble) res5).getRawValue(), is(20D));
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
     public void testArithmeticMultiplyInteger() throws EvaluationException {
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

    @Test
    public void testArithmeticMultiplyDouble() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{d10, d10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("10 * 10 = 100", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(100D));

        IValue res2 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{d10, d0});
        assertThat("10 * 0 = 0", ((ValueTypeDouble.ValueDouble) res2).getRawValue(), is(0D));

        IValue res3 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{d10, d1});
        assertThat("10 * 1 = 10", ((ValueTypeDouble.ValueDouble) res3).getRawValue(), is(10D));

        IValue res4 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{d0, d10});
        assertThat("0 * 10 = 0", ((ValueTypeDouble.ValueDouble) res4).getRawValue(), is(0D));

        IValue res5 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{d1, d10});
        assertThat("1 * 10 = 10", ((ValueTypeDouble.ValueDouble) res5).getRawValue(), is(10D));

        IValue res6 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{dm10, d10});
        assertThat("-10 * 10 = -100", ((ValueTypeDouble.ValueDouble) res6).getRawValue(), is(-100D));
    }

    @Test
    public void testArithmeticMultiplyIntegerDouble() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{i10, d10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("10 * 10 = 100", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(100D));

        IValue res2 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{i10, d0});
        assertThat("10 * 0 = 0", ((ValueTypeDouble.ValueDouble) res2).getRawValue(), is(0D));

        IValue res3 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{i10, d1});
        assertThat("10 * 1 = 10", ((ValueTypeDouble.ValueDouble) res3).getRawValue(), is(10D));

        IValue res4 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{i0, d10});
        assertThat("0 * 10 = 0", ((ValueTypeDouble.ValueDouble) res4).getRawValue(), is(0D));

        IValue res5 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{i1, d10});
        assertThat("1 * 10 = 10", ((ValueTypeDouble.ValueDouble) res5).getRawValue(), is(10D));

        IValue res6 = Operators.ARITHMETIC_MULTIPLICATION.evaluate(new IVariable[]{im10, d10});
        assertThat("-10 * 10 = -100", ((ValueTypeDouble.ValueDouble) res6).getRawValue(), is(-100D));
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
    public void testArithmeticDivideInteger() throws EvaluationException {
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

    @Test
    public void testArithmeticDivideDouble() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{d10, d10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("10 / 10 = 1", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(1D));

        IValue res2 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{d0, d10});
        assertThat("0 / 10 = 0", ((ValueTypeDouble.ValueDouble) res2).getRawValue(), is(0D));

        IValue res3 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{dm10, d10});
        assertThat("-10 / 10 = -1", ((ValueTypeDouble.ValueDouble) res3).getRawValue(), is(-1D));

        IValue res4 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{d10, dm10});
        assertThat("10 / -10 = -1", ((ValueTypeDouble.ValueDouble) res4).getRawValue(), is(-1D));

        IValue res5 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{d10, d15});
        assertThat("10 / 15 = 0.666...", ((ValueTypeDouble.ValueDouble) res5).getRawValue(), is(10D / 15D));

        IValue res6 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{d15, d10});
        assertThat("15 / 10 = 1.5", ((ValueTypeDouble.ValueDouble) res6).getRawValue(), is(1.5D));
    }

    @Test
    public void testArithmeticDivideIntegerDouble() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{i10, d10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("10 / 10 = 1", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(1D));

        IValue res2 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{i0, d10});
        assertThat("0 / 10 = 0", ((ValueTypeDouble.ValueDouble) res2).getRawValue(), is(0D));

        IValue res3 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{im10, d10});
        assertThat("-10 / 10 = -1", ((ValueTypeDouble.ValueDouble) res3).getRawValue(), is(-1D));

        IValue res4 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{i10, dm10});
        assertThat("10 / -10 = -1", ((ValueTypeDouble.ValueDouble) res4).getRawValue(), is(-1D));

        IValue res5 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{i10, d15});
        assertThat("10 / 15 = 0.666...", ((ValueTypeDouble.ValueDouble) res5).getRawValue(), is(10D / 15D));

        IValue res6 = Operators.ARITHMETIC_DIVISION.evaluate(new IVariable[]{i15, d10});
        assertThat("15 / 10 = 1.5", ((ValueTypeDouble.ValueDouble) res6).getRawValue(), is(1.5D));
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
     * ----------------------------------- MAX -----------------------------------
     */

    @Test
    public void testArithmeticMaxInteger() throws EvaluationException {
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

    @Test
    public void testArithmeticMaxDouble() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{d10, d10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("max(10, 10) = 10", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(10D));

        IValue res2 = Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{d0, d10});
        assertThat("max(0, 10) = 10", ((ValueTypeDouble.ValueDouble) res2).getRawValue(), is(10D));

        IValue res3 = Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{d10, d0});
        assertThat("max(10, 0) = 10", ((ValueTypeDouble.ValueDouble) res3).getRawValue(), is(10D));

        IValue res4 = Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{d0, dm10});
        assertThat("max(0, -10) = 0", ((ValueTypeDouble.ValueDouble) res4).getRawValue(), is(0D));

        IValue res5 = Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{dm10, d0});
        assertThat("max(-10, 0) = 0", ((ValueTypeDouble.ValueDouble) res5).getRawValue(), is(0D));
    }

    @Test
    public void testArithmeticMaxIntegerDouble() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{i10, d10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("max(10, 10) = 10", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(10D));

        IValue res2 = Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{i0, d10});
        assertThat("max(0, 10) = 10", ((ValueTypeDouble.ValueDouble) res2).getRawValue(), is(10D));

        IValue res3 = Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{i10, d0});
        assertThat("max(10, 0) = 10", ((ValueTypeDouble.ValueDouble) res3).getRawValue(), is(10D));

        IValue res4 = Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{i0, dm10});
        assertThat("max(0, -10) = 0", ((ValueTypeDouble.ValueDouble) res4).getRawValue(), is(0D));

        IValue res5 = Operators.ARITHMETIC_MAXIMUM.evaluate(new IVariable[]{im10, d0});
        assertThat("max(-10, 0) = 0", ((ValueTypeDouble.ValueDouble) res5).getRawValue(), is(0D));
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
    public void testArithmeticMinInteger() throws EvaluationException {
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

    @Test
    public void testArithmeticMinDouble() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{d10, d10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("max(10, 10) = 10", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(10D));

        IValue res2 = Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{d0, d10});
        assertThat("max(0, 10) = 10", ((ValueTypeDouble.ValueDouble) res2).getRawValue(), is(0D));

        IValue res3 = Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{d10, d0});
        assertThat("max(10, 0) = 10", ((ValueTypeDouble.ValueDouble) res3).getRawValue(), is(0D));

        IValue res4 = Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{d0, dm10});
        assertThat("max(0, -10) = 0", ((ValueTypeDouble.ValueDouble) res4).getRawValue(), is(-10D));

        IValue res5 = Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{dm10, d0});
        assertThat("max(-10, 0) = 0", ((ValueTypeDouble.ValueDouble) res5).getRawValue(), is(-10D));
    }

    @Test
    public void testArithmeticMinIntegerDouble() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{i10, d10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("max(10, 10) = 10", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(10D));

        IValue res2 = Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{i0, d10});
        assertThat("max(0, 10) = 10", ((ValueTypeDouble.ValueDouble) res2).getRawValue(), is(0D));

        IValue res3 = Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{i10, d0});
        assertThat("max(10, 0) = 10", ((ValueTypeDouble.ValueDouble) res3).getRawValue(), is(0D));

        IValue res4 = Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{i0, dm10});
        assertThat("max(0, -10) = 0", ((ValueTypeDouble.ValueDouble) res4).getRawValue(), is(-10D));

        IValue res5 = Operators.ARITHMETIC_MINIMUM.evaluate(new IVariable[]{im10, d0});
        assertThat("max(-10, 0) = 0", ((ValueTypeDouble.ValueDouble) res5).getRawValue(), is(-10D));
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

}
