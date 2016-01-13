package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test the different integer operators.
 * @author rubensworks
 */
public class TestListOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableList labc;

    @BeforeClass
    public static void beforeClass() {
        ValueTypeListProxyFactories.load();
    }

    @Before
    public void before() {
        labc = new DummyVariableList(ValueTypeList.ValueList.ofAll(
                ValueTypeString.ValueString.of("a"),
                ValueTypeString.ValueString.of("b"),
                ValueTypeString.ValueString.of("c")
        ));
    }

    /**
     * ----------------------------------- LENGTH -----------------------------------
     */

    @Test
    public void testListLength() throws EvaluationException {
        IValue res1 = Operators.LIST_LENGTH.evaluate(new IVariable[]{labc});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("len(abc) = 3", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(3));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLengthLarge() throws EvaluationException {
        Operators.LIST_LENGTH.evaluate(new IVariable[]{labc, labc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLengthSmall() throws EvaluationException {
        Operators.LIST_LENGTH.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeLength() throws EvaluationException {
        Operators.LIST_LENGTH.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- GET -----------------------------------
     */

    @Test
    public void testListElement() throws EvaluationException {
        IValue res1 = Operators.LIST_ELEMENT.evaluate(new IVariable[]{labc, new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0))});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("get(abc, 0) = 'a'", ((ValueTypeString.ValueString) res1).getRawValue(), is("a"));

        IValue res2 = Operators.LIST_ELEMENT.evaluate(new IVariable[]{labc, new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1))});
        assertThat("result is a string", res2, instanceOf(ValueTypeString.ValueString.class));
        assertThat("get(abc, 1) = 'b'", ((ValueTypeString.ValueString) res2).getRawValue(), is("b"));

        IValue res3 = Operators.LIST_ELEMENT.evaluate(new IVariable[]{labc, new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(2))});
        assertThat("result is a string", res3, instanceOf(ValueTypeString.ValueString.class));
        assertThat("get(abc, 2) = 'c'", ((ValueTypeString.ValueString) res3).getRawValue(), is("c"));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeElementLarge() throws EvaluationException {
        Operators.LIST_ELEMENT.evaluate(new IVariable[]{labc, labc, labc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeElementSmall() throws EvaluationException {
        Operators.LIST_ELEMENT.evaluate(new IVariable[]{labc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeElement() throws EvaluationException {
        Operators.LIST_ELEMENT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

}
