package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.helper.CyclopsCoreInstance;
import org.cyclops.integrateddynamics.ModBaseMocked;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Test implicit casting in arithmetic operators.
 * Expects the conversions Integer -> Long -> Double, i.e. simplest int type to most complex float type.
 * @author met4000
 */
public class TestImplicitArithmeticCasting {

    static { CyclopsCoreInstance.MOD = new ModBaseMocked(); }
    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableInteger i0;
    private DummyVariableLong l0;
    private DummyVariableDouble d0;

    @Before
    public void before() {
        ValueCastMappings.load();

        i0 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0));
        l0 = new DummyVariableLong(ValueTypeLong.ValueLong.of(0));
        d0 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(0));
    }

    /**
     * ----------------------------------- SAME TYPE -----------------------------------
     */

    /**
     * Verifies that an arithmetic operation with two inputs of the same type has the same output type.
     * Uses {@link Operators#ARITHMETIC_ADDITION} for testing - it is assumed that all other operations will behave the same.
     * Any operators that behave differently need separate tests.
     * @throws EvaluationException
     */
    @Test
    public void testImplicitArithmeticCastSameType() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{i0, i0});
        assertThat("i0 + i0 = i0", res1, instanceOf(ValueTypeInteger.ValueInteger.class));

        IValue res2 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{l0, l0});
        assertThat("l0 + l0 = l0", res2, instanceOf(ValueTypeLong.ValueLong.class));

        IValue res3 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{d0, d0});
        assertThat("d0 + d0 = d0", res3, instanceOf(ValueTypeDouble.ValueDouble.class));
    }

    /**
     * Fails if all type checks incorrectly always return true.
     * @throws EvaluationException
     */
    @Test
    public void testImplicitArithmeticCastSameTypeFalseNegative() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{i0, i0});
        assertThat("i0 + i0 != l0", res1, not(instanceOf(ValueTypeLong.ValueLong.class)));

        IValue res2 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{l0, l0});
        assertThat("l0 + l0 != d0", res2, not(instanceOf(ValueTypeDouble.ValueDouble.class)));

        IValue res3 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{d0, d0});
        assertThat("d0 + d0 != i0", res3, not(instanceOf(ValueTypeInteger.ValueInteger.class)));
    }

    /**
     * ----------------------------------- TO INTEGER -----------------------------------
     */

    // N/A

    /**
     * ----------------------------------- TO LONG -----------------------------------
     */

    @Test
    public void testImplicitArithmeticCastIntToLong() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{i0, l0});
        assertThat("i0 + l0 = l0", res1, instanceOf(ValueTypeLong.ValueLong.class));

        IValue res2 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{l0, i0});
        assertThat("l0 + i0 = l0", res2, instanceOf(ValueTypeLong.ValueLong.class));
    }

    /**
     * ----------------------------------- TO DOUBLE -----------------------------------
     */

    @Test
    public void testImplicitArithmeticCastIntToDouble() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{i0, d0});
        assertThat("i0 + d0 = d0", res1, instanceOf(ValueTypeDouble.ValueDouble.class));

        IValue res2 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{d0, i0});
        assertThat("d0 + i0 = d0", res2, instanceOf(ValueTypeDouble.ValueDouble.class));
    }

    @Test
    public void testImplicitArithmeticCastLongToDouble() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{l0, d0});
        assertThat("l0 + d0 = d0", res1, instanceOf(ValueTypeDouble.ValueDouble.class));

        IValue res2 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{d0, l0});
        assertThat("d0 + l0 = d0", res2, instanceOf(ValueTypeDouble.ValueDouble.class));
    }

}
