package org.cyclops.integrateddynamics.core.evaluate.variable;

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
 * Expects Integer -> Long -> Double, i.e. simplest int type to most complex float type.
 * @author met4000
 */
public class TestImplicitArithmeticCasting {

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
     * Verifies that a binary operation with two inputs of the same type has the same output type.
     * Assumes that the casting of all other operations will behave the same as {@link Operators#ARITHMETIC_ADDITION}.
     * Also checks that the output type isn't considered to also be different type - should catch all outputs incorrectly being considered the correct type.
     * @throws EvaluationException
     */
    @Test
    public void testImplicitArithmeticCastSameType() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{i0, i0});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("result is not a long", res1, not(instanceOf(ValueTypeLong.ValueLong.class)));

        IValue res2 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{l0, l0});
        assertThat("result is a long", res2, instanceOf(ValueTypeLong.ValueLong.class));
        assertThat("result is not a double", res2, not(instanceOf(ValueTypeDouble.ValueDouble.class)));

        IValue res3 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{d0, d0});
        assertThat("result is a double", res3, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("result is not an integer", res3, not(instanceOf(ValueTypeInteger.ValueInteger.class)));
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
        assertThat("result is a long", res1, instanceOf(ValueTypeLong.ValueLong.class));

        IValue res2 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{l0, i0});
        assertThat("result is a long", res2, instanceOf(ValueTypeLong.ValueLong.class));
    }

    /**
     * ----------------------------------- TO DOUBLE -----------------------------------
     */

    @Test
    public void testImplicitArithmeticCastIntToDouble() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{i0, d0});
        assertThat("result is a double", res1, instanceOf(ValueTypeDouble.ValueDouble.class));

        IValue res2 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{d0, i0});
        assertThat("result is a double", res2, instanceOf(ValueTypeDouble.ValueDouble.class));
    }

    @Test
    public void testImplicitArithmeticCastLongToDouble() throws EvaluationException {
        IValue res1 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{l0, d0});
        assertThat("result is a double", res1, instanceOf(ValueTypeDouble.ValueDouble.class));

        IValue res2 = Operators.ARITHMETIC_ADDITION.evaluate(new IVariable[]{d0, l0});
        assertThat("result is a double", res2, instanceOf(ValueTypeDouble.ValueDouble.class));
    }

}
