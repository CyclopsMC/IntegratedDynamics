package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test the different integer operators.
 * @author rubensworks
 */
public class TestParseOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private ValueParseRegistry vpr;

    private static <T> ValueTypeString.ValueString s(T v){
        return ValueTypeString.ValueString.of(String.valueOf(v));
    }

    @Before
    public void before() {
        vpr = ValueParseRegistry.getInstance();
        ValueParseMappings.load();
    }

    /**
     * ----------------------------------- INTEGER -----------------------------------
     */
    @Test
    public void testParseInt_IsInt() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.INTEGER, s("garbage"));
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
    }
    @Test
    public void testParseIntEmpty() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.INTEGER, s(""));
        assertThat("parse_Integer(\"garbage\")", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));
    }
    @Test
    public void testParseIntGarbage() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.INTEGER, s("garbage"));
        assertThat("parse_Integer(\"garbage\")", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));
    }
    @Test
    public void testParseInt0() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.INTEGER, s(0));
        assertThat("parse_Integer(0xFF)", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));
    }
    @Test
    public void testParseInt1() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.INTEGER, s(1));
        assertThat("parse_Integer(0xFF)", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(1));
    }
    @Test
    public void testParseIntN1() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.INTEGER, s(-1));
        assertThat("parse_Integer(0xFF)", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(-1));
    }
    @Test
    public void testParseIntHex_x() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.INTEGER, s("0xFF"));
        assertThat("parse_Integer(0xFF)", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0xFF));
    }
    @Test
    public void testParseIntHex_X() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.INTEGER, s("0XFF"));
        assertThat("parse_Integer(0XFF)", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0xFF));
    }
    @Test
    public void testParseIntHex_H() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.INTEGER, s("#FF"));
        assertThat("parse_Integer(#FF)", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0xFF));
    }
    @Test
    public void testParseIntNHex() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.INTEGER, s("-0xFF"));
        assertThat("parse_Integer(0xFF)", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(-0xFF));
    }
    @Test
    public void testParseIntOctal() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.INTEGER, s("01"));
        assertThat("parse_Integer(01)", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(1));
    }
    @Test
    public void testParseIntNOctal() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.INTEGER, s("-01"));
        assertThat("parse_Integer(-01)", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(-1));
    }
    @Test
    public void testParseIntMax() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.INTEGER, s(Integer.MAX_VALUE));
        assertThat("parse_Integer(<Integer.MAX_VALUE>)", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(Integer.MAX_VALUE));
    }
    @Test
    public void testParseIntMaxP1() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.INTEGER, s((long) Integer.MAX_VALUE + 1));
        assertThat("parse_Integer(<Integer.MAX_VALUE + 1>)", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));
    }
    @Test
    public void testParseIntMin() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.INTEGER, s(Integer.MIN_VALUE));
        assertThat("parse_Integer(<Integer.MIN_VALUE>)", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(Integer.MIN_VALUE));
    }
    @Test
    public void testParseIntMinM1() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.INTEGER, s((long) Integer.MIN_VALUE - 1));
        assertThat("parse_Integer(<Integer.MIN_VALUE - 1>)", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));
    }

    /**
     * ----------------------------------- LONG -----------------------------------
     */

    @Test
    public void testParseLong_IsLong() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.LONG, s("garbage"));
        assertThat("result is a long", res1, instanceOf(ValueTypeLong.ValueLong.class));
    }
    @Test
    public void testParseLongEmpty() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.LONG, s(""));
        assertThat("parse_Long(\"garbage\")", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(0L));
    }
    @Test
    public void testParseLongGarbage() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.LONG, s("garbage"));
        assertThat("parse_Long(\"garbage\")", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(0L));
    }
    @Test
    public void testParseLong0() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.LONG, s(0L));
        assertThat("parse_Long(0xFF)", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(0L));
    }
    @Test
    public void testParseLong1() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.LONG, s(1L));
        assertThat("parse_Long(0xFF)", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(1L));
    }
    @Test
    public void testParseLongN1() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.LONG, s(-1L));
        assertThat("parse_Long(-1L)", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(-1L));
    }
    @Test
    public void testParseLongHex_x() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.LONG, s("0xFF"));
        assertThat("parse_Long(0xFF)", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(0xFFL));
    }
    @Test
    public void testParseLongHex_X() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.LONG, s("0XFF"));
        assertThat("parse_Long(0XFF)", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(0xFFL));
    }
    @Test
    public void testParseLongHex_H() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.LONG, s("#FF"));
        assertThat("parse_Long(#FF)", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(0xFFL));
    }
    @Test
    public void testParseLongNHex() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.LONG, s("-0xFF"));
        assertThat("parse_Long(0xFF)", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(-0xFFL));
    }
    @Test
    public void testParseLongOctal() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.LONG, s("01"));
        assertThat("parse_Long(01)", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(1L));
    }
    @Test
    public void testParseLongNOctal() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.LONG, s("-01"));
        assertThat("parse_Long(01)", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(-1L));
    }
    @Test
    public void testParseLongMax() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.LONG, s(Long.MAX_VALUE));
        assertThat("parse_Long(<Long.MAX_VALUE>)", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(Long.MAX_VALUE));
    }
    @Test
    public void testParseLongMaxP1() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.LONG, s("9223372036854775808"));
        assertThat("parse_Long(<Long.MAX_VALUE + 1>)", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(0L));
    }
    @Test
    public void testParseLongMin() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.LONG, s(Long.MIN_VALUE));
        assertThat("parse_Long(<Long.MIN_VALUE>)", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(Long.MIN_VALUE));
    }
    @Test
    public void testParseLongMinM1() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.LONG, s("-9223372036854775809"));
        assertThat("parse_Long(<Long.MIN_VALUE - 1>)", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(0L));
    }

    /**
     * ----------------------------------- DOUBLE -----------------------------------
     */
    // TODO: Floating point Hex/Octal
    // TODO: No leading 0 as in .1

    @Test
    public void testParseDouble_IsDouble() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("garbage"));
        assertThat("result is a double", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
    }
    @Test
    public void testParseDoubleEmpty() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s(""));
        assertThat("parse_Double(\"garbage\")", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(0.0));
    }
    @Test
    public void testParseDoubleGarbage() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("garbage"));
        assertThat("parse_Double(\"garbage\")", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(0.0));
    }
    @Test
    public void testParseDouble0() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s(0.0));
        assertThat("parse_Double(0xFF)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(0.0));
    }
    @Test
    public void testParseDouble1() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s(1.0));
        assertThat("parse_Double(0xFF)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(1.0));
    }
    @Test
    public void testParseDoubleN0() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("-0.0"));
        assertThat("parse_Double(-1L)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(-0.0));
    }
    @Test
    public void testParseDoubleN1() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s(-1.0));
        assertThat("parse_Double(-1L)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(-1.0));
    }
    @Test
    public void testParseDoubleHex_x() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("0xFF"));
        assertThat("parse_Double(0xFF)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(255.0));
    }
    @Test
    public void testParseDoubleHex_X() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("0XFF"));
        assertThat("parse_Double(0XFF)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(255.0));
    }
    @Test
    public void testParseDoubleHex_H() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("#FF"));
        assertThat("parse_Double(#FF)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(255.0));
    }
    @Test
    public void testParseDoubleNHex() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("-0xFF"));
        assertThat("parse_Double(-0xFF)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(-255.0));
    }
    @Test
    public void testParseDoubleDHex() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("0xFF.FF"));
        assertThat("parse_Double(0xFF.FF)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(255.255));
    }
    @Test
    public void testParseDoubleNDHex() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("-0xFF.FF"));
        assertThat("parse_Double(-0xFF.FF)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(-255.255));
    }
    @Test
    public void testParseDoubleOctal() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("01"));
        assertThat("parse_Double(01)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(1.0));
    }
    @Test
    public void testParseDoubleNOctal() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("-01"));
        assertThat("parse_Double(-01)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(-1.0));
    }
    @Test
    public void testParseDoubleDOctal() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("01.1"));
        assertThat("parse_Double(01)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(1.1));
    }
    @Test
    public void testParseDoubleNDOctal() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("-01.1"));
        assertThat("parse_Double(-01)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(-1.1));
    }
    @Test
    public void testParseDoubleMax() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s(Double.MAX_VALUE));
        assertThat("parse_Double(<Double.MAX_VALUE>)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(Double.MAX_VALUE));
    }
    @Test
    public void testParseDoubleMaxP1() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("1.7976931348623157e+309"));
        assertThat("parse_Double(<Double.MAX_VALUE * 10>)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(0.0));
    }
    @Test
    public void testParseDoubleMin() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s(Double.MIN_VALUE));
        assertThat("parse_Double(<Double.MIN_VALUE>)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(Double.MIN_VALUE));
    }
    @Test
    public void testParseDoubleMinM1() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("4.9e-325"));
        assertThat("parse_Double(<Double.MIN_VALUE * -10>)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(0.0));
    }
    @Test
    public void testParseDoubleInf() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("Inf"));
        assertThat("parse_Double(Inf)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(Double.POSITIVE_INFINITY));
    }
    @Test
    public void testParseDoubleNInf() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("-Inf"));
        assertThat("parse_Double(-Inf)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(Double.NEGATIVE_INFINITY));
    }    @Test
    public void testParseDoubleInfinity() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("Infinity"));
        assertThat("parse_Double(Infinity)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(Double.POSITIVE_INFINITY));
    }
    @Test
    public void testParseDoubleNInfinity() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("-Infinity"));
        assertThat("parse_Double(-Infinity)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(Double.NEGATIVE_INFINITY));
    }
    @Test
    public void testParseDoubleinf() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("inf"));
        assertThat("parse_Double(inf)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(Double.POSITIVE_INFINITY));
    }
    @Test
    public void testParseDoubleNinf() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("-inf"));
        assertThat("parse_Double(-inf)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(Double.NEGATIVE_INFINITY));
    }    @Test
    public void testParseDoubleinfinity() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("infinity"));
        assertThat("parse_Double(infinity)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(Double.POSITIVE_INFINITY));
    }
    @Test
    public void testParseDoubleNinfinity() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.DOUBLE, s("-infinity"));
        assertThat("parse_Double(-infinity)", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(Double.NEGATIVE_INFINITY));
    }

    /**
     * ----------------------------------- BOOLEAN -----------------------------------
     */
    // TODO: Need /([Tt](rue)?|[Ff](alse)?)/ cases
    @Test
    public void testParseBoolean_IsBoolean() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.BOOLEAN, s("garbage"));
        assertThat("result is an boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
    }
    @Test
    public void testParseBooleanEmpty() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.BOOLEAN, s(""));
        assertThat("parse_Boolean(\"garbage\")", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(false));
    }
    @Test
    public void testParseBooleanGarbage() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.BOOLEAN, s("garbage"));
        assertThat("parse_Boolean(\"garbage\")", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
    }
    @Test
    public void testParseBoolean0() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.BOOLEAN, s(0));
        assertThat("parse_Boolean(0xFF)", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(false));
    }
    @Test
    public void testParseBoolean1() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.BOOLEAN, s(1));
        assertThat("parse_Boolean(0xFF)", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
    }
    @Test
    public void testParseBooleanN1() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.BOOLEAN, s(-1));
        assertThat("parse_Boolean(0xFF)", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
    }
    @Test
    public void testParseBooleanHex_x() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.BOOLEAN, s("0xFF"));
        assertThat("parse_Boolean(0xFF)", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
    }
    @Test
    public void testParseBooleanHex_X() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.BOOLEAN, s("0XFF"));
        assertThat("parse_Boolean(0XFF)", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
    }
    @Test
    public void testParseBooleanHex_H() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.BOOLEAN, s("#FF"));
        assertThat("parse_Boolean(#FF)", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
    }
    @Test
    public void testParseBooleanNHex() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.BOOLEAN, s("-0xFF"));
        assertThat("parse_Boolean(0xFF)", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
    }
    @Test
    public void testParseBooleanOctal() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.BOOLEAN, s("01"));
        assertThat("parse_Boolean(01)", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
    }
    @Test
    public void testParseBooleanNOctal() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.BOOLEAN, s("-01"));
        assertThat("parse_Boolean(-01)", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
    }

    /**
     * ----------------------------------- STRING -----------------------------------
     */
    @Test
    public void testParseString_IsString() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.STRING, s("garbage"));
        assertThat("result is an string", res1, instanceOf(ValueTypeString.ValueString.class));
    }
    @Test
    public void testParseStringEmpty() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.STRING, s(""));
        assertThat("parse_String(\"\")", ((ValueTypeString.ValueString) res1).getRawValue(), is(""));
    }
    @Test
    public void testParseStringIdentity() throws EvaluationException {
        IValue res1 = vpr.parse(ValueTypes.STRING, s("♨"));
        assertThat("parse_String(\"\")", ((ValueTypeString.ValueString) res1).getRawValue(), is("♨"));
    }

    /**
     * ----------------------------------- NBT -----------------------------------
     */
    // TODO: Heh
}
