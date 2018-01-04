package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.*;
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
    private DummyVariable<ValueTypeLong.ValueLong> l1;
    private DummyVariableDouble d1_5;
    private DummyVariableBoolean btrue;
    private DummyVariableList ltags;
    private DummyVariableList lints;
    private DummyVariableList llongs;

    private DummyVariableNbt nempty;
    private DummyVariableNbt nsasa;
    private DummyVariableNbt nsbsb;
    private DummyVariableNbt nsasasbsc;
    private DummyVariableNbt nall;
    private DummyVariableNbt nsome;

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
        l1 = new DummyVariable<ValueTypeLong.ValueLong>(ValueTypes.LONG, ValueTypeLong.ValueLong.of(1));
        d1_5 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(1.5D));
        btrue = new DummyVariableBoolean(ValueTypeBoolean.ValueBoolean.of(true));

        nempty = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(new NBTTagCompound()));

        NBTTagCompound tsasa = new NBTTagCompound();
        tsasa.setString("a", "a");
        nsasa = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(tsasa));

        NBTTagCompound tsbsb = new NBTTagCompound();
        tsbsb.setString("b", "b");
        nsbsb = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(tsbsb));

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
        subList.appendTag(subTag);
        tall.setTag("list", subList);
        nall = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(tall));

        NBTTagCompound tsome = new NBTTagCompound();
        tsome.setByte("byte", (byte)1);
        tsome.setInteger("integer", 3);
        tsome.setFloat("float", 5.5F);
        tsome.setString("string", "seven");
        tsome.setBoolean("boolean", true);
        NBTTagCompound subTagSome = new NBTTagCompound();
        subTagSome.setString("hello", "world");
        tsome.setTag("tag", subTagSome);
        NBTTagList subListSome = new NBTTagList();
        subListSome.appendTag(subTagSome);
        tsome.setTag("list", subListSome);
        nsome = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(tsome));

        ltags = new DummyVariableList(ValueTypeList.ValueList.ofAll(
                ValueTypeNbt.ValueNbt.of(new NBTTagCompound()),
                ValueTypeNbt.ValueNbt.of(tsasa),
                ValueTypeNbt.ValueNbt.of(new NBTTagCompound())
        ));
        lints = new DummyVariableList(ValueTypeList.ValueList.ofAll(
                ValueTypeInteger.ValueInteger.of(5),
                ValueTypeInteger.ValueInteger.of(4),
                ValueTypeInteger.ValueInteger.of(3)
        ));
        llongs = new DummyVariableList(ValueTypeList.ValueList.ofAll(
                ValueTypeLong.ValueLong.of(5),
                ValueTypeLong.ValueLong.of(4),
                ValueTypeLong.ValueLong.of(3)
        ));
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
        assertThat("valuelisttag({...}, listtag).size = 2", ((ValueTypeList.ValueList) res2).getRawValue().getLength(), is(2));
        assertThat("valuelisttag({...}, listtag)[0] = ..list..", ((ValueTypeList.ValueList) res2).getRawValue().get(0), is(ValueTypeNbt.ValueNbt.of(subTag)));
        assertThat("valuelisttag({...}, listtag)[1] = ..list..", ((ValueTypeList.ValueList) res2).getRawValue().get(1), is(ValueTypeNbt.ValueNbt.of(subTag)));
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

    /**
     * ----------------------------------- WITHOUT -----------------------------------
     */

    @Test
    public void testNbtWithout() throws EvaluationException {
        IValue res1 = Operators.NBT_WITHOUT.evaluate(new IVariable[]{nsasa, sa});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("without({a:a}, a) = {}", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(new NBTTagCompound()));

        IValue res2 = Operators.NBT_WITHOUT.evaluate(new IVariable[]{nsasasbsc, sa});
        NBTTagCompound tsbc = new NBTTagCompound();
        tsbc.setString("b", "c");
        assertThat("without({a:a;b:c}, a) = {b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue(), is(tsbc));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithoutSizeLarge() throws EvaluationException {
        Operators.NBT_WITHOUT.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithoutSizeSmall() throws EvaluationException {
        Operators.NBT_WITHOUT.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithout() throws EvaluationException {
        Operators.NBT_WITHOUT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_BOOLEAN -----------------------------------
     */

    @Test
    public void testNbtWithBoolean() throws EvaluationException {
        IValue res1 = Operators.NBT_WITH_BOOLEAN.evaluate(new IVariable[]{nsasa, sc, btrue});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        NBTTagCompound t1 = new NBTTagCompound();
        t1.setString("a", "a");
        t1.setBoolean("c", true);
        assertThat("withboolean({a:a}, c, true) = {a:a,c:true}", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(t1));

        IValue res2 = Operators.NBT_WITH_BOOLEAN.evaluate(new IVariable[]{nsasasbsc, sa, btrue});
        NBTTagCompound t2 = new NBTTagCompound();
        t2.setBoolean("a", true);
        t2.setString("b", "c");
        assertThat("withboolean({a:a;b:c}, a, true) = {a:true;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithBooleanSizeLarge() throws EvaluationException {
        Operators.NBT_WITH_BOOLEAN.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithBooleanSizeSmall() throws EvaluationException {
        Operators.NBT_WITH_BOOLEAN.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithBoolean() throws EvaluationException {
        Operators.NBT_WITH_BOOLEAN.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_SHORT -----------------------------------
     */

    @Test
    public void testNbtWithShort() throws EvaluationException {
        IValue res1 = Operators.NBT_WITH_SHORT.evaluate(new IVariable[]{nsasa, sc, i1});
        assertThat("result is a short", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        NBTTagCompound t1 = new NBTTagCompound();
        t1.setString("a", "a");
        t1.setShort("c", (short) 1);
        assertThat("withshort({a:a}, c, 1) = {a:a,c:1}", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(t1));

        IValue res2 = Operators.NBT_WITH_SHORT.evaluate(new IVariable[]{nsasasbsc, sa, i1});
        NBTTagCompound t2 = new NBTTagCompound();
        t2.setShort("a", (short) 1);
        t2.setString("b", "c");
        assertThat("withshort({a:a;b:c}, a, 1) = {a:1;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithShortSizeLarge() throws EvaluationException {
        Operators.NBT_WITH_SHORT.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithShortSizeSmall() throws EvaluationException {
        Operators.NBT_WITH_SHORT.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithShort() throws EvaluationException {
        Operators.NBT_WITH_SHORT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_INTEGER -----------------------------------
     */

    @Test
    public void testNbtWithInteger() throws EvaluationException {
        IValue res1 = Operators.NBT_WITH_INTEGER.evaluate(new IVariable[]{nsasa, sc, i1});
        assertThat("result is a integer", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        NBTTagCompound t1 = new NBTTagCompound();
        t1.setString("a", "a");
        t1.setInteger("c", 1);
        assertThat("withinteger({a:a}, c, 1) = {a:a,c:1}", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(t1));

        IValue res2 = Operators.NBT_WITH_INTEGER.evaluate(new IVariable[]{nsasasbsc, sa, i1});
        NBTTagCompound t2 = new NBTTagCompound();
        t2.setInteger("a", 1);
        t2.setString("b", "c");
        assertThat("withinteger({a:a;b:c}, a, 1) = {a:1;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithIntegerSizeLarge() throws EvaluationException {
        Operators.NBT_WITH_INTEGER.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithIntegerSizeSmall() throws EvaluationException {
        Operators.NBT_WITH_INTEGER.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithInteger() throws EvaluationException {
        Operators.NBT_WITH_INTEGER.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_LONG -----------------------------------
     */

    @Test
    public void testNbtWithLong() throws EvaluationException {
        IValue res1 = Operators.NBT_WITH_LONG.evaluate(new IVariable[]{nsasa, sc, l1});
        assertThat("result is a long", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        NBTTagCompound t1 = new NBTTagCompound();
        t1.setString("a", "a");
        t1.setLong("c", 1L);
        assertThat("withlong({a:a}, c, 1) = {a:a,c:1}", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(t1));

        IValue res2 = Operators.NBT_WITH_LONG.evaluate(new IVariable[]{nsasasbsc, sa, l1});
        NBTTagCompound t2 = new NBTTagCompound();
        t2.setLong("a", 1L);
        t2.setString("b", "c");
        assertThat("withlong({a:a;b:c}, a, 1) = {a:1;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithLongSizeLarge() throws EvaluationException {
        Operators.NBT_WITH_LONG.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithLongSizeSmall() throws EvaluationException {
        Operators.NBT_WITH_LONG.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithLong() throws EvaluationException {
        Operators.NBT_WITH_LONG.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_DOUBLE -----------------------------------
     */

    @Test
    public void testNbtWithDouble() throws EvaluationException {
        IValue res1 = Operators.NBT_WITH_DOUBLE.evaluate(new IVariable[]{nsasa, sc, d1_5});
        assertThat("result is a double", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        NBTTagCompound t1 = new NBTTagCompound();
        t1.setString("a", "a");
        t1.setDouble("c", 1.5D);
        assertThat("withdouble({a:a}, c, 1.5) = {a:a,c:1.5}", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(t1));

        IValue res2 = Operators.NBT_WITH_DOUBLE.evaluate(new IVariable[]{nsasasbsc, sa, d1_5});
        NBTTagCompound t2 = new NBTTagCompound();
        t2.setDouble("a", 1.5D);
        t2.setString("b", "c");
        assertThat("withdouble({a:a;b:c}, a, 1.5) = {a:1.5;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithDoubleSizeLarge() throws EvaluationException {
        Operators.NBT_WITH_DOUBLE.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithDoubleSizeSmall() throws EvaluationException {
        Operators.NBT_WITH_DOUBLE.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithDouble() throws EvaluationException {
        Operators.NBT_WITH_DOUBLE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_FLOAT -----------------------------------
     */

    @Test
    public void testNbtWithFloat() throws EvaluationException {
        IValue res1 = Operators.NBT_WITH_FLOAT.evaluate(new IVariable[]{nsasa, sc, d1_5});
        assertThat("result is a float", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        NBTTagCompound t1 = new NBTTagCompound();
        t1.setString("a", "a");
        t1.setFloat("c", 1.5F);
        assertThat("withfloat({a:a}, c, 1.5) = {a:a,c:1.5}", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(t1));

        IValue res2 = Operators.NBT_WITH_FLOAT.evaluate(new IVariable[]{nsasasbsc, sa, d1_5});
        NBTTagCompound t2 = new NBTTagCompound();
        t2.setFloat("a", 1.5F);
        t2.setString("b", "c");
        assertThat("withfloat({a:a;b:c}, a, 1.5) = {a:1.5;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithFloatSizeLarge() throws EvaluationException {
        Operators.NBT_WITH_FLOAT.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithFloatSizeSmall() throws EvaluationException {
        Operators.NBT_WITH_FLOAT.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithFloat() throws EvaluationException {
        Operators.NBT_WITH_FLOAT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_STRING -----------------------------------
     */

    @Test
    public void testNbtWithString() throws EvaluationException {
        IValue res1 = Operators.NBT_WITH_STRING.evaluate(new IVariable[]{nsasa, sc, sc});
        assertThat("result is a string", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        NBTTagCompound t1 = new NBTTagCompound();
        t1.setString("a", "a");
        t1.setString("c", "c");
        assertThat("withstring({a:a}, c, c) = {a:a,c:c}", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(t1));

        IValue res2 = Operators.NBT_WITH_STRING.evaluate(new IVariable[]{nsasasbsc, sa, sc});
        NBTTagCompound t2 = new NBTTagCompound();
        t2.setString("a", "c");
        t2.setString("b", "c");
        assertThat("withstring({a:a;b:c}, a, c) = {a:c;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithStringSizeLarge() throws EvaluationException {
        Operators.NBT_WITH_STRING.evaluate(new IVariable[]{nempty, sa, i1});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithStringSizeSmall() throws EvaluationException {
        Operators.NBT_WITH_STRING.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithString() throws EvaluationException {
        Operators.NBT_WITH_STRING.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_TAG -----------------------------------
     */

    @Test
    public void testNbtWithTag() throws EvaluationException {
        NBTTagCompound tsasa = new NBTTagCompound();
        tsasa.setString("a", "a");

        IValue res1 = Operators.NBT_WITH_TAG.evaluate(new IVariable[]{nsasa, sc, nsasa});
        assertThat("result is a tag", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        NBTTagCompound t1 = new NBTTagCompound();
        t1.setString("a", "a");
        t1.setTag("c", tsasa);
        assertThat("withtag({a:a}, c, tag) = {a:a,c:tag}", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(t1));

        IValue res2 = Operators.NBT_WITH_TAG.evaluate(new IVariable[]{nsasasbsc, sa, nsasa});
        NBTTagCompound t2 = new NBTTagCompound();
        t2.setTag("a", tsasa);
        t2.setString("b", "c");
        assertThat("withtag({a:a;b:c}, a, tag) = {a:tag;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithTagSizeLarge() throws EvaluationException {
        Operators.NBT_WITH_TAG.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithTagSizeSmall() throws EvaluationException {
        Operators.NBT_WITH_TAG.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithTag() throws EvaluationException {
        Operators.NBT_WITH_TAG.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_LIST_TAG -----------------------------------
     */

    @Test
    public void testNbtWithListTag() throws EvaluationException {
        NBTTagCompound tsasa = new NBTTagCompound();
        tsasa.setString("a", "a");
        NBTTagList tlist = new NBTTagList();
        tlist.appendTag(new NBTTagCompound());
        tlist.appendTag(tsasa);
        tlist.appendTag(new NBTTagCompound());

        IValue res1 = Operators.NBT_WITH_LIST_TAG.evaluate(new IVariable[]{nsasa, sc, ltags});
        assertThat("result is a listtag", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        NBTTagCompound t1 = new NBTTagCompound();
        t1.setString("a", "a");
        t1.setTag("c", tlist);
        assertThat("withlisttag({a:a}, c, listtag) = {a:a,c:listtag}", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(t1));

        IValue res2 = Operators.NBT_WITH_LIST_TAG.evaluate(new IVariable[]{nsasasbsc, sa, ltags});
        NBTTagCompound t2 = new NBTTagCompound();
        t2.setTag("a", tlist);
        t2.setString("b", "c");
        assertThat("withlisttag({a:a;b:c}, a, listtag) = {a:listtag;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithListTagSizeLarge() throws EvaluationException {
        Operators.NBT_WITH_LIST_TAG.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithListTagSizeSmall() throws EvaluationException {
        Operators.NBT_WITH_LIST_TAG.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithListTag() throws EvaluationException {
        Operators.NBT_WITH_LIST_TAG.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_LIST_BYTE -----------------------------------
     */

    @Test
    public void testNbtWithListByte() throws EvaluationException {
        NBTTagCompound tsasa = new NBTTagCompound();
        tsasa.setString("a", "a");
        NBTTagList tlist = new NBTTagList();
        tlist.appendTag(new NBTTagByte((byte) 5));
        tlist.appendTag(new NBTTagByte((byte) 4));
        tlist.appendTag(new NBTTagByte((byte) 3));

        IValue res1 = Operators.NBT_WITH_LIST_BYTE.evaluate(new IVariable[]{nsasa, sc, lints});
        assertThat("result is a listbyte", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        NBTTagCompound t1 = new NBTTagCompound();
        t1.setString("a", "a");
        t1.setTag("c", tlist);
        assertThat("withlistbyte({a:a}, c, listbyte) = {a:a,c:listbyte}", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(t1));

        IValue res2 = Operators.NBT_WITH_LIST_BYTE.evaluate(new IVariable[]{nsasasbsc, sa, lints});
        NBTTagCompound t2 = new NBTTagCompound();
        t2.setTag("a", tlist);
        t2.setString("b", "c");
        assertThat("withlistbyte({a:a;b:c}, a, listbyte) = {a:listbyte;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithListByteSizeLarge() throws EvaluationException {
        Operators.NBT_WITH_LIST_BYTE.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithListByteSizeSmall() throws EvaluationException {
        Operators.NBT_WITH_LIST_BYTE.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithListByte() throws EvaluationException {
        Operators.NBT_WITH_LIST_BYTE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_LIST_INT -----------------------------------
     */

    @Test
    public void testNbtWithListInt() throws EvaluationException {
        NBTTagCompound tsasa = new NBTTagCompound();
        tsasa.setString("a", "a");
        NBTTagList tlist = new NBTTagList();
        tlist.appendTag(new NBTTagInt(5));
        tlist.appendTag(new NBTTagInt(4));
        tlist.appendTag(new NBTTagInt(3));

        IValue res1 = Operators.NBT_WITH_LIST_INT.evaluate(new IVariable[]{nsasa, sc, lints});
        assertThat("result is a listint", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        NBTTagCompound t1 = new NBTTagCompound();
        t1.setString("a", "a");
        t1.setTag("c", tlist);
        assertThat("withlistint({a:a}, c, listint) = {a:a,c:listint}", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(t1));

        IValue res2 = Operators.NBT_WITH_LIST_INT.evaluate(new IVariable[]{nsasasbsc, sa, lints});
        NBTTagCompound t2 = new NBTTagCompound();
        t2.setTag("a", tlist);
        t2.setString("b", "c");
        assertThat("withlistint({a:a;b:c}, a, listint) = {a:listint;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithListIntSizeLarge() throws EvaluationException {
        Operators.NBT_WITH_LIST_INT.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithListIntSizeSmall() throws EvaluationException {
        Operators.NBT_WITH_LIST_INT.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithListInt() throws EvaluationException {
        Operators.NBT_WITH_LIST_INT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_LIST_LONG -----------------------------------
     */

    @Test
    public void testNbtWithListLong() throws EvaluationException {
        NBTTagCompound tsasa = new NBTTagCompound();
        tsasa.setString("a", "a");
        NBTTagList tlist = new NBTTagList();
        tlist.appendTag(new NBTTagLong(5));
        tlist.appendTag(new NBTTagLong(4));
        tlist.appendTag(new NBTTagLong(3));

        IValue res1 = Operators.NBT_WITH_LIST_LONG.evaluate(new IVariable[]{nsasa, sc, llongs});
        assertThat("result is a listlong", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        NBTTagCompound t1 = new NBTTagCompound();
        t1.setString("a", "a");
        t1.setTag("c", tlist);
        assertThat("withlistlong({a:a}, c, listlong) = {a:a,c:listlong}", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(t1));

        IValue res2 = Operators.NBT_WITH_LIST_LONG.evaluate(new IVariable[]{nsasasbsc, sa, llongs});
        NBTTagCompound t2 = new NBTTagCompound();
        t2.setTag("a", tlist);
        t2.setString("b", "c");
        assertThat("withlistlong({a:a;b:c}, a, listlong) = {a:listlong;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithListLongSizeLarge() throws EvaluationException {
        Operators.NBT_WITH_LIST_LONG.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithListLongSizeSmall() throws EvaluationException {
        Operators.NBT_WITH_LIST_LONG.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithListLong() throws EvaluationException {
        Operators.NBT_WITH_LIST_LONG.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- SUBSET -----------------------------------
     */

    @Test
    public void testNbtSubset() throws EvaluationException {
        IValue res1 = Operators.NBT_SUBSET.evaluate(new IVariable[]{nempty, nall});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("subset({}, all) = true", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));

        IValue res2 = Operators.NBT_SUBSET.evaluate(new IVariable[]{nall, nempty});
        assertThat("subset(all, {}) = false", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(false));

        IValue res3 = Operators.NBT_SUBSET.evaluate(new IVariable[]{nsome, nall});
        assertThat("subset(some, all) = true", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(true));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtSubsetSizeLarge() throws EvaluationException {
        Operators.NBT_SUBSET.evaluate(new IVariable[]{nempty, nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtSubsetSizeSmall() throws EvaluationException {
        Operators.NBT_SUBSET.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtSubset() throws EvaluationException {
        Operators.NBT_SUBSET.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- UNION -----------------------------------
     */

    @Test
    public void testNbtUnion() throws EvaluationException {
        IValue res1 = Operators.NBT_UNION.evaluate(new IVariable[]{nempty, nall});
        assertThat("result is an nbt tag", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("union({}, all) = all", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(nall.getValue().getRawValue()));

        IValue res2 = Operators.NBT_UNION.evaluate(new IVariable[]{nall, nempty});
        assertThat("union(all, {}) = all", ((ValueTypeNbt.ValueNbt) res2).getRawValue(), is(nall.getValue().getRawValue()));

        IValue res3 = Operators.NBT_UNION.evaluate(new IVariable[]{nsome, nall});
        assertThat("union(some, all) = all", ((ValueTypeNbt.ValueNbt) res3).getRawValue(), is(nall.getValue().getRawValue()));

        IValue res4 = Operators.NBT_UNION.evaluate(new IVariable[]{nsasa, nsbsb});
        NBTTagCompound tsasasbsb = new NBTTagCompound();
        tsasasbsb.setString("a", "a");
        tsasasbsb.setString("b", "b");
        assertThat("union(sasa, sbsb) = sasasbsb", ((ValueTypeNbt.ValueNbt) res4).getRawValue(), is(tsasasbsb));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtUnionSizeLarge() throws EvaluationException {
        Operators.NBT_UNION.evaluate(new IVariable[]{nempty, nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtUnionSizeSmall() throws EvaluationException {
        Operators.NBT_UNION.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtUnion() throws EvaluationException {
        Operators.NBT_UNION.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- INTERSECTION -----------------------------------
     */

    @Test
    public void testNbtIntersection() throws EvaluationException {
        IValue res1 = Operators.NBT_INTERSECTION.evaluate(new IVariable[]{nempty, nall});
        assertThat("result is an nbt tag", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("intersection({}, all) = {}", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(new NBTTagCompound()));

        IValue res2 = Operators.NBT_INTERSECTION.evaluate(new IVariable[]{nall, nempty});
        assertThat("intersection(all, {}) = {}", ((ValueTypeNbt.ValueNbt) res2).getRawValue(), is(new NBTTagCompound()));

        IValue res3 = Operators.NBT_INTERSECTION.evaluate(new IVariable[]{nsome, nall});
        assertThat("intersection(some, all) = some", ((ValueTypeNbt.ValueNbt) res3).getRawValue(), is(nsome.getValue().getRawValue()));

        IValue res4 = Operators.NBT_INTERSECTION.evaluate(new IVariable[]{nsasa, nsbsb});
        assertThat("intersection(sasa, sbsb) = {}", ((ValueTypeNbt.ValueNbt) res4).getRawValue(), is(new NBTTagCompound()));

        IValue res5 = Operators.NBT_INTERSECTION.evaluate(new IVariable[]{nsasa, nsasasbsc});
        assertThat("intersection(sasa, sasasbsc) = {}", ((ValueTypeNbt.ValueNbt) res5).getRawValue(), is(nsasa.getValue().getRawValue()));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtIntersectionSizeLarge() throws EvaluationException {
        Operators.NBT_INTERSECTION.evaluate(new IVariable[]{nempty, nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtIntersectionSizeSmall() throws EvaluationException {
        Operators.NBT_INTERSECTION.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtIntersection() throws EvaluationException {
        Operators.NBT_INTERSECTION.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- MINUS -----------------------------------
     */

    @Test
    public void testNbtMinus() throws EvaluationException {
        IValue res1 = Operators.NBT_MINUS.evaluate(new IVariable[]{nempty, nall});
        assertThat("result is an nbt tag", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("minus({}, all) = {}", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(new NBTTagCompound()));

        IValue res2 = Operators.NBT_MINUS.evaluate(new IVariable[]{nall, nempty});
        assertThat("minus(all, {}) = all", ((ValueTypeNbt.ValueNbt) res2).getRawValue(), is(nall.getValue().getRawValue()));

        IValue res3 = Operators.NBT_MINUS.evaluate(new IVariable[]{nsome, nall});
        assertThat("minus(some, all) = {}", ((ValueTypeNbt.ValueNbt) res3).getRawValue(), is(new NBTTagCompound()));

        IValue res4 = Operators.NBT_MINUS.evaluate(new IVariable[]{nsasa, nsbsb});
        assertThat("minus(sasa, sbsb) = sasa", ((ValueTypeNbt.ValueNbt) res4).getRawValue(), is(nsasa.getValue().getRawValue()));

        IValue res5 = Operators.NBT_MINUS.evaluate(new IVariable[]{nsasasbsc, nsasa});
        NBTTagCompound tsbsc = new NBTTagCompound();
        tsbsc.setString("b", "c");
        assertThat("minus(sasasbsc, sasa) = {}", ((ValueTypeNbt.ValueNbt) res5).getRawValue(), is(tsbsc));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtMinusSizeLarge() throws EvaluationException {
        Operators.NBT_MINUS.evaluate(new IVariable[]{nempty, nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtMinusSizeSmall() throws EvaluationException {
        Operators.NBT_MINUS.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtMinus() throws EvaluationException {
        Operators.NBT_MINUS.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

}
