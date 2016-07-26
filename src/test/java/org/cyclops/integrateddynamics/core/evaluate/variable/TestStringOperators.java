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
public class TestStringOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableString sabc;
    private DummyVariableInteger i10;
    private DummyVariableDouble d10_5;

    @Before
    public void before() {
        sabc = new DummyVariableString(ValueTypeString.ValueString.of("abc"));
        i10 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(10));
        d10_5 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(10.5D));
    }

    /**
     * ----------------------------------- LENGTH -----------------------------------
     */

    @Test
    public void testStringLength() throws EvaluationException {
        IValue res1 = Operators.STRING_LENGTH.evaluate(new IVariable[]{sabc});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("len(abc) = 3", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(3));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLengthLarge() throws EvaluationException {
        Operators.STRING_LENGTH.evaluate(new IVariable[]{sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLengthSmall() throws EvaluationException {
        Operators.STRING_LENGTH.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeLength() throws EvaluationException {
        Operators.STRING_LENGTH.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- CONCAT -----------------------------------
     */

    @Test
    public void testStringConcat() throws EvaluationException {
        IValue res1 = Operators.STRING_CONCAT.evaluate(new IVariable[]{sabc, sabc});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("abc + abc = abcabc", ((ValueTypeString.ValueString) res1).getRawValue(), is("abcabc"));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizConcatLarge() throws EvaluationException {
        Operators.STRING_CONCAT.evaluate(new IVariable[]{sabc, sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeConcatSmall() throws EvaluationException {
        Operators.STRING_CONCAT.evaluate(new IVariable[]{sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeConcat() throws EvaluationException {
        Operators.STRING_CONCAT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- NAMED_NAME -----------------------------------
     */

    @Test
    public void testStringNamedName() throws EvaluationException {
        IValue res1 = Operators.NAMED_NAME.evaluate(new IVariable[]{i10});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("name(10) = 10", ((ValueTypeString.ValueString) res1).getRawValue(), is("10"));

        IValue res2 = Operators.NAMED_NAME.evaluate(new IVariable[]{d10_5});
        assertThat("name(10.5) = 10.5", ((ValueTypeString.ValueString) res2).getRawValue(), is("10.5"));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizNamedNameLarge() throws EvaluationException {
        Operators.NAMED_NAME.evaluate(new IVariable[]{sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeNamedNameSmall() throws EvaluationException {
        Operators.NAMED_NAME.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNamedName() throws EvaluationException {
        Operators.NAMED_NAME.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

}
