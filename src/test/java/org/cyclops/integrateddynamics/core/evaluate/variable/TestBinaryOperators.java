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
 * Test the different binary operators.
 * @author rubensworks
 */
public class TestBinaryOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableInteger i0;
    private DummyVariableInteger i1;
    private DummyVariableInteger i2;
    private DummyVariableInteger i3;
    private DummyVariableInteger i5;
    private DummyVariableInteger im10;
    private DummyVariableInteger i10;
    private DummyVariableInteger i15;

    @Before
    public void before() {
        i0   = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0  ));
        i1   = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1  ));
        i2   = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(2  ));
        i3   = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(3  ));
        i5   = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(5  ));
        im10 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(-10));
        i10  = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(10 ));
        i15  = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(15 ));
    }

    /**
     * ----------------------------------- AND -----------------------------------
     */

    @Test
    public void testBinaryAnd() throws EvaluationException {
        IValue res1 = Operators.BINARY_AND.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10 & 10 = 10", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(10));

        IValue res2 = Operators.BINARY_AND.evaluate(new IVariable[]{i0, i10});
        assertThat("0 & 10 = 0", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(0));

        IValue res3 = Operators.BINARY_AND.evaluate(new IVariable[]{i10, i0});
        assertThat("10 & 0 = 0", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(0));

        IValue res4 = Operators.BINARY_AND.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 & 10 = 2", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(2));

        IValue res5 = Operators.BINARY_AND.evaluate(new IVariable[]{i10, im10});
        assertThat("10 & -10 = 2", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(2));

        IValue res6 = Operators.BINARY_AND.evaluate(new IVariable[]{i1, i2});
        assertThat("1 & 2 = 0", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(0));

        IValue res7 = Operators.BINARY_AND.evaluate(new IVariable[]{i5, i3});
        assertThat("5 & 3 = 1", ((ValueTypeInteger.ValueInteger) res7).getRawValue(), is(1));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeAndLarge() throws EvaluationException {
        Operators.BINARY_AND.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeAndSmall() throws EvaluationException {
        Operators.BINARY_AND.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeAnd() throws EvaluationException {
        Operators.BINARY_AND.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- OR -----------------------------------
     */

    @Test
    public void testBinaryOr() throws EvaluationException {
        IValue res1 = Operators.BINARY_OR.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10 | 10 = 10", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(10));

        IValue res2 = Operators.BINARY_OR.evaluate(new IVariable[]{i0, i10});
        assertThat("0 | 10 = 10", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(10));

        IValue res3 = Operators.BINARY_OR.evaluate(new IVariable[]{i10, i0});
        assertThat("10 | 0 = 10", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(10));

        IValue res4 = Operators.BINARY_OR.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 | 10 = -2", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(-2));

        IValue res5 = Operators.BINARY_OR.evaluate(new IVariable[]{i10, im10});
        assertThat("10 | -10 = -2", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(-2));

        IValue res6 = Operators.BINARY_OR.evaluate(new IVariable[]{i1, i2});
        assertThat("1 | 2 = 3", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(3));

        IValue res7 = Operators.BINARY_OR.evaluate(new IVariable[]{i5, i3});
        assertThat("5 | 3 = 7", ((ValueTypeInteger.ValueInteger) res7).getRawValue(), is(7));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeOrLarge() throws EvaluationException {
        Operators.BINARY_OR.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeOrSmall() throws EvaluationException {
        Operators.BINARY_OR.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeOr() throws EvaluationException {
        Operators.BINARY_OR.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- XOR -----------------------------------
     */

    @Test
    public void testBinaryXor() throws EvaluationException {
        IValue res1 = Operators.BINARY_XOR.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10 ^ 10 = 10", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));

        IValue res2 = Operators.BINARY_XOR.evaluate(new IVariable[]{i0, i10});
        assertThat("0 ^ 10 = 10", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(10));

        IValue res3 = Operators.BINARY_XOR.evaluate(new IVariable[]{i10, i0});
        assertThat("10 ^ 0 = 10", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(10));

        IValue res4 = Operators.BINARY_XOR.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 ^ 10 = -4", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(-4));

        IValue res5 = Operators.BINARY_XOR.evaluate(new IVariable[]{i10, im10});
        assertThat("10 ^ -10 = -4", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(-4));

        IValue res6 = Operators.BINARY_XOR.evaluate(new IVariable[]{i1, i2});
        assertThat("1 ^ 2 = 3", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(3));

        IValue res7 = Operators.BINARY_XOR.evaluate(new IVariable[]{i5, i3});
        assertThat("5 ^ 3 = 6", ((ValueTypeInteger.ValueInteger) res7).getRawValue(), is(6));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeXorLarge() throws EvaluationException {
        Operators.BINARY_XOR.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeXorSmall() throws EvaluationException {
        Operators.BINARY_XOR.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeXor() throws EvaluationException {
        Operators.BINARY_XOR.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- COMPLEMENT -----------------------------------
     */

    @Test
    public void testBinaryComplement() throws EvaluationException {
        IValue res1 = Operators.BINARY_COMPLEMENT.evaluate(new IVariable[]{i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("~10 = -11", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(-11));

        IValue res2 = Operators.BINARY_COMPLEMENT.evaluate(new IVariable[]{im10});
        assertThat("~-10 = 9", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(9));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeComplementLarge() throws EvaluationException {
        Operators.BINARY_COMPLEMENT.evaluate(new IVariable[]{i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeComplementSmall() throws EvaluationException {
        Operators.BINARY_COMPLEMENT.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeComplement() throws EvaluationException {
        Operators.BINARY_COMPLEMENT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- LSHIFT -----------------------------------
     */

    @Test
    public void testBinaryLShift() throws EvaluationException {
        IValue res1 = Operators.BINARY_LSHIFT.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10 << 10 = 10240", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(10240));

        IValue res2 = Operators.BINARY_LSHIFT.evaluate(new IVariable[]{i0, i10});
        assertThat("0 << 10 = 0", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(0));

        IValue res3 = Operators.BINARY_LSHIFT.evaluate(new IVariable[]{i10, i0});
        assertThat("10 << 0 = 10", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(10));

        IValue res4 = Operators.BINARY_LSHIFT.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 << 10 = -10240", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(-10240));

        IValue res5 = Operators.BINARY_LSHIFT.evaluate(new IVariable[]{i10, im10});
        assertThat("10 << -10 = 41943040", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(41943040));

        IValue res6 = Operators.BINARY_LSHIFT.evaluate(new IVariable[]{i1, i2});
        assertThat("1 << 2 = 4", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(4));

        IValue res7 = Operators.BINARY_LSHIFT.evaluate(new IVariable[]{i5, i3});
        assertThat("5 << 3 = 40", ((ValueTypeInteger.ValueInteger) res7).getRawValue(), is(40));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLShiftLarge() throws EvaluationException {
        Operators.BINARY_LSHIFT.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLShiftSmall() throws EvaluationException {
        Operators.BINARY_LSHIFT.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeLShift() throws EvaluationException {
        Operators.BINARY_LSHIFT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- RSHIFT -----------------------------------
     */

    @Test
    public void testBinaryRShift() throws EvaluationException {
        IValue res1 = Operators.BINARY_RSHIFT.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10 >> 10 = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));

        IValue res2 = Operators.BINARY_RSHIFT.evaluate(new IVariable[]{i0, i10});
        assertThat("0 >> 10 = 0", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(0));

        IValue res3 = Operators.BINARY_RSHIFT.evaluate(new IVariable[]{i10, i0});
        assertThat("10 >> 0 = 10", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(10));

        IValue res4 = Operators.BINARY_RSHIFT.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 >> 10 = -1", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(-1));

        IValue res5 = Operators.BINARY_RSHIFT.evaluate(new IVariable[]{i10, im10});
        assertThat("10 >> -10 = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(0));

        IValue res6 = Operators.BINARY_RSHIFT.evaluate(new IVariable[]{i1, i2});
        assertThat("1 >> 2 = 0", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(0));

        IValue res7 = Operators.BINARY_RSHIFT.evaluate(new IVariable[]{i5, i3});
        assertThat("5 >> 3 = 0", ((ValueTypeInteger.ValueInteger) res7).getRawValue(), is(0));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeRShiftLarge() throws EvaluationException {
        Operators.BINARY_RSHIFT.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeRShiftSmall() throws EvaluationException {
        Operators.BINARY_RSHIFT.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeRShift() throws EvaluationException {
        Operators.BINARY_RSHIFT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- RZSHIFT -----------------------------------
     */

    @Test
    public void testBinaryRZShift() throws EvaluationException {
        IValue res1 = Operators.BINARY_RZSHIFT.evaluate(new IVariable[]{i10, i10});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("10 >>> 10 = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));

        IValue res2 = Operators.BINARY_RZSHIFT.evaluate(new IVariable[]{i0, i10});
        assertThat("0 >>> 10 = 0", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(0));

        IValue res3 = Operators.BINARY_RZSHIFT.evaluate(new IVariable[]{i10, i0});
        assertThat("10 >>> 0 = 10", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(10));

        IValue res4 = Operators.BINARY_RZSHIFT.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 >>> 10 = 4194303", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(4194303));

        IValue res5 = Operators.BINARY_RZSHIFT.evaluate(new IVariable[]{i10, im10});
        assertThat("10 >>> -10 = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(0));

        IValue res6 = Operators.BINARY_RZSHIFT.evaluate(new IVariable[]{i1, i2});
        assertThat("1 >>> 2 = 0", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(0));

        IValue res7 = Operators.BINARY_RZSHIFT.evaluate(new IVariable[]{i5, i3});
        assertThat("5 >>> 3 = 0", ((ValueTypeInteger.ValueInteger) res7).getRawValue(), is(0));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeRZShiftLarge() throws EvaluationException {
        Operators.BINARY_RZSHIFT.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeRZShiftSmall() throws EvaluationException {
        Operators.BINARY_RZSHIFT.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeRZShift() throws EvaluationException {
        Operators.BINARY_RZSHIFT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

}
