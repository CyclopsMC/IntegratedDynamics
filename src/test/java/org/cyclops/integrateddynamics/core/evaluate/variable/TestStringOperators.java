package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.junit.Before;
import org.junit.Test;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;

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
    private DummyVariableString shello;
    private DummyVariableString sworld;
    private DummyVariableString shelloWorld;
    private DummyVariableString sregex;
    private DummyVariableString sbrokenRegex;
    private DummyVariableInteger i0;
    private DummyVariableInteger i1;
    private DummyVariableInteger i2;
    private DummyVariableInteger i10;
    private DummyVariableDouble d10_5;

    @Before
    public void before() {
        sabc = new DummyVariableString(ValueTypeString.ValueString.of("abc"));
        shello = new DummyVariableString(ValueTypeString.ValueString.of("hello"));
        sworld = new DummyVariableString(ValueTypeString.ValueString.of("world"));
        shelloWorld = new DummyVariableString(ValueTypeString.ValueString.of("hello world"));
        sregex = new DummyVariableString(ValueTypeString.ValueString.of("\\A(.+?)(world)\\z"));
        sbrokenRegex = new DummyVariableString(ValueTypeString.ValueString.of("[.+"));
        i0 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0));
        i1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1));
        i2 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(2));
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
     * ----------------------------------- CONTAINS -----------------------------------
     */

    @Test
    public void testStringContains() throws EvaluationException {
        IValue res1 = Operators.STRING_CONTAINS.evaluate(new IVariable[]{shelloWorld, shello});
        IValue res2 = Operators.STRING_CONTAINS.evaluate(new IVariable[]{shelloWorld, sworld});
        IValue res3 = Operators.STRING_CONTAINS.evaluate(new IVariable[]{shelloWorld, sabc});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("hello world contains hello", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
        assertThat("hello world contains world", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));
        assertThat("hello world doesn't contain abc", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeContainsLarge() throws EvaluationException {
        Operators.STRING_CONTAINS.evaluate(new IVariable[]{sabc, sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeContainsSmall() throws EvaluationException {
        Operators.STRING_CONTAINS.evaluate(new IVariable[]{sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeContains() throws EvaluationException {
        Operators.STRING_CONTAINS.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- STARTS_WITH -----------------------------------
     */

    @Test
    public void testStringStartsWith() throws EvaluationException {
        IValue res1 = Operators.STRING_STARTS_WITH.evaluate(new IVariable[]{shelloWorld, shello});
        IValue res2 = Operators.STRING_STARTS_WITH.evaluate(new IVariable[]{shelloWorld, sworld});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("hello world starts with hello", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
        assertThat("hello world doesn't start with world", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeStartsWithLarge() throws EvaluationException {
        Operators.STRING_STARTS_WITH.evaluate(new IVariable[]{sabc, sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeStartsWithSmall() throws EvaluationException {
        Operators.STRING_STARTS_WITH.evaluate(new IVariable[]{sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeStartsWith() throws EvaluationException {
        Operators.STRING_STARTS_WITH.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ENDS_WITH -----------------------------------
     */

    @Test
    public void testStringEndsWith() throws EvaluationException {
        IValue res1 = Operators.STRING_ENDS_WITH.evaluate(new IVariable[]{shelloWorld, sworld});
        IValue res2 = Operators.STRING_ENDS_WITH.evaluate(new IVariable[]{shelloWorld, shello});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("hello world ends with world", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
        assertThat("hello world doesn't end with hello", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeEndsWithLarge() throws EvaluationException {
        Operators.STRING_ENDS_WITH.evaluate(new IVariable[]{sabc, sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeEndsWithSmall() throws EvaluationException {
        Operators.STRING_ENDS_WITH.evaluate(new IVariable[]{sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeEndsWith() throws EvaluationException {
        Operators.STRING_ENDS_WITH.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- CONTAINS_REGEX -----------------------------------
     */

    @Test
    public void testStringContainsRegex() throws EvaluationException {
        DummyVariableString shelloPlus = new DummyVariableString(ValueTypeString.ValueString.of("hello.+"));
        DummyVariableString sstarWorld = new DummyVariableString(ValueTypeString.ValueString.of(".*world"));
        IValue res1 = Operators.STRING_CONTAINS_REGEX.evaluate(new IVariable[]{shelloWorld, shelloPlus});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("hello world contains_regex hello.+", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
        IValue res2 = Operators.STRING_CONTAINS_REGEX.evaluate(new IVariable[]{shelloWorld, sstarWorld});
        assertThat("hello world contains_regex .*world", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));
        IValue res3 = Operators.STRING_CONTAINS_REGEX.evaluate(new IVariable[]{shelloWorld, sregex});
        assertThat("hello world contains a complex regex", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(true));
        IValue res4 = Operators.STRING_CONTAINS_REGEX.evaluate(new IVariable[]{shelloWorld, sabc});
        assertThat("hello world doesn't match abc", ((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidPatternContainsRegex() throws EvaluationException {
        Operators.STRING_CONTAINS_REGEX.evaluate(new IVariable[]{sabc, sbrokenRegex});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeContainsRegexLarge() throws EvaluationException {
        Operators.STRING_CONTAINS_REGEX.evaluate(new IVariable[]{sabc, sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeContainsRegexSmall() throws EvaluationException {
        Operators.STRING_CONTAINS_REGEX.evaluate(new IVariable[]{sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeContainsRegex() throws EvaluationException {
        Operators.STRING_CONTAINS_REGEX.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- SPLIT_ON -----------------------------------
     */

    @Test
    public void testStringSplitOn() throws EvaluationException {
        DummyVariableString sspace = new DummyVariableString(ValueTypeString.ValueString.of(" "));
        IValue res1 = Operators.STRING_SPLIT_ON.evaluate(new IVariable[]{shelloWorld, sspace});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy<ValueTypeString, ValueTypeString.ValueString> list = ((ValueTypeList.ValueList) res1).getRawValue();
        assertThat("split_on('hello world', ' ')[0] = hello", list.get(0).getRawValue(), is("hello"));
        assertThat("split_on('hello world', ' ')[1] = world", list.get(1).getRawValue(), is("world"));
        assertThat("split_on('hello world', ' ').size = 2", list.getLength(), is(2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeSplitOnLarge() throws EvaluationException {
        Operators.STRING_SPLIT_ON.evaluate(new IVariable[]{sabc, sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeSplitOnSmall() throws EvaluationException {
        Operators.STRING_SPLIT_ON.evaluate(new IVariable[]{sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeSplitOn() throws EvaluationException {
        Operators.STRING_SPLIT_ON.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- SPLIT_ON_REGEX -----------------------------------
     */

    @Test
    public void testStringSplitOnRegex() throws EvaluationException {
        DummyVariableString swhitespace = new DummyVariableString(ValueTypeString.ValueString.of("\\s"));
        IValue res1 = Operators.STRING_SPLIT_ON_REGEX.evaluate(new IVariable[]{shelloWorld, swhitespace});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy<ValueTypeString, ValueTypeString.ValueString> list = ((ValueTypeList.ValueList) res1).getRawValue();
        assertThat("split_on_regex('hello world', ' ')[0] = hello", list.get(0).getRawValue(), is("hello"));
        assertThat("split_on_regex('hello world', ' ')[1] = world", list.get(1).getRawValue(), is("world"));
        assertThat("split_on_regex('hello world', ' ').size = 2", list.getLength(), is(2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidPatternSplitOnRegex() throws EvaluationException {
        DummyVariableString sbroken = new DummyVariableString(ValueTypeString.ValueString.of("[.+"));
        Operators.STRING_SPLIT_ON_REGEX.evaluate(new IVariable[]{sabc, sbroken});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeSplitOnRegexLarge() throws EvaluationException {
        Operators.STRING_SPLIT_ON_REGEX.evaluate(new IVariable[]{sabc, sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeSplitOnRegexSmall() throws EvaluationException {
        Operators.STRING_SPLIT_ON_REGEX.evaluate(new IVariable[]{sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeSplitOnRegex() throws EvaluationException {
        Operators.STRING_SPLIT_ON_REGEX.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- SUBSTRING -----------------------------------
     */

    @Test
    public void testStringSubstring() throws EvaluationException {
        IValue res1 = Operators.STRING_SUBSTRING.evaluate(new IVariable[]{sabc, i1, i2});
        IValue res2 = Operators.STRING_SUBSTRING.evaluate(new IVariable[]{shelloWorld, i1, i10});
        IValue res3 = Operators.STRING_SUBSTRING.evaluate(new IVariable[]{sabc, i1, i1});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("abc substring (1, 2) = b", ((ValueTypeString.ValueString) res1).getRawValue(), is("b"));
        assertThat("hello world substring (1, 10) = 'ello worl'", ((ValueTypeString.ValueString) res2).getRawValue(), is("ello worl"));
        assertThat("abc substring (1, 1) = ''", ((ValueTypeString.ValueString) res3).getRawValue(), is(""));
    }

    @Test(expected = EvaluationException.class)
    public void testOutOfBoundsSubstring() throws EvaluationException {
        Operators.STRING_SUBSTRING.evaluate(new IVariable[]{sabc, i10, i10});
    }

    @Test(expected = EvaluationException.class)
    public void testNegativeSubstring() throws EvaluationException {
        DummyVariableInteger iNeg1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(-1));
        Operators.STRING_SUBSTRING.evaluate(new IVariable[]{sabc, iNeg1, i10});
    }

    @Test(expected = EvaluationException.class)
    public void testInvertedSubstring() throws EvaluationException {
        Operators.STRING_SUBSTRING.evaluate(new IVariable[]{sabc, i10, i1});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeSubstringLarge() throws EvaluationException {
        Operators.STRING_SUBSTRING.evaluate(new IVariable[]{sabc, sabc, sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeSubstringSmall() throws EvaluationException {
        Operators.STRING_SUBSTRING.evaluate(new IVariable[]{sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeSubstring() throws EvaluationException {
        Operators.STRING_SUBSTRING.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- REGEX_GROUP -----------------------------------
     */

    @Test
    public void testStringRegexGroup() throws EvaluationException {
        IValue res1 = Operators.STRING_REGEX_GROUP.evaluate(new IVariable[]{shelloWorld, sregex, i0});
        IValue res2 = Operators.STRING_REGEX_GROUP.evaluate(new IVariable[]{shelloWorld, sregex, i1});
        IValue res3 = Operators.STRING_REGEX_GROUP.evaluate(new IVariable[]{shelloWorld, sregex, i2});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("hello world regex_group ('\\A(.+?)(world)\\z', 0) = hello world", ((ValueTypeString.ValueString) res1).getRawValue(), is("hello world"));
        assertThat("hello world regex_group ('\\A(.+?)(world)\\z', 1) = 'hello '", ((ValueTypeString.ValueString) res2).getRawValue(), is("hello "));
        assertThat("hello world regex_group ('\\A(.+?)(world)\\z', 2) = world", ((ValueTypeString.ValueString) res3).getRawValue(), is("world"));
    }

    @Test(expected = EvaluationException.class)
    public void testOutOfBoundsRegexGroup() throws EvaluationException {
        Operators.STRING_REGEX_GROUP.evaluate(new IVariable[]{shelloWorld, sregex, i10});
    }

    @Test(expected = EvaluationException.class)
    public void testNegativeRegexGroup() throws EvaluationException {
        DummyVariableInteger iNeg1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(-1));
        Operators.STRING_REGEX_GROUP.evaluate(new IVariable[]{shelloWorld, sregex, iNeg1});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidPatternRegexGroup() throws EvaluationException {
        Operators.STRING_REGEX_GROUP.evaluate(new IVariable[]{sabc, sbrokenRegex, i1});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeRegexGroupLarge() throws EvaluationException {
        Operators.STRING_REGEX_GROUP.evaluate(new IVariable[]{sabc, sabc, sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeRegexGroupSmall() throws EvaluationException {
        Operators.STRING_REGEX_GROUP.evaluate(new IVariable[]{sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeRegexGroup() throws EvaluationException {
        Operators.STRING_REGEX_GROUP.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE});
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
