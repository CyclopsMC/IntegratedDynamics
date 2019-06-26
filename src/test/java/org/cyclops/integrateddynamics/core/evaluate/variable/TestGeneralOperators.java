package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.junit.Assert.assertThat;

/**
 * Test the different general operators.
 * @author rubensworks
 */
public class TestGeneralOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableBoolean bTrue;
    private DummyVariableBoolean bFalse;

    @Before
    public void before() {
        bTrue = new DummyVariableBoolean(ValueTypeBoolean.ValueBoolean.of(true));
        bFalse = new DummyVariableBoolean(ValueTypeBoolean.ValueBoolean.of(false));
    }

    /**
     * ----------------------------------- CHOICE -----------------------------------
     */

    @Test
    public void testLogicalChoice() throws EvaluationException {
        DummyVariableInteger i1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1));
        DummyVariableInteger i2 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(2));

        IValue res1 = Operators.GENERAL_CHOICE.evaluate(new IVariable[]{bTrue, i1, i2});
        assertThat("result is 1", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("true ? 1 : 2 = 1", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(1));

        IValue res2 = Operators.GENERAL_CHOICE.evaluate(new IVariable[]{bFalse, i1, i2});
        assertThat("false ? 1 : 2 = 1", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeChoiceLarge() throws EvaluationException {
        Operators.GENERAL_CHOICE.evaluate(new IVariable[]{bTrue, bTrue, bTrue, bTrue});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeChoiceSmall() throws EvaluationException {
        Operators.GENERAL_CHOICE.evaluate(new IVariable[]{bTrue, bTrue});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeChoice() throws EvaluationException {
        Operators.GENERAL_CHOICE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidLogicalChoiceDifferentTypes() throws EvaluationException {
        DummyVariableInteger i1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1));
        Operators.GENERAL_CHOICE.evaluate(new IVariable[]{bFalse, i1, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- IDENTITY -----------------------------------
     */

    @Test
    public void testLogicalIdentity() throws EvaluationException {
        DummyVariableInteger i1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1));

        IValue res1 = Operators.GENERAL_IDENTITY.evaluate(new IVariable[]{i1});
        assertThat("result is 1", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("1 = 1", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(1));

        IValue res2 = Operators.GENERAL_IDENTITY.evaluate(new IVariable[]{bFalse});
        assertThat("false = false", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIdentityLarge() throws EvaluationException {
        Operators.GENERAL_IDENTITY.evaluate(new IVariable[]{bTrue, bTrue});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIdentitySmall() throws EvaluationException {
        Operators.GENERAL_IDENTITY.evaluate(new IVariable[]{});
    }

    /**
     * ----------------------------------- CONSTANT -----------------------------------
     */

    @Test
    public void testLogicalConstant() throws EvaluationException {
        DummyVariableInteger i1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1));

        IValue res1 = Operators.GENERAL_CONSTANT.evaluate(new IVariable[]{i1, bFalse});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("K 1 false = 1", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(1));

        IValue res2 = Operators.GENERAL_CONSTANT.evaluate(new IVariable[]{bFalse, i1});
        assertThat("result is a boolean", res2, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("K false 1 = false", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeConstantLarge() throws EvaluationException {
        Operators.GENERAL_CONSTANT.evaluate(new IVariable[]{bTrue, bTrue, bTrue});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeConstantSmall() throws EvaluationException {
        Operators.GENERAL_CONSTANT.evaluate(new IVariable[]{bTrue});
    }

    /**
     * ----------------------------------- ISNULL -----------------------------------
     */

    @Test
    public void testLogicalIsNull() throws EvaluationException {
        DummyVariableInteger i1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1));

        IValue res1 = Operators.NULLABLE_ISNULL.evaluate(new IVariable[]{i1});
        assertThat("result is false", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(false));

        IValue res2 = Operators.NULLABLE_ISNULL.evaluate(new IVariable[]{bFalse});
        assertThat("result is false", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIsNullLarge() throws EvaluationException {
        Operators.NULLABLE_ISNULL.evaluate(new IVariable[]{bTrue, bTrue});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIsNullSmall() throws EvaluationException {
        Operators.NULLABLE_ISNULL.evaluate(new IVariable[]{});
    }

    /**
     * ----------------------------------- ISNOTNULL -----------------------------------
     */

    @Test
    public void testLogicalIsNotNull() throws EvaluationException {
        DummyVariableInteger i1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1));

        IValue res1 = Operators.NULLABLE_ISNOTNULL.evaluate(new IVariable[]{i1});
        assertThat("result is true", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));

        IValue res2 = Operators.NULLABLE_ISNOTNULL.evaluate(new IVariable[]{bFalse});
        assertThat("result is true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIsNotNullLarge() throws EvaluationException {
        Operators.NULLABLE_ISNOTNULL.evaluate(new IVariable[]{bTrue, bTrue});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIsNotNullSmall() throws EvaluationException {
        Operators.NULLABLE_ISNOTNULL.evaluate(new IVariable[]{});
    }

    /**
     * ----------------------------------- ABS -----------------------------------
     */
    @Test
    public void testAbsolute() throws  EvaluationException {
        DummyVariableInteger i1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1));
        DummyVariableInteger i2 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(-2));

        IValue res1 = Operators.NUMBER_ABSOLUTE.evaluate(new IVariable[]{i1});
        assertThat("positive numbers unchanged", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(1));

        IValue res2 = Operators.NUMBER_ABSOLUTE.evaluate(new IVariable[]{i2});
        assertThat("negative numbers changed", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(2));
    }

    @Test
    public void testAbsoluteDouble() throws EvaluationException {
        DummyVariableDouble d1 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(-3.14));

        IValue res1 = Operators.NUMBER_ABSOLUTE.evaluate(new IVariable[]{d1});
        assertThat("negative doubles changed", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(3.14));
    }

    /**
     * ----------------------------------- RANDOM -----------------------------------
     */

    @Test
    public void testRandZeroMax() throws EvaluationException {
        DummyVariableInteger iMax = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(10));

        IValue res1 = Operators.RANDOM_ZERO_MAX.evaluate(new IVariable[]{iMax});
        int result = ((ValueTypeInteger.ValueInteger) res1).getRawValue();
        boolean inRange = result >= 0 && result < 10;
        assertThat("number in rand range", inRange, is(true));
    }

    @Test(expected = EvaluationException.class)
    public void testRandMaxOfZero() throws EvaluationException {
        DummyVariableInteger iMax = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0));

        Operators.RANDOM_ZERO_MAX.evaluate(new IVariable[]{iMax});
    }

    @Test
    public void testRandMinMax() throws EvaluationException {
        DummyVariableInteger iMin = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0));
        DummyVariableInteger iMax = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(10));

        IValue res1 = Operators.RANDOM_MIN_MAX.evaluate(new IVariable[]{iMin,iMax});
        int result = ((ValueTypeInteger.ValueInteger) res1).getRawValue();
        boolean inRange = result >= 0 && result < 10;
        assertThat("number in rand range 0 <= X < 10", inRange, is(true));

        DummyVariableInteger iMin2 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(5));
        IValue res2 = Operators.RANDOM_MIN_MAX.evaluate(new IVariable[]{iMin,iMax});
        int result2 = ((ValueTypeInteger.ValueInteger) res1).getRawValue();
        boolean inRange2 = result >= 5 && result < 10;
        assertThat("number in rand range 5 <= X < 10", inRange, is(true));

        DummyVariableInteger iMin3 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(-100));
        DummyVariableInteger iMax2 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(-90));

        IValue res3 = Operators.RANDOM_MIN_MAX.evaluate(new IVariable[]{iMin,iMax});
        int result3 = ((ValueTypeInteger.ValueInteger) res1).getRawValue();
        boolean inRange3 = result >= -100 && result < -90;
        assertThat("number in rand range -100 <= X < -90", inRange, is(true));
    }

    @Test(expected = EvaluationException.class)
    public void testRandMixedTypes() throws EvaluationException {
        DummyVariableInteger iMin = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0));
        DummyVariableDouble iMax = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(100.0));

        IValue res1 = Operators.RANDOM_MIN_MAX.evaluate(new IVariable[]{iMin,iMax});
        assertThat("mixed minmax uses max type", res1.getType(), is(ValueTypes.DOUBLE));
    }

    @Test(expected = EvaluationException.class)
    public void testRandMinMaxEqual() throws EvaluationException {
        DummyVariableInteger iMin = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0));
        DummyVariableInteger iMax = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0));

        Operators.RANDOM_ZERO_MAX.evaluate(new IVariable[]{iMin,iMax});
    }

    @Test(expected = EvaluationException.class)
    public void testRandMaxLessThanMin() throws EvaluationException {
        DummyVariableInteger iMin = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(100));
        DummyVariableInteger iMax = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0));

        Operators.RANDOM_ZERO_MAX.evaluate(new IVariable[]{iMin,iMax});
    }

    @Test
    public void testRandBool() throws EvaluationException {
        IValue res1 = Operators.RANDOM_BOOLEAN.evaluate(new IVariable[]{});
        assertThat("random boolean", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(),
                anyOf(is(true), is(false)));
    }

}
