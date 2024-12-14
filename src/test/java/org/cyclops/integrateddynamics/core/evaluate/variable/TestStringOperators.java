package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Iterator;

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
            new DummyVariable<>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableString sempty;
    private DummyVariableString sabc;
    private DummyVariableString sl;
    private DummyVariableString scomma;
    private DummyVariableString shello;
    private DummyVariableString sworld;
    private DummyVariableString shelloWorld;
    private DummyVariableString sregex;
    private DummyVariableString sbrokenRegex;
    private DummyVariableInteger i0;
    private DummyVariableInteger i1;
    private DummyVariableInteger i2;
    private DummyVariableInteger i10;
    private DummyVariableInteger i11;
    private DummyVariableDouble d10_5;
    private DummyVariableList labc;
    private DummyVariableList lint;
    private DummyVariableList lstringinvalidtypes;
    private DummyVariableList lstring_inf;

    @BeforeClass
    public static void beforeClass() {
        ValueTypeListProxyFactories.load();
    }

    @Before
    public void before() {
        sempty = new DummyVariableString(ValueTypeString.ValueString.of(""));
        sabc = new DummyVariableString(ValueTypeString.ValueString.of("abc"));
        sl = new DummyVariableString(ValueTypeString.ValueString.of("l"));
        scomma = new DummyVariableString(ValueTypeString.ValueString.of(","));
        shello = new DummyVariableString(ValueTypeString.ValueString.of("hello"));
        sworld = new DummyVariableString(ValueTypeString.ValueString.of("world"));
        shelloWorld = new DummyVariableString(ValueTypeString.ValueString.of("hello world"));
        sregex = new DummyVariableString(ValueTypeString.ValueString.of("\\A(.+?)(world)\\z"));
        sbrokenRegex = new DummyVariableString(ValueTypeString.ValueString.of("[.+"));
        i0 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0));
        i1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1));
        i2 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(2));
        i10 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(10));
        i11 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(11));
        d10_5 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(10.5D));
        labc = new DummyVariableList(ValueTypeList.ValueList.ofAll(ValueTypes.STRING,
                ValueTypeString.ValueString.of("a"),
                ValueTypeString.ValueString.of("b"),
                ValueTypeString.ValueString.of("c")
        ));
        lint = new DummyVariableList(ValueTypeList.ValueList.ofAll(ValueTypes.INTEGER,
                ValueTypeInteger.ValueInteger.of(123)));
        lstringinvalidtypes = new DummyVariableList(ValueTypeList.ValueList.ofAll(ValueTypes.STRING,
                ValueTypeInteger.ValueInteger.of(123)
        ));
        lstring_inf = new DummyVariableList(ValueTypeList.ValueList.ofFactory(new IValueTypeListProxy() {
            @Override
            public int getLength() throws EvaluationException {
                return 0;
            }

            @Override
            public IValue get(int index) throws EvaluationException {
                return null;
            }

            @Override
            public IValueType<? extends IValue> getValueType() {
                return ValueTypes.STRING;
            }

            @Override
            public ResourceLocation getName() {
                return null;
            }

            @Override
            public MutableComponent toCompactString() {
                return null;
            }

            @Override
            public boolean isInfinite() {
                return true;
            }

            @Override
            public Iterator<IValue> iterator() {
                return null;
            }
        }));
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
    public void testInvalidInputSizeConcatLarge() throws EvaluationException {
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
        IValue res1 = Operators.STRING_CONTAINS.evaluate(new IVariable[]{shello, shelloWorld});
        IValue res2 = Operators.STRING_CONTAINS.evaluate(new IVariable[]{sworld, shelloWorld});
        IValue res3 = Operators.STRING_CONTAINS.evaluate(new IVariable[]{sabc, shelloWorld});
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
     * ----------------------------------- INDEX_OF -----------------------------------
     */

    @Test
    public void testStringIndexOf() throws EvaluationException {
        IValue res1 = Operators.STRING_INDEX_OF.evaluate(new IVariable[]{shello, shelloWorld});
        IValue res2 = Operators.STRING_INDEX_OF.evaluate(new IVariable[]{sworld, shelloWorld});
        IValue res3 = Operators.STRING_INDEX_OF.evaluate(new IVariable[]{sabc, shelloWorld});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("hello world index_of hello = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));
        assertThat("hello world index_of world = 6", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(6));
        assertThat("hello world index_of abc = -1", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(-1));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIndexOfLarge() throws EvaluationException {
        Operators.STRING_INDEX_OF.evaluate(new IVariable[]{sabc, sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIndexOfSmall() throws EvaluationException {
        Operators.STRING_INDEX_OF.evaluate(new IVariable[]{sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeIndexOf() throws EvaluationException {
        Operators.STRING_INDEX_OF.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- INDEX_OF_REGEX -----------------------------------
     */

    @Test
    public void testStringIndexOfRegex() throws EvaluationException {
        DummyVariableString word = new DummyVariableString(ValueTypeString.ValueString.of("worl?d"));
        IValue res1 = Operators.STRING_INDEX_OF_REGEX.evaluate(new IVariable[]{sregex, shelloWorld});
        IValue res2 = Operators.STRING_INDEX_OF_REGEX.evaluate(new IVariable[]{sworld, shelloWorld});
        IValue res3 = Operators.STRING_INDEX_OF_REGEX.evaluate(new IVariable[]{word, shelloWorld});
        IValue res4 = Operators.STRING_INDEX_OF_REGEX.evaluate(new IVariable[]{sabc, shelloWorld});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("hello world index_of_regex complex regex = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));
        assertThat("hello world index_of_regex world = 6", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(6));
        assertThat("hello world index_of_regex worl?d = 6", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(6));
        assertThat("hello world index_of_regex abc = -1", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(-1));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidPatternIndexOfRegex() throws EvaluationException {
        Operators.STRING_INDEX_OF_REGEX.evaluate(new IVariable[]{sbrokenRegex, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIndexOfRegexLarge() throws EvaluationException {
        Operators.STRING_INDEX_OF_REGEX.evaluate(new IVariable[]{sabc, sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIndexOfRegexSmall() throws EvaluationException {
        Operators.STRING_INDEX_OF_REGEX.evaluate(new IVariable[]{sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeIndexOfRegex() throws EvaluationException {
        Operators.STRING_INDEX_OF_REGEX.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- STARTS_WITH -----------------------------------
     */

    @Test
    public void testStringStartsWith() throws EvaluationException {
        IValue res1 = Operators.STRING_STARTS_WITH.evaluate(new IVariable[]{shello, shelloWorld});
        IValue res2 = Operators.STRING_STARTS_WITH.evaluate(new IVariable[]{sworld, shelloWorld});
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
        IValue res1 = Operators.STRING_ENDS_WITH.evaluate(new IVariable[]{sworld, shelloWorld});
        IValue res2 = Operators.STRING_ENDS_WITH.evaluate(new IVariable[]{shello, shelloWorld});
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
        DummyVariableString sello = new DummyVariableString(ValueTypeString.ValueString.of("e..o"));
        IValue res1 = Operators.STRING_CONTAINS_REGEX.evaluate(new IVariable[]{shelloPlus, shelloWorld});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("hello world contains_regex hello.+", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
        IValue res2 = Operators.STRING_CONTAINS_REGEX.evaluate(new IVariable[]{sstarWorld, shelloWorld});
        assertThat("hello world contains_regex .*world", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));
        IValue res3 = Operators.STRING_CONTAINS_REGEX.evaluate(new IVariable[]{sregex, shelloWorld});
        assertThat("hello world contains a complex regex", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(true));
        IValue res4 = Operators.STRING_CONTAINS_REGEX.evaluate(new IVariable[]{sabc, shelloWorld});
        assertThat("hello world doesn't match abc", ((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), is(false));
        IValue res5 = Operators.STRING_CONTAINS_REGEX.evaluate(new IVariable[]{sello, shelloWorld});
        assertThat("hello world contains e..o", ((ValueTypeBoolean.ValueBoolean) res5).getRawValue(), is(true));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidPatternContainsRegex() throws EvaluationException {
        Operators.STRING_CONTAINS_REGEX.evaluate(new IVariable[]{sbrokenRegex, sabc});
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
     * ----------------------------------- MATCHES_REGEX -----------------------------------
     */

    @Test
    public void testStringMatchesRegex() throws EvaluationException {
        DummyVariableString shelloPlus = new DummyVariableString(ValueTypeString.ValueString.of("hello.+"));
        DummyVariableString sstarWorld = new DummyVariableString(ValueTypeString.ValueString.of(".*world"));
        DummyVariableString sello = new DummyVariableString(ValueTypeString.ValueString.of("e..o"));
        IValue res1 = Operators.STRING_MATCHES_REGEX.evaluate(new IVariable[]{shelloPlus, shelloWorld});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("hello world matches_regex hello.+", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
        IValue res2 = Operators.STRING_MATCHES_REGEX.evaluate(new IVariable[]{sstarWorld, shelloWorld});
        assertThat("hello world matches_regex .*world", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));
        IValue res3 = Operators.STRING_MATCHES_REGEX.evaluate(new IVariable[]{sregex, shelloWorld});
        assertThat("hello world matches a complex regex", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(true));
        IValue res4 = Operators.STRING_MATCHES_REGEX.evaluate(new IVariable[]{sabc, shelloWorld});
        assertThat("hello world doesn't match abc", ((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), is(false));
        IValue res5 = Operators.STRING_MATCHES_REGEX.evaluate(new IVariable[]{sello, shelloWorld});
        assertThat("hello world doesn't match e..o", ((ValueTypeBoolean.ValueBoolean) res5).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidPatternMatchesRegex() throws EvaluationException {
        Operators.STRING_MATCHES_REGEX.evaluate(new IVariable[]{sbrokenRegex, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMatchesRegexLarge() throws EvaluationException {
        Operators.STRING_MATCHES_REGEX.evaluate(new IVariable[]{sabc, sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMatchesRegexSmall() throws EvaluationException {
        Operators.STRING_MATCHES_REGEX.evaluate(new IVariable[]{sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeMatchesRegex() throws EvaluationException {
        Operators.STRING_MATCHES_REGEX.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- SPLIT_ON -----------------------------------
     */

    @Test
    public void testStringSplitOn() throws EvaluationException {
        DummyVariableString sspace = new DummyVariableString(ValueTypeString.ValueString.of(" "));
        IValue res1 = Operators.STRING_SPLIT_ON.evaluate(new IVariable[]{sspace, shelloWorld});
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
        IValue res1 = Operators.STRING_SPLIT_ON_REGEX.evaluate(new IVariable[]{swhitespace, shelloWorld});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy<ValueTypeString, ValueTypeString.ValueString> list = ((ValueTypeList.ValueList) res1).getRawValue();
        assertThat("split_on_regex('hello world', ' ')[0] = hello", list.get(0).getRawValue(), is("hello"));
        assertThat("split_on_regex('hello world', ' ')[1] = world", list.get(1).getRawValue(), is("world"));
        assertThat("split_on_regex('hello world', ' ').size = 2", list.getLength(), is(2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidPatternSplitOnRegex() throws EvaluationException {
        DummyVariableString sbroken = new DummyVariableString(ValueTypeString.ValueString.of("[.+"));
        Operators.STRING_SPLIT_ON_REGEX.evaluate(new IVariable[]{sbroken, sabc});
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
        IValue res1 = Operators.STRING_SUBSTRING.evaluate(new IVariable[]{i1, i2, sabc});
        IValue res2 = Operators.STRING_SUBSTRING.evaluate(new IVariable[]{i1, i10, shelloWorld});
        IValue res3 = Operators.STRING_SUBSTRING.evaluate(new IVariable[]{i1, i1, sabc});
        IValue res4 = Operators.STRING_SUBSTRING.evaluate(new IVariable[]{i1, i11, shelloWorld});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("abc substring (1, 2) = b", ((ValueTypeString.ValueString) res1).getRawValue(), is("b"));
        assertThat("hello world substring (1, 10) = 'ello worl'", ((ValueTypeString.ValueString) res2).getRawValue(), is("ello worl"));
        assertThat("abc substring (1, 1) = ''", ((ValueTypeString.ValueString) res3).getRawValue(), is(""));
        assertThat("hello world substring (1, 11) = 'ello world'", ((ValueTypeString.ValueString) res4).getRawValue(), is("ello world"));
    }

    @Test(expected = EvaluationException.class)
    public void testOutOfBoundsSubstring() throws EvaluationException {
        Operators.STRING_SUBSTRING.evaluate(new IVariable[]{i10, i10, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testNegativeSubstring() throws EvaluationException {
        DummyVariableInteger iNeg1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(-1));
        Operators.STRING_SUBSTRING.evaluate(new IVariable[]{iNeg1, i10, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvertedSubstring() throws EvaluationException {
        Operators.STRING_SUBSTRING.evaluate(new IVariable[]{i10, i1, sabc});
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
        IValue res1 = Operators.STRING_REGEX_GROUP.evaluate(new IVariable[]{sregex, i0, shelloWorld});
        IValue res2 = Operators.STRING_REGEX_GROUP.evaluate(new IVariable[]{sregex, i1, shelloWorld});
        IValue res3 = Operators.STRING_REGEX_GROUP.evaluate(new IVariable[]{sregex, i2, shelloWorld});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("hello world regex_group ('\\A(.+?)(world)\\z', 0) = hello world", ((ValueTypeString.ValueString) res1).getRawValue(), is("hello world"));
        assertThat("hello world regex_group ('\\A(.+?)(world)\\z', 1) = 'hello '", ((ValueTypeString.ValueString) res2).getRawValue(), is("hello "));
        assertThat("hello world regex_group ('\\A(.+?)(world)\\z', 2) = world", ((ValueTypeString.ValueString) res3).getRawValue(), is("world"));
    }

    @Test(expected = EvaluationException.class)
    public void testOutOfBoundsRegexGroup() throws EvaluationException {
        Operators.STRING_REGEX_GROUP.evaluate(new IVariable[]{sregex, i10, shelloWorld});
    }

    @Test(expected = EvaluationException.class)
    public void testNegativeRegexGroup() throws EvaluationException {
        DummyVariableInteger iNeg1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(-1));
        Operators.STRING_REGEX_GROUP.evaluate(new IVariable[]{sregex, iNeg1, shelloWorld});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidPatternRegexGroup() throws EvaluationException {
        Operators.STRING_REGEX_GROUP.evaluate(new IVariable[]{sbrokenRegex, i1, sabc});
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
     * ----------------------------------- REGEX_GROUPS -----------------------------------
     */

    @Test
    public void testStringRegexGroups() throws EvaluationException {
        IValue res1 = Operators.STRING_REGEX_GROUPS.evaluate(new IVariable[]{sabc, shelloWorld});
        IValue res2 = Operators.STRING_REGEX_GROUPS.evaluate(new IVariable[]{sregex, shelloWorld});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy<ValueTypeString, ValueTypeString.ValueString> list1 = ((ValueTypeList.ValueList) res1).getRawValue();
        assertThat("(hello_world regex_groups abc).size = 0", list1.getLength(), is(0));
        IValueTypeListProxy<ValueTypeString, ValueTypeString.ValueString> list2 = ((ValueTypeList.ValueList) res2).getRawValue();
        assertThat("hello world regex_groups ('\\A(.+?)(world)\\z')[0] = hello world", list2.get(0).getRawValue(), is("hello world"));
        assertThat("hello world regex_groups ('\\A(.+?)(world)\\z')[1] = 'hello '", list2.get(1).getRawValue(), is("hello "));
        assertThat("hello world regex_groups ('\\A(.+?)(world)\\z')[2] = world", list2.get(2).getRawValue(), is("world"));
        assertThat("(hello world regex_groups ('\\A(.+?)(world)\\z')).size = 3", list2.getLength(), is(3));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidPatternRegexGroups() throws EvaluationException {
        Operators.STRING_REGEX_GROUPS.evaluate(new IVariable[]{sbrokenRegex, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeRegexGroupsLarge() throws EvaluationException {
        Operators.STRING_REGEX_GROUPS.evaluate(new IVariable[]{sabc, sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeRegexGroupsSmall() throws EvaluationException {
        Operators.STRING_REGEX_GROUPS.evaluate(new IVariable[]{sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeRegexGroups() throws EvaluationException {
        Operators.STRING_REGEX_GROUPS.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- REGEX_SCAN -----------------------------------
     */

    @Test
    public void testStringRegexScan() throws EvaluationException {
        DummyVariableString firstLetters = new DummyVariableString(ValueTypeString.ValueString.of("(\\S)\\S*"));
        IValue res1 = Operators.STRING_REGEX_SCAN.evaluate(new IVariable[]{sabc, i0, shelloWorld});
        IValue res2 = Operators.STRING_REGEX_SCAN.evaluate(new IVariable[]{firstLetters, i1, shelloWorld});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy<ValueTypeString, ValueTypeString.ValueString> list1 = ((ValueTypeList.ValueList) res1).getRawValue();
        assertThat("(hello_world regex_scan abc).size = 0", list1.getLength(), is(0));
        IValueTypeListProxy<ValueTypeString, ValueTypeString.ValueString> list2 = ((ValueTypeList.ValueList) res2).getRawValue();
        assertThat("hello world regex_scan ('(\\S)\\S*', 1)[0] = h", list2.get(0).getRawValue(), is("h"));
        assertThat("hello world regex_scan ('(\\S)\\S*', 1)[1] = w", list2.get(1).getRawValue(), is("w"));
        assertThat("hello world regex_scan ('(\\S)\\S*', 1).size = 2", list2.getLength(), is(2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidPatternRegexScan() throws EvaluationException {
        Operators.STRING_REGEX_SCAN.evaluate(new IVariable[]{sbrokenRegex, i0, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeRegexScanLarge() throws EvaluationException {
        Operators.STRING_REGEX_SCAN.evaluate(new IVariable[]{sabc, sabc, sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeRegexScanSmall() throws EvaluationException {
        Operators.STRING_REGEX_SCAN.evaluate(new IVariable[]{sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeRegexScan() throws EvaluationException {
        Operators.STRING_REGEX_SCAN.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- REPLACE -----------------------------------
     */

    @Test
    public void testStringReplace() throws EvaluationException {
        IValue res1 = Operators.STRING_REPLACE.evaluate(new IVariable[]{sl, sempty, shelloWorld});
        IValue res2 = Operators.STRING_REPLACE.evaluate(new IVariable[]{shelloWorld, sempty, shelloWorld});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("hello world replace (l, '') = heo word", ((ValueTypeString.ValueString)res1).getRawValue(), is("heo word"));
        assertThat("hello world replace (hello world, '') = ''", ((ValueTypeString.ValueString)res2).getRawValue(), is(""));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeReplaceLarge() throws EvaluationException {
        Operators.STRING_REPLACE.evaluate(new IVariable[]{sabc, sabc, sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeReplaceSmall() throws EvaluationException {
        Operators.STRING_REPLACE.evaluate(new IVariable[]{sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeReplace() throws EvaluationException {
        Operators.STRING_REPLACE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- REPLACE_REGEX -----------------------------------
     */

    @Test
    public void testStringReplaceRegex() throws EvaluationException {
        DummyVariableString szerozero = new DummyVariableString(ValueTypeString.ValueString.of("$0$0"));
        DummyVariableString sone = new DummyVariableString(ValueTypeString.ValueString.of("$1"));
        IValue res1 = Operators.STRING_REPLACE_REGEX.evaluate(new IVariable[]{sl, szerozero, shelloWorld});
        IValue res2 = Operators.STRING_REPLACE_REGEX.evaluate(new IVariable[]{sregex, sone, shelloWorld});
        IValue res3 = Operators.STRING_REPLACE_REGEX.evaluate(new IVariable[]{sregex, sempty, shelloWorld});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("hello world replace (l, '$0') = 'hellllo worlld'", ((ValueTypeString.ValueString)res1).getRawValue(), is("hellllo worlld"));
        assertThat("hello world replace (complex regex, $1) = 'hello '", ((ValueTypeString.ValueString)res2).getRawValue(), is("hello "));
        assertThat("hello world replace (complex regex, '') = ''", ((ValueTypeString.ValueString)res3).getRawValue(), is(""));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidPatternReplaceRegex() throws EvaluationException {
        Operators.STRING_REPLACE_REGEX.evaluate(new IVariable[]{sbrokenRegex, i0, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeReplaceRegexLarge() throws EvaluationException {
        Operators.STRING_REPLACE_REGEX.evaluate(new IVariable[]{sabc, sabc, sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeReplaceRegexSmall() throws EvaluationException {
        Operators.STRING_REPLACE_REGEX.evaluate(new IVariable[]{sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeReplaceRegex() throws EvaluationException {
        Operators.STRING_REPLACE_REGEX.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- JOIN -----------------------------------
     */

    @Test
    public void testStringJoin() throws EvaluationException {
        IValue res1 = Operators.STRING_JOIN.evaluate(new IVariable[]{scomma, labc});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("join(',', ['a', 'b', 'c']) = 'a,b,c'", ((ValueTypeString.ValueString) res1).getRawValue(), is("a,b,c"));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputListType() throws EvaluationException {
        Operators.STRING_JOIN.evaluate(new IVariable[]{scomma, lint});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputListTypeInner() throws EvaluationException {
        Operators.STRING_JOIN.evaluate(new IVariable[]{scomma, lstringinvalidtypes});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputListInfinite() throws EvaluationException {
        Operators.STRING_JOIN.evaluate(new IVariable[]{scomma, lstring_inf});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeJoinLarge() throws EvaluationException {
        Operators.STRING_JOIN.evaluate(new IVariable[]{scomma, labc, labc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeJoinSmall() throws EvaluationException {
        Operators.STRING_JOIN.evaluate(new IVariable[]{scomma});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeJoin() throws EvaluationException {
        Operators.STRING_JOIN.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
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

    /**
     * ----------------------------------- STRING_ERROR -----------------------------------
     */

    @Test(expected = EvaluationException.class)
    public void testStringErrorWithSymbols() throws EvaluationException {
        Operators.STRING_ERROR.evaluate(sregex);
    }

    @Test(expected = EvaluationException.class)
    public void testStringErrorWithSpaces() throws EvaluationException {
        Operators.STRING_ERROR.evaluate(shelloWorld);
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLong() throws EvaluationException {
        Operators.STRING_ERROR.evaluate(new IVariable[]{sabc, sabc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeErrorSmall() throws EvaluationException {
        Operators.STRING_ERROR.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeError() throws EvaluationException {
        Operators.STRING_ERROR.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }
}
