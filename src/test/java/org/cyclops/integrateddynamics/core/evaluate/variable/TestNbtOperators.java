package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
 * Test the different NBT operators.
 * @author rubensworks
 */
public class TestNbtOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableString sa;
    private DummyVariableString sb;
    private DummyVariableString sc;
    private DummyVariableString sbyte;
    private DummyVariableString sshort;
    private DummyVariableString sinteger;
    private DummyVariableString slong;
    private DummyVariableString sfloat;
    private DummyVariableString sdouble;
    private DummyVariableString sstring;
    private DummyVariableString sbytearray;
    private DummyVariableString sintarray;
    private DummyVariableString sboolean;
    private DummyVariableString stag;
    private DummyVariableString slist;
    private DummyVariableInteger i1;
    private DummyVariableDouble d1_5;

    private DummyVariableNbt nempty;
    private DummyVariableNbt nsasa;
    private DummyVariableNbt nsasasbsc;
    private DummyVariableNbt nall;

    @BeforeClass
    public static void beforeClass() {
        ValueTypeListProxyFactories.load();
    }

    @Before
    public void before() {
        sa = new DummyVariableString(ValueTypeString.ValueString.of("a"));
        sb = new DummyVariableString(ValueTypeString.ValueString.of("b"));
        sc = new DummyVariableString(ValueTypeString.ValueString.of("c"));
        sbyte = new DummyVariableString(ValueTypeString.ValueString.of("byte"));
        sshort = new DummyVariableString(ValueTypeString.ValueString.of("short"));
        sinteger = new DummyVariableString(ValueTypeString.ValueString.of("integer"));
        slong = new DummyVariableString(ValueTypeString.ValueString.of("long"));
        sfloat = new DummyVariableString(ValueTypeString.ValueString.of("float"));
        sdouble = new DummyVariableString(ValueTypeString.ValueString.of("double"));
        sstring = new DummyVariableString(ValueTypeString.ValueString.of("string"));
        sbytearray = new DummyVariableString(ValueTypeString.ValueString.of("bytearray"));
        sintarray = new DummyVariableString(ValueTypeString.ValueString.of("intarray"));
        sboolean = new DummyVariableString(ValueTypeString.ValueString.of("boolean"));
        stag = new DummyVariableString(ValueTypeString.ValueString.of("tag"));
        slist = new DummyVariableString(ValueTypeString.ValueString.of("list"));

        i1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1));
        d1_5 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(1.5D));

        nempty = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(new NBTTagCompound()));

        NBTTagCompound tsasa = new NBTTagCompound();
        tsasa.setString("a", "a");
        nsasa = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(tsasa));

        NBTTagCompound tsasasbsc = new NBTTagCompound();
        tsasasbsc.setString("a", "a");
        tsasasbsc.setString("b", "c");
        nsasasbsc = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(tsasasbsc));

        NBTTagCompound tall = new NBTTagCompound();
        tall.setByte("byte", (byte)1);
        tall.setShort("short", (short) 2);
        tall.setInteger("integer", 3);
        tall.setLong("long", 4L);
        tall.setFloat("float", 5.5F);
        tall.setDouble("double", 6.5D);
        tall.setString("string", "seven");
        tall.setByteArray("bytearray", new byte[]{8,9,10});
        tall.setIntArray("intarray", new int[]{11, 12, 13});
        tall.setBoolean("boolean", true);
        NBTTagCompound subTag = new NBTTagCompound();
        subTag.setString("hello", "world");
        tall.setTag("tag", subTag);
        NBTTagList subList = new NBTTagList();
        subList.appendTag(subTag);
        tall.setTag("list", subList);
        nall = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(tall));
    }

    /**
     * ----------------------------------- SIZE -----------------------------------
     */

    @Test
    public void testNbtSize() throws EvaluationException {
        IValue res1 = Operators.NBT_SIZE.evaluate(new IVariable[]{nempty});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("size({}) = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));

        IValue res2 = Operators.NBT_SIZE.evaluate(new IVariable[]{nsasa});
        assertThat("size({a:a}) = 1", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(1));

        IValue res3 = Operators.NBT_SIZE.evaluate(new IVariable[]{nsasasbsc});
        assertThat("size({a:a;b:b}) = 2", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtSizeSizeLarge() throws EvaluationException {
        Operators.NBT_SIZE.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtSizeSizeSmall() throws EvaluationException {
        Operators.NBT_SIZE.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtSize() throws EvaluationException {
        Operators.NBT_SIZE.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- KEYS -----------------------------------
     */

    @Test
    public void testNbtKeys() throws EvaluationException {
        IValue res1 = Operators.NBT_KEYS.evaluate(new IVariable[]{nempty});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        assertThat("keys({}).size = 0", ((ValueTypeList.ValueList) res1).getRawValue().getLength(), is(0));

        IValue res2 = Operators.NBT_KEYS.evaluate(new IVariable[]{nsasasbsc});
        assertThat("keys({a:a;b:b}).size = 2", ((ValueTypeList.ValueList) res2).getRawValue().getLength(), is(2));
        assertThat("keys({a:a;b:b})[0] = a", ((ValueTypeList.ValueList) res2).getRawValue().get(0), is(ValueTypeString.ValueString.of("a")));
        assertThat("keys({a:a;b:b})[1] = b", ((ValueTypeList.ValueList) res2).getRawValue().get(1), is(ValueTypeString.ValueString.of("b")));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtKeysSizeLarge() throws EvaluationException {
        Operators.NBT_KEYS.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtKeysSizeSmall() throws EvaluationException {
        Operators.NBT_KEYS.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtKeys() throws EvaluationException {
        Operators.NBT_KEYS.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- HASKEY -----------------------------------
     */

    @Test
    public void testNbtHasKey() throws EvaluationException {
        IValue res1 = Operators.NBT_HASKEY.evaluate(new IVariable[]{nempty, sa});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("haskey({}, a) = false", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(false));

        IValue res2 = Operators.NBT_HASKEY.evaluate(new IVariable[]{nsasasbsc, sa});
        assertThat("haskey({a:a}, a) = true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtHasKeySizeLarge() throws EvaluationException {
        Operators.NBT_HASKEY.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtHasKeySizeSmall() throws EvaluationException {
        Operators.NBT_HASKEY.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtHasKey() throws EvaluationException {
        Operators.NBT_HASKEY.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_TYPE -----------------------------------
     */

    @Test
    public void testNbtValueType() throws EvaluationException {
        IValue res1 = Operators.NBT_VALUE_TYPE.evaluate(new IVariable[]{nempty, sa});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("valuetype({}, a) = null", ((ValueTypeString.ValueString) res1).getRawValue(), is("null"));

        assertThat("valuetype({...}, byte) = byte", ((ValueTypeString.ValueString)
                Operators.NBT_VALUE_TYPE.evaluate(new IVariable[]{nall, sbyte})).getRawValue(), is("BYTE"));
        assertThat("valuetype({...}, short) = short", ((ValueTypeString.ValueString)
                Operators.NBT_VALUE_TYPE.evaluate(new IVariable[]{nall, sshort})).getRawValue(), is("SHORT"));
        assertThat("valuetype({...}, integer) = integer", ((ValueTypeString.ValueString)
                Operators.NBT_VALUE_TYPE.evaluate(new IVariable[]{nall, sinteger})).getRawValue(), is("INT"));
        assertThat("valuetype({...}, long) = long", ((ValueTypeString.ValueString)
                Operators.NBT_VALUE_TYPE.evaluate(new IVariable[]{nall, slong})).getRawValue(), is("LONG"));
        assertThat("valuetype({...}, float) = float", ((ValueTypeString.ValueString)
                Operators.NBT_VALUE_TYPE.evaluate(new IVariable[]{nall, sfloat})).getRawValue(), is("FLOAT"));
        assertThat("valuetype({...}, double) = double", ((ValueTypeString.ValueString)
                Operators.NBT_VALUE_TYPE.evaluate(new IVariable[]{nall, sdouble})).getRawValue(), is("DOUBLE"));
        assertThat("valuetype({...}, string) = string", ((ValueTypeString.ValueString)
                Operators.NBT_VALUE_TYPE.evaluate(new IVariable[]{nall, sstring})).getRawValue(), is("STRING"));
        assertThat("valuetype({...}, bytearray) = bytearray", ((ValueTypeString.ValueString)
                Operators.NBT_VALUE_TYPE.evaluate(new IVariable[]{nall, sbytearray})).getRawValue(), is("BYTE[]"));
        assertThat("valuetype({...}, intarray) = intarray", ((ValueTypeString.ValueString)
                Operators.NBT_VALUE_TYPE.evaluate(new IVariable[]{nall, sintarray})).getRawValue(), is("INT[]"));
        assertThat("valuetype({...}, boolean) = byte", ((ValueTypeString.ValueString)
                Operators.NBT_VALUE_TYPE.evaluate(new IVariable[]{nall, sboolean})).getRawValue(), is("BYTE"));
        assertThat("valuetype({...}, tag) = tag", ((ValueTypeString.ValueString)
                Operators.NBT_VALUE_TYPE.evaluate(new IVariable[]{nall, stag})).getRawValue(), is("COMPOUND"));
        assertThat("valuetype({...}, list) = list", ((ValueTypeString.ValueString)
                Operators.NBT_VALUE_TYPE.evaluate(new IVariable[]{nall, slist})).getRawValue(), is("LIST"));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueTypeSizeLarge() throws EvaluationException {
        Operators.NBT_VALUE_TYPE.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueTypeSizeSmall() throws EvaluationException {
        Operators.NBT_VALUE_TYPE.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueType() throws EvaluationException {
        Operators.NBT_VALUE_TYPE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_BOOLEAN -----------------------------------
     */

    @Test
    public void testNbtValueBoolean() throws EvaluationException {
        IValue res1 = Operators.NBT_VALUE_BOOLEAN.evaluate(new IVariable[]{nempty, sboolean});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("valueboolean({}, boolean) = false", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(false));

        IValue res2 = Operators.NBT_VALUE_BOOLEAN.evaluate(new IVariable[]{nall, sboolean});
        assertThat("valueboolean({...}, boolean) = true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));

        IValue res3 = Operators.NBT_VALUE_BOOLEAN.evaluate(new IVariable[]{nall, sbyte});
        assertThat("valueboolean({...}, byte) = true", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(true));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueBooleanSizeLarge() throws EvaluationException {
        Operators.NBT_VALUE_BOOLEAN.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueBooleanSizeSmall() throws EvaluationException {
        Operators.NBT_VALUE_BOOLEAN.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueBoolean() throws EvaluationException {
        Operators.NBT_VALUE_BOOLEAN.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_INTEGER -----------------------------------
     */

    @Test
    public void testNbtValueInteger() throws EvaluationException {
        IValue res1 = Operators.NBT_VALUE_INTEGER.evaluate(new IVariable[]{nempty, sinteger});
        assertThat("result is a integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("valueinteger({}, integer) = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));

        IValue res2 = Operators.NBT_VALUE_INTEGER.evaluate(new IVariable[]{nall, sinteger});
        assertThat("valueinteger({...}, integer) = 3", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(3));

        IValue res3 = Operators.NBT_VALUE_INTEGER.evaluate(new IVariable[]{nall, sbyte});
        assertThat("valueinteger({...}, byte) = 3", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(1));

        IValue res4 = Operators.NBT_VALUE_INTEGER.evaluate(new IVariable[]{nall, sshort});
        assertThat("valueinteger({...}, short) = 2", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueIntegerSizeLarge() throws EvaluationException {
        Operators.NBT_VALUE_INTEGER.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueIntegerSizeSmall() throws EvaluationException {
        Operators.NBT_VALUE_INTEGER.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueInteger() throws EvaluationException {
        Operators.NBT_VALUE_INTEGER.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_LONG -----------------------------------
     */

    @Test
    public void testNbtValueLong() throws EvaluationException {
        IValue res1 = Operators.NBT_VALUE_LONG.evaluate(new IVariable[]{nempty, slong});
        assertThat("result is a long", res1, instanceOf(ValueTypeLong.ValueLong.class));
        assertThat("valuelong({}, long) = 0", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(0L));

        IValue res2 = Operators.NBT_VALUE_LONG.evaluate(new IVariable[]{nall, slong});
        assertThat("valuelong({...}, long) = 4", ((ValueTypeLong.ValueLong) res2).getRawValue(), is(4L));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueLongSizeLarge() throws EvaluationException {
        Operators.NBT_VALUE_LONG.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueLongSizeSmall() throws EvaluationException {
        Operators.NBT_VALUE_LONG.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueLong() throws EvaluationException {
        Operators.NBT_VALUE_LONG.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_DOUBLE -----------------------------------
     */

    @Test
    public void testNbtValueDouble() throws EvaluationException {
        IValue res1 = Operators.NBT_VALUE_DOUBLE.evaluate(new IVariable[]{nempty, sdouble});
        assertThat("result is a double", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("valuedouble({}, double) = 0", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(0D));

        IValue res2 = Operators.NBT_VALUE_DOUBLE.evaluate(new IVariable[]{nall, sdouble});
        assertThat("valuedouble({...}, double) = 6.5", ((ValueTypeDouble.ValueDouble) res2).getRawValue(), is(6.5D));

        IValue res3 = Operators.NBT_VALUE_DOUBLE.evaluate(new IVariable[]{nall, sfloat});
        assertThat("valuedouble({...}, float) = 5.5", ((ValueTypeDouble.ValueDouble) res3).getRawValue(), is(5.5D));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueDoubleSizeLarge() throws EvaluationException {
        Operators.NBT_VALUE_DOUBLE.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueDoubleSizeSmall() throws EvaluationException {
        Operators.NBT_VALUE_DOUBLE.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueDouble() throws EvaluationException {
        Operators.NBT_VALUE_DOUBLE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_STRING -----------------------------------
     */

    @Test
    public void testNbtValueString() throws EvaluationException {
        IValue res1 = Operators.NBT_VALUE_STRING.evaluate(new IVariable[]{nempty, sstring});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("valuestring({}, string) = ", ((ValueTypeString.ValueString) res1).getRawValue(), is(""));

        IValue res2 = Operators.NBT_VALUE_STRING.evaluate(new IVariable[]{nall, sstring});
        assertThat("valuestring({...}, string) = seven", ((ValueTypeString.ValueString) res2).getRawValue(), is("seven"));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueStringSizeLarge() throws EvaluationException {
        Operators.NBT_VALUE_STRING.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueStringSizeSmall() throws EvaluationException {
        Operators.NBT_VALUE_STRING.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueString() throws EvaluationException {
        Operators.NBT_VALUE_STRING.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_TAG -----------------------------------
     */

    @Test
    public void testNbtValueTag() throws EvaluationException {
        IValue res1 = Operators.NBT_VALUE_TAG.evaluate(new IVariable[]{nempty, stag});
        assertThat("result is a tag", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("valuetag({}, tag) = {}", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(new NBTTagCompound()));

        IValue res2 = Operators.NBT_VALUE_TAG.evaluate(new IVariable[]{nall, stag});
        NBTTagCompound subTag = new NBTTagCompound();
        subTag.setString("hello", "world");
        assertThat("valuetag({...}, tag) = {hello:world}", ((ValueTypeNbt.ValueNbt) res2).getRawValue(), is(subTag));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueTagSizeLarge() throws EvaluationException {
        Operators.NBT_VALUE_TAG.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueTagSizeSmall() throws EvaluationException {
        Operators.NBT_VALUE_TAG.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueTag() throws EvaluationException {
        Operators.NBT_VALUE_TAG.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_LIST_TAG -----------------------------------
     */

    @Test
    public void testNbtValueListTag() throws EvaluationException {
        IValue res1 = Operators.NBT_VALUE_LIST_TAG.evaluate(new IVariable[]{nempty, slist});
        assertThat("result is a listtag", res1, instanceOf(ValueTypeList.ValueList.class));
        assertThat("valuelisttag({}, listtag).size = 0", ((ValueTypeList.ValueList) res1).getRawValue().getLength(), is(0));

        IValue res2 = Operators.NBT_VALUE_LIST_TAG.evaluate(new IVariable[]{nall, slist});
        NBTTagCompound subTag = new NBTTagCompound();
        subTag.setString("hello", "world");
        NBTTagList subList = new NBTTagList();
        subList.appendTag(subTag);
        assertThat("valuelisttag({...}, listtag).size = 1", ((ValueTypeList.ValueList) res2).getRawValue().getLength(), is(1));
        assertThat("valuelisttag({...}, listtag)[0] = ..list..", ((ValueTypeList.ValueList) res2).getRawValue().get(0), is(ValueTypeNbt.ValueNbt.of(subTag)));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueListTagSizeLarge() throws EvaluationException {
        Operators.NBT_VALUE_LIST_TAG.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueListTagSizeSmall() throws EvaluationException {
        Operators.NBT_VALUE_LIST_TAG.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueListTag() throws EvaluationException {
        Operators.NBT_VALUE_LIST_TAG.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_LIST_INT -----------------------------------
     */

    @Test
    public void testNbtValueListInt() throws EvaluationException {
        IValue res1 = Operators.NBT_VALUE_LIST_INT.evaluate(new IVariable[]{nempty, slist});
        assertThat("result is a listint", res1, instanceOf(ValueTypeList.ValueList.class));
        assertThat("valuelistint({}, listint).size = 0", ((ValueTypeList.ValueList) res1).getRawValue().getLength(), is(0));

        IValue res2 = Operators.NBT_VALUE_LIST_INT.evaluate(new IVariable[]{nall, sintarray});
        assertThat("valuelistint({...}, listint).size = 3", ((ValueTypeList.ValueList) res2).getRawValue().getLength(), is(3));
        assertThat("valuelistint({...}, listint)[0] = 11", ((ValueTypeList.ValueList) res2).getRawValue().get(0), is(ValueTypeInteger.ValueInteger.of(11)));
        assertThat("valuelistint({...}, listint)[1] = 12", ((ValueTypeList.ValueList) res2).getRawValue().get(1), is(ValueTypeInteger.ValueInteger.of(12)));
        assertThat("valuelistint({...}, listint)[2] = 13", ((ValueTypeList.ValueList) res2).getRawValue().get(2), is(ValueTypeInteger.ValueInteger.of(13)));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueListIntSizeLarge() throws EvaluationException {
        Operators.NBT_VALUE_LIST_INT.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueListIntSizeSmall() throws EvaluationException {
        Operators.NBT_VALUE_LIST_INT.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueListInt() throws EvaluationException {
        Operators.NBT_VALUE_LIST_INT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_LIST_BYTE -----------------------------------
     */

    @Test
    public void testNbtValueListByte() throws EvaluationException {
        IValue res1 = Operators.NBT_VALUE_LIST_BYTE.evaluate(new IVariable[]{nempty, slist});
        assertThat("result is a listbyte", res1, instanceOf(ValueTypeList.ValueList.class));
        assertThat("valuelistbyte({}, listbyte).size = 0", ((ValueTypeList.ValueList) res1).getRawValue().getLength(), is(0));

        IValue res2 = Operators.NBT_VALUE_LIST_BYTE.evaluate(new IVariable[]{nall, sbytearray});
        assertThat("valuelistbyte({...}, listbyte).size = 3", ((ValueTypeList.ValueList) res2).getRawValue().getLength(), is(3));
        assertThat("valuelistbyte({...}, listbyte)[0] = 8", ((ValueTypeList.ValueList) res2).getRawValue().get(0), is(ValueTypeInteger.ValueInteger.of(8)));
        assertThat("valuelistbyte({...}, listbyte)[1] = 9", ((ValueTypeList.ValueList) res2).getRawValue().get(1), is(ValueTypeInteger.ValueInteger.of(9)));
        assertThat("valuelistbyte({...}, listbyte)[2] = 10", ((ValueTypeList.ValueList) res2).getRawValue().get(2), is(ValueTypeInteger.ValueInteger.of(10)));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueListByteSizeLarge() throws EvaluationException {
        Operators.NBT_VALUE_LIST_BYTE.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueListByteSizeSmall() throws EvaluationException {
        Operators.NBT_VALUE_LIST_BYTE.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueListByte() throws EvaluationException {
        Operators.NBT_VALUE_LIST_BYTE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

}
