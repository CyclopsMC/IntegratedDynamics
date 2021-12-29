package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.*;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

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
    private DummyVariableString slongarray;
    private DummyVariableString sboolean;
    private DummyVariableString scompound;
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

    private DummyVariableNbt nboolean;
    private DummyVariableNbt nbyte;
    private DummyVariableNbt nshort;
    private DummyVariableNbt nint;
    private DummyVariableNbt nlong;
    private DummyVariableNbt ndouble;
    private DummyVariableNbt nfloat;
    private DummyVariableNbt nstring;
    private DummyVariableNbt ntaglist;
    private DummyVariableNbt nbytelist;
    private DummyVariableNbt nintlist;
    private DummyVariableNbt nlonglist;

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
        slongarray = new DummyVariableString(ValueTypeString.ValueString.of("longarray"));
        sboolean = new DummyVariableString(ValueTypeString.ValueString.of("boolean"));
        scompound = new DummyVariableString(ValueTypeString.ValueString.of("compound"));
        slist = new DummyVariableString(ValueTypeString.ValueString.of("list"));

        i1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1));
        l1 = new DummyVariable<ValueTypeLong.ValueLong>(ValueTypes.LONG, ValueTypeLong.ValueLong.of(1));
        d1_5 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(1.5D));
        btrue = new DummyVariableBoolean(ValueTypeBoolean.ValueBoolean.of(true));

        nempty = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(new CompoundTag()));

        CompoundTag tsasa = new CompoundTag();
        tsasa.putString("a", "a");
        nsasa = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(tsasa));

        CompoundTag tsbsb = new CompoundTag();
        tsbsb.putString("b", "b");
        nsbsb = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(tsbsb));

        CompoundTag tsasasbsc = new CompoundTag();
        tsasasbsc.putString("a", "a");
        tsasasbsc.putString("b", "c");
        nsasasbsc = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(tsasasbsc));

        CompoundTag tall = new CompoundTag();
        tall.putByte("byte", (byte)1);
        tall.putShort("short", (short) 2);
        tall.putInt("integer", 3);
        tall.putLong("long", 4L);
        tall.putFloat("float", 5.5F);
        tall.putDouble("double", 6.5D);
        tall.putString("string", "seven");
        tall.putByteArray("bytearray", new byte[]{8,9,10});
        tall.putIntArray("intarray", new int[]{11, 12, 13});
        tall.putLongArray("longarray", new long[]{14, 15, 16});
        tall.putBoolean("boolean", true);
        CompoundTag subCompound = new CompoundTag();
        subCompound.putString("hello", "world");
        tall.put("compound", subCompound);
        ListTag subList = new ListTag();
        subList.add(subCompound);
        subList.add(subCompound);
        tall.put("list", subList);
        nall = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(tall));

        CompoundTag tsome = new CompoundTag();
        tsome.putByte("byte", (byte)1);
        tsome.putInt("integer", 3);
        tsome.putFloat("float", 5.5F);
        tsome.putString("string", "seven");
        tsome.putBoolean("boolean", true);
        CompoundTag subTagSome = new CompoundTag();
        subTagSome.putString("hello", "world");
        tsome.put("compound", subTagSome);
        ListTag subListSome = new ListTag();
        subListSome.add(subTagSome);
        tsome.put("list", subListSome);
        nsome = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(tsome));

        ltags = new DummyVariableList(ValueTypeList.ValueList.ofAll(
                ValueTypeNbt.ValueNbt.of(new CompoundTag()),
                ValueTypeNbt.ValueNbt.of(tsasa),
                ValueTypeNbt.ValueNbt.of(new CompoundTag())
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

        nboolean = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(ByteTag.ONE));
        nbyte = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(ByteTag.ONE));
        nshort = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(ShortTag.valueOf((short) 2)));
        nint = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(IntTag.valueOf(3)));
        nlong = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(LongTag.valueOf(4L)));
        ndouble = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(DoubleTag.valueOf(5.5)));
        nfloat = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(FloatTag.valueOf(6.5F)));
        nstring = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(StringTag.valueOf("7")));
        ntaglist = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(subList));
        nbytelist = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(new ByteArrayTag(new byte[]{0, 1, 2})));
        nintlist = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(new IntArrayTag(new int[]{0, 1, 2})));
        nlonglist = new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(new LongArrayTag(new long[]{0, 1, 2})));
    }

    /**
     * ----------------------------------- SIZE -----------------------------------
     */

    @Test
    public void testNbtSize() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_SIZE.evaluate(new IVariable[]{nempty});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("size({}) = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));

        IValue res2 = Operators.NBT_COMPOUND_SIZE.evaluate(new IVariable[]{nsasa});
        assertThat("size({a:a}) = 1", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(1));

        IValue res3 = Operators.NBT_COMPOUND_SIZE.evaluate(new IVariable[]{nsasasbsc});
        assertThat("size({a:a;b:b}) = 2", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtSizeSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_SIZE.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtSizeSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_SIZE.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtSize() throws EvaluationException {
        Operators.NBT_COMPOUND_SIZE.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- KEYS -----------------------------------
     */

    @Test
    public void testNbtKeys() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_KEYS.evaluate(new IVariable[]{nempty});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        assertThat("keys({}).size = 0", ((ValueTypeList.ValueList) res1).getRawValue().getLength(), is(0));

        IValue res2 = Operators.NBT_COMPOUND_KEYS.evaluate(new IVariable[]{nsasasbsc});
        assertThat("keys({a:a;b:b}).size = 2", ((ValueTypeList.ValueList) res2).getRawValue().getLength(), is(2));
        assertThat("keys({a:a;b:b})[0] = a", ((ValueTypeList.ValueList) res2).getRawValue().get(0), is(ValueTypeString.ValueString.of("a")));
        assertThat("keys({a:a;b:b})[1] = b", ((ValueTypeList.ValueList) res2).getRawValue().get(1), is(ValueTypeString.ValueString.of("b")));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtKeysSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_KEYS.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtKeysSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_KEYS.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtKeys() throws EvaluationException {
        Operators.NBT_COMPOUND_KEYS.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- HASKEY -----------------------------------
     */

    @Test
    public void testNbtHasKey() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_HASKEY.evaluate(new IVariable[]{nempty, sa});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("haskey({}, a) = false", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(false));

        IValue res2 = Operators.NBT_COMPOUND_HASKEY.evaluate(new IVariable[]{nsasasbsc, sa});
        assertThat("haskey({a:a}, a) = true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtHasKeySizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_HASKEY.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtHasKeySizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_HASKEY.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtHasKey() throws EvaluationException {
        Operators.NBT_COMPOUND_HASKEY.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_TYPE -----------------------------------
     */

    @Test
    public void testNbtValueType() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_VALUE_TYPE.evaluate(new IVariable[]{nempty, sa});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("valuetype({}, a) = null", ((ValueTypeString.ValueString) res1).getRawValue(), is("null"));

        assertThat("valuetype({...}, byte) = byte", ((ValueTypeString.ValueString)
                Operators.NBT_COMPOUND_VALUE_TYPE.evaluate(new IVariable[]{nall, sbyte})).getRawValue(), is("BYTE"));
        assertThat("valuetype({...}, short) = short", ((ValueTypeString.ValueString)
                Operators.NBT_COMPOUND_VALUE_TYPE.evaluate(new IVariable[]{nall, sshort})).getRawValue(), is("SHORT"));
        assertThat("valuetype({...}, integer) = integer", ((ValueTypeString.ValueString)
                Operators.NBT_COMPOUND_VALUE_TYPE.evaluate(new IVariable[]{nall, sinteger})).getRawValue(), is("INT"));
        assertThat("valuetype({...}, long) = long", ((ValueTypeString.ValueString)
                Operators.NBT_COMPOUND_VALUE_TYPE.evaluate(new IVariable[]{nall, slong})).getRawValue(), is("LONG"));
        assertThat("valuetype({...}, float) = float", ((ValueTypeString.ValueString)
                Operators.NBT_COMPOUND_VALUE_TYPE.evaluate(new IVariable[]{nall, sfloat})).getRawValue(), is("FLOAT"));
        assertThat("valuetype({...}, double) = double", ((ValueTypeString.ValueString)
                Operators.NBT_COMPOUND_VALUE_TYPE.evaluate(new IVariable[]{nall, sdouble})).getRawValue(), is("DOUBLE"));
        assertThat("valuetype({...}, string) = string", ((ValueTypeString.ValueString)
                Operators.NBT_COMPOUND_VALUE_TYPE.evaluate(new IVariable[]{nall, sstring})).getRawValue(), is("STRING"));
        assertThat("valuetype({...}, bytearray) = bytearray", ((ValueTypeString.ValueString)
                Operators.NBT_COMPOUND_VALUE_TYPE.evaluate(new IVariable[]{nall, sbytearray})).getRawValue(), is("BYTE[]"));
        assertThat("valuetype({...}, intarray) = intarray", ((ValueTypeString.ValueString)
                Operators.NBT_COMPOUND_VALUE_TYPE.evaluate(new IVariable[]{nall, sintarray})).getRawValue(), is("INT[]"));
        assertThat("valuetype({...}, boolean) = byte", ((ValueTypeString.ValueString)
                Operators.NBT_COMPOUND_VALUE_TYPE.evaluate(new IVariable[]{nall, sboolean})).getRawValue(), is("BYTE"));
        assertThat("valuetype({...}, tag) = tag", ((ValueTypeString.ValueString)
                Operators.NBT_COMPOUND_VALUE_TYPE.evaluate(new IVariable[]{nall, scompound})).getRawValue(), is("COMPOUND"));
        assertThat("valuetype({...}, list) = list", ((ValueTypeString.ValueString)
                Operators.NBT_COMPOUND_VALUE_TYPE.evaluate(new IVariable[]{nall, slist})).getRawValue(), is("LIST"));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueTypeSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_TYPE.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueTypeSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_TYPE.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueType() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_TYPE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_TAG -----------------------------------
     */

    @Test
    public void testNbtValueTag() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_VALUE_TAG.evaluate(new IVariable[]{nempty, sboolean});
        assertThat("result is a tag", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("valuetag({}, boolean) = false", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(Optional.empty()));

        IValue res2 = Operators.NBT_COMPOUND_VALUE_TAG.evaluate(new IVariable[]{nall, sboolean});
        assertThat("valuetag({...}, boolean) = true", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(ByteTag.ONE));

        IValue res3 = Operators.NBT_COMPOUND_VALUE_TAG.evaluate(new IVariable[]{nall, sinteger});
        assertThat("valuetag({...}, integer) = true", ((ValueTypeNbt.ValueNbt) res3).getRawValue().get(), is(IntTag.valueOf(3)));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueTagSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_TAG.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueTagSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_TAG.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueTag() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_TAG.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_BOOLEAN -----------------------------------
     */

    @Test
    public void testNbtValueBoolean() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_VALUE_BOOLEAN.evaluate(new IVariable[]{nempty, sboolean});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("valueboolean({}, boolean) = false", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(false));

        IValue res2 = Operators.NBT_COMPOUND_VALUE_BOOLEAN.evaluate(new IVariable[]{nall, sboolean});
        assertThat("valueboolean({...}, boolean) = true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));

        IValue res3 = Operators.NBT_COMPOUND_VALUE_BOOLEAN.evaluate(new IVariable[]{nall, sbyte});
        assertThat("valueboolean({...}, byte) = true", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(true));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueBooleanSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_BOOLEAN.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueBooleanSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_BOOLEAN.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueBoolean() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_BOOLEAN.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_INTEGER -----------------------------------
     */

    @Test
    public void testNbtValueInteger() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_VALUE_INTEGER.evaluate(new IVariable[]{nempty, sinteger});
        assertThat("result is a integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("valueinteger({}, integer) = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));

        IValue res2 = Operators.NBT_COMPOUND_VALUE_INTEGER.evaluate(new IVariable[]{nall, sinteger});
        assertThat("valueinteger({...}, integer) = 3", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(3));

        IValue res3 = Operators.NBT_COMPOUND_VALUE_INTEGER.evaluate(new IVariable[]{nall, sbyte});
        assertThat("valueinteger({...}, byte) = 3", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(1));

        IValue res4 = Operators.NBT_COMPOUND_VALUE_INTEGER.evaluate(new IVariable[]{nall, sshort});
        assertThat("valueinteger({...}, short) = 2", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueIntegerSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_INTEGER.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueIntegerSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_INTEGER.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueInteger() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_INTEGER.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_LONG -----------------------------------
     */

    @Test
    public void testNbtValueLong() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_VALUE_LONG.evaluate(new IVariable[]{nempty, slong});
        assertThat("result is a long", res1, instanceOf(ValueTypeLong.ValueLong.class));
        assertThat("valuelong({}, long) = 0", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(0L));

        IValue res2 = Operators.NBT_COMPOUND_VALUE_LONG.evaluate(new IVariable[]{nall, slong});
        assertThat("valuelong({...}, long) = 4", ((ValueTypeLong.ValueLong) res2).getRawValue(), is(4L));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueLongSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_LONG.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueLongSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_LONG.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueLong() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_LONG.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_DOUBLE -----------------------------------
     */

    @Test
    public void testNbtValueDouble() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_VALUE_DOUBLE.evaluate(new IVariable[]{nempty, sdouble});
        assertThat("result is a double", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("valuedouble({}, double) = 0", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(0D));

        IValue res2 = Operators.NBT_COMPOUND_VALUE_DOUBLE.evaluate(new IVariable[]{nall, sdouble});
        assertThat("valuedouble({...}, double) = 6.5", ((ValueTypeDouble.ValueDouble) res2).getRawValue(), is(6.5D));

        IValue res3 = Operators.NBT_COMPOUND_VALUE_DOUBLE.evaluate(new IVariable[]{nall, sfloat});
        assertThat("valuedouble({...}, float) = 5.5", ((ValueTypeDouble.ValueDouble) res3).getRawValue(), is(5.5D));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueDoubleSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_DOUBLE.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueDoubleSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_DOUBLE.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueDouble() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_DOUBLE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_STRING -----------------------------------
     */

    @Test
    public void testNbtValueString() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_VALUE_STRING.evaluate(new IVariable[]{nempty, sstring});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("valuestring({}, string) = ", ((ValueTypeString.ValueString) res1).getRawValue(), is(""));

        IValue res2 = Operators.NBT_COMPOUND_VALUE_STRING.evaluate(new IVariable[]{nall, sstring});
        assertThat("valuestring({...}, string) = seven", ((ValueTypeString.ValueString) res2).getRawValue(), is("seven"));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueStringSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_STRING.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueStringSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_STRING.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueString() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_STRING.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_COMPOUND -----------------------------------
     */

    @Test
    public void testNbtValueCompound() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_VALUE_COMPOUND.evaluate(new IVariable[]{nempty, scompound});
        assertThat("result is a tag", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("valuecompound({}, compound) = {}", ((ValueTypeNbt.ValueNbt) res1).getRawValue(), is(Optional.empty()));

        IValue res2 = Operators.NBT_COMPOUND_VALUE_COMPOUND.evaluate(new IVariable[]{nall, scompound});
        CompoundTag subTag = new CompoundTag();
        subTag.putString("hello", "world");
        assertThat("valuecompound({...}, compound) = {hello:world}", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(subTag));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueCompoundSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_COMPOUND.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueCompoundSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_COMPOUND.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueCompound() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_COMPOUND.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_LIST_TAG -----------------------------------
     */

    @Test
    public void testNbtValueListTag() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_VALUE_LIST_TAG.evaluate(new IVariable[]{nempty, slist});
        assertThat("result is a listtag", res1, instanceOf(ValueTypeList.ValueList.class));
        assertThat("valuelisttag({}, listtag).size = 0", ((ValueTypeList.ValueList) res1).getRawValue().getLength(), is(0));

        IValue res2 = Operators.NBT_COMPOUND_VALUE_LIST_TAG.evaluate(new IVariable[]{nall, slist});
        CompoundTag subTag = new CompoundTag();
        subTag.putString("hello", "world");
        ListTag subList = new ListTag();
        subList.add(subTag);
        assertThat("valuelisttag({...}, listtag).size = 2", ((ValueTypeList.ValueList) res2).getRawValue().getLength(), is(2));
        assertThat("valuelisttag({...}, listtag)[0] = ..list..", ((ValueTypeList.ValueList) res2).getRawValue().get(0), is(ValueTypeNbt.ValueNbt.of(subTag)));
        assertThat("valuelisttag({...}, listtag)[1] = ..list..", ((ValueTypeList.ValueList) res2).getRawValue().get(1), is(ValueTypeNbt.ValueNbt.of(subTag)));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueListTagSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_LIST_TAG.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueListTagSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_LIST_TAG.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueListTag() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_LIST_TAG.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_LIST_INT -----------------------------------
     */

    @Test
    public void testNbtValueListInt() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_VALUE_LIST_INT.evaluate(new IVariable[]{nempty, slist});
        assertThat("result is a listint", res1, instanceOf(ValueTypeList.ValueList.class));
        assertThat("valuelistint({}, listint).size = 0", ((ValueTypeList.ValueList) res1).getRawValue().getLength(), is(0));

        IValue res2 = Operators.NBT_COMPOUND_VALUE_LIST_INT.evaluate(new IVariable[]{nall, sintarray});
        assertThat("valuelistint({...}, listint).size = 3", ((ValueTypeList.ValueList) res2).getRawValue().getLength(), is(3));
        assertThat("valuelistint({...}, listint)[0] = 11", ((ValueTypeList.ValueList) res2).getRawValue().get(0), is(ValueTypeInteger.ValueInteger.of(11)));
        assertThat("valuelistint({...}, listint)[1] = 12", ((ValueTypeList.ValueList) res2).getRawValue().get(1), is(ValueTypeInteger.ValueInteger.of(12)));
        assertThat("valuelistint({...}, listint)[2] = 13", ((ValueTypeList.ValueList) res2).getRawValue().get(2), is(ValueTypeInteger.ValueInteger.of(13)));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueListIntSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_LIST_INT.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueListIntSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_LIST_INT.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueListInt() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_LIST_INT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_LIST_LONG -----------------------------------
     */

    @Test
    public void testNbtValueListLong() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_VALUE_LIST_LONG.evaluate(new IVariable[]{nempty, slist});
        assertThat("result is a listlong", res1, instanceOf(ValueTypeList.ValueList.class));
        assertThat("valuelistlong({}, listlong).size = 0", ((ValueTypeList.ValueList) res1).getRawValue().getLength(), is(0));

        IValue res2 = Operators.NBT_COMPOUND_VALUE_LIST_LONG.evaluate(new IVariable[]{nall, slongarray});
        assertThat("valuelistlong({...}, listlong).size = 3", ((ValueTypeList.ValueList) res2).getRawValue().getLength(), is(3));
        assertThat("valuelistlong({...}, listlong)[0] = 14", ((ValueTypeList.ValueList) res2).getRawValue().get(0), is(ValueTypeLong.ValueLong.of(14)));
        assertThat("valuelistlong({...}, listlong)[1] = 15", ((ValueTypeList.ValueList) res2).getRawValue().get(1), is(ValueTypeLong.ValueLong.of(15)));
        assertThat("valuelistlong({...}, listlong)[2] = 16", ((ValueTypeList.ValueList) res2).getRawValue().get(2), is(ValueTypeLong.ValueLong.of(16)));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueListLongSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_LIST_LONG.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueListLongSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_LIST_LONG.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueListLong() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_LIST_LONG.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- VALUE_LIST_BYTE -----------------------------------
     */

    @Test
    public void testNbtValueListByte() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_VALUE_LIST_BYTE.evaluate(new IVariable[]{nempty, slist});
        assertThat("result is a listbyte", res1, instanceOf(ValueTypeList.ValueList.class));
        assertThat("valuelistbyte({}, listbyte).size = 0", ((ValueTypeList.ValueList) res1).getRawValue().getLength(), is(0));

        IValue res2 = Operators.NBT_COMPOUND_VALUE_LIST_BYTE.evaluate(new IVariable[]{nall, sbytearray});
        assertThat("valuelistbyte({...}, listbyte).size = 3", ((ValueTypeList.ValueList) res2).getRawValue().getLength(), is(3));
        assertThat("valuelistbyte({...}, listbyte)[0] = 8", ((ValueTypeList.ValueList) res2).getRawValue().get(0), is(ValueTypeInteger.ValueInteger.of(8)));
        assertThat("valuelistbyte({...}, listbyte)[1] = 9", ((ValueTypeList.ValueList) res2).getRawValue().get(1), is(ValueTypeInteger.ValueInteger.of(9)));
        assertThat("valuelistbyte({...}, listbyte)[2] = 10", ((ValueTypeList.ValueList) res2).getRawValue().get(2), is(ValueTypeInteger.ValueInteger.of(10)));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueListByteSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_LIST_BYTE.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtValueListByteSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_LIST_BYTE.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtValueListByte() throws EvaluationException {
        Operators.NBT_COMPOUND_VALUE_LIST_BYTE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITHOUT -----------------------------------
     */

    @Test
    public void testNbtWithout() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_WITHOUT.evaluate(new IVariable[]{nsasa, sa});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("without({a:a}, a) = {}", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(new CompoundTag()));

        IValue res2 = Operators.NBT_COMPOUND_WITHOUT.evaluate(new IVariable[]{nsasasbsc, sa});
        CompoundTag tsbc = new CompoundTag();
        tsbc.putString("b", "c");
        assertThat("without({a:a;b:c}, a) = {b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(tsbc));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithoutSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_WITHOUT.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithoutSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_WITHOUT.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithout() throws EvaluationException {
        Operators.NBT_COMPOUND_WITHOUT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_BOOLEAN -----------------------------------
     */

    @Test
    public void testNbtWithBoolean() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_WITH_BOOLEAN.evaluate(new IVariable[]{nsasa, sc, btrue});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        CompoundTag t1 = new CompoundTag();
        t1.putString("a", "a");
        t1.putBoolean("c", true);
        assertThat("withboolean({a:a}, c, true) = {a:a,c:true}", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(t1));

        IValue res2 = Operators.NBT_COMPOUND_WITH_BOOLEAN.evaluate(new IVariable[]{nsasasbsc, sa, btrue});
        CompoundTag t2 = new CompoundTag();
        t2.putBoolean("a", true);
        t2.putString("b", "c");
        assertThat("withboolean({a:a;b:c}, a, true) = {a:true;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithBooleanSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_BOOLEAN.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithBooleanSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_BOOLEAN.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithBoolean() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_BOOLEAN.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_SHORT -----------------------------------
     */

    @Test
    public void testNbtWithShort() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_WITH_SHORT.evaluate(new IVariable[]{nsasa, sc, i1});
        assertThat("result is a short", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        CompoundTag t1 = new CompoundTag();
        t1.putString("a", "a");
        t1.putShort("c", (short) 1);
        assertThat("withshort({a:a}, c, 1) = {a:a,c:1}", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(t1));

        IValue res2 = Operators.NBT_COMPOUND_WITH_SHORT.evaluate(new IVariable[]{nsasasbsc, sa, i1});
        CompoundTag t2 = new CompoundTag();
        t2.putShort("a", (short) 1);
        t2.putString("b", "c");
        assertThat("withshort({a:a;b:c}, a, 1) = {a:1;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithShortSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_SHORT.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithShortSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_SHORT.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithShort() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_SHORT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_INTEGER -----------------------------------
     */

    @Test
    public void testNbtWithInteger() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_WITH_INTEGER.evaluate(new IVariable[]{nsasa, sc, i1});
        assertThat("result is a integer", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        CompoundTag t1 = new CompoundTag();
        t1.putString("a", "a");
        t1.putInt("c", 1);
        assertThat("withinteger({a:a}, c, 1) = {a:a,c:1}", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(t1));

        IValue res2 = Operators.NBT_COMPOUND_WITH_INTEGER.evaluate(new IVariable[]{nsasasbsc, sa, i1});
        CompoundTag t2 = new CompoundTag();
        t2.putInt("a", 1);
        t2.putString("b", "c");
        assertThat("withinteger({a:a;b:c}, a, 1) = {a:1;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithIntegerSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_INTEGER.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithIntegerSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_INTEGER.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithInteger() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_INTEGER.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_LONG -----------------------------------
     */

    @Test
    public void testNbtWithLong() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_WITH_LONG.evaluate(new IVariable[]{nsasa, sc, l1});
        assertThat("result is a long", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        CompoundTag t1 = new CompoundTag();
        t1.putString("a", "a");
        t1.putLong("c", 1L);
        assertThat("withlong({a:a}, c, 1) = {a:a,c:1}", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(t1));

        IValue res2 = Operators.NBT_COMPOUND_WITH_LONG.evaluate(new IVariable[]{nsasasbsc, sa, l1});
        CompoundTag t2 = new CompoundTag();
        t2.putLong("a", 1L);
        t2.putString("b", "c");
        assertThat("withlong({a:a;b:c}, a, 1) = {a:1;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithLongSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_LONG.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithLongSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_LONG.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithLong() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_LONG.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_DOUBLE -----------------------------------
     */

    @Test
    public void testNbtWithDouble() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_WITH_DOUBLE.evaluate(new IVariable[]{nsasa, sc, d1_5});
        assertThat("result is a double", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        CompoundTag t1 = new CompoundTag();
        t1.putString("a", "a");
        t1.putDouble("c", 1.5D);
        assertThat("withdouble({a:a}, c, 1.5) = {a:a,c:1.5}", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(t1));

        IValue res2 = Operators.NBT_COMPOUND_WITH_DOUBLE.evaluate(new IVariable[]{nsasasbsc, sa, d1_5});
        CompoundTag t2 = new CompoundTag();
        t2.putDouble("a", 1.5D);
        t2.putString("b", "c");
        assertThat("withdouble({a:a;b:c}, a, 1.5) = {a:1.5;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithDoubleSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_DOUBLE.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithDoubleSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_DOUBLE.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithDouble() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_DOUBLE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_FLOAT -----------------------------------
     */

    @Test
    public void testNbtWithFloat() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_WITH_FLOAT.evaluate(new IVariable[]{nsasa, sc, d1_5});
        assertThat("result is a float", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        CompoundTag t1 = new CompoundTag();
        t1.putString("a", "a");
        t1.putFloat("c", 1.5F);
        assertThat("withfloat({a:a}, c, 1.5) = {a:a,c:1.5}", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(t1));

        IValue res2 = Operators.NBT_COMPOUND_WITH_FLOAT.evaluate(new IVariable[]{nsasasbsc, sa, d1_5});
        CompoundTag t2 = new CompoundTag();
        t2.putFloat("a", 1.5F);
        t2.putString("b", "c");
        assertThat("withfloat({a:a;b:c}, a, 1.5) = {a:1.5;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithFloatSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_FLOAT.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithFloatSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_FLOAT.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithFloat() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_FLOAT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_STRING -----------------------------------
     */

    @Test
    public void testNbtWithString() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_WITH_STRING.evaluate(new IVariable[]{nsasa, sc, sc});
        assertThat("result is a string", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        CompoundTag t1 = new CompoundTag();
        t1.putString("a", "a");
        t1.putString("c", "c");
        assertThat("withstring({a:a}, c, c) = {a:a,c:c}", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(t1));

        IValue res2 = Operators.NBT_COMPOUND_WITH_STRING.evaluate(new IVariable[]{nsasasbsc, sa, sc});
        CompoundTag t2 = new CompoundTag();
        t2.putString("a", "c");
        t2.putString("b", "c");
        assertThat("withstring({a:a;b:c}, a, c) = {a:c;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithStringSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_STRING.evaluate(new IVariable[]{nempty, sa, i1});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithStringSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_STRING.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithString() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_STRING.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_TAG -----------------------------------
     */

    @Test
    public void testNbtWithTag() throws EvaluationException {
        CompoundTag tsasa = new CompoundTag();
        tsasa.putString("a", "a");

        IValue res1 = Operators.NBT_COMPOUND_WITH_COMPOUND.evaluate(new IVariable[]{nsasa, sc, nsasa});
        assertThat("result is a tag", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        CompoundTag t1 = new CompoundTag();
        t1.putString("a", "a");
        t1.put("c", tsasa);
        assertThat("withtag({a:a}, c, tag) = {a:a,c:tag}", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(t1));

        IValue res2 = Operators.NBT_COMPOUND_WITH_COMPOUND.evaluate(new IVariable[]{nsasasbsc, sa, nsasa});
        CompoundTag t2 = new CompoundTag();
        t2.put("a", tsasa);
        t2.putString("b", "c");
        assertThat("withtag({a:a;b:c}, a, tag) = {a:tag;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithTagSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_COMPOUND.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithTagSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_COMPOUND.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithTag() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_COMPOUND.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_LIST_TAG -----------------------------------
     */

    @Test
    public void testNbtWithListTag() throws EvaluationException {
        CompoundTag tsasa = new CompoundTag();
        tsasa.putString("a", "a");
        ListTag tlist = new ListTag();
        tlist.add(new CompoundTag());
        tlist.add(tsasa);
        tlist.add(new CompoundTag());

        IValue res1 = Operators.NBT_COMPOUND_WITH_LIST_TAG.evaluate(new IVariable[]{nsasa, sc, ltags});
        assertThat("result is a listtag", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        CompoundTag t1 = new CompoundTag();
        t1.putString("a", "a");
        t1.put("c", tlist);
        assertThat("withlisttag({a:a}, c, listtag) = {a:a,c:listtag}", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(t1));

        IValue res2 = Operators.NBT_COMPOUND_WITH_LIST_TAG.evaluate(new IVariable[]{nsasasbsc, sa, ltags});
        CompoundTag t2 = new CompoundTag();
        t2.put("a", tlist);
        t2.putString("b", "c");
        assertThat("withlisttag({a:a;b:c}, a, listtag) = {a:listtag;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithListTagSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_LIST_TAG.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithListTagSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_LIST_TAG.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithListTag() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_LIST_TAG.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_LIST_BYTE -----------------------------------
     */

    @Test
    public void testNbtWithListByte() throws EvaluationException {
        CompoundTag tsasa = new CompoundTag();
        tsasa.putString("a", "a");
        ByteArrayTag tlist = new ByteArrayTag(new byte[]{5, 4, 3});

        IValue res1 = Operators.NBT_COMPOUND_WITH_LIST_BYTE.evaluate(new IVariable[]{nsasa, sc, lints});
        assertThat("result is a listbyte", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        CompoundTag t1 = new CompoundTag();
        t1.putString("a", "a");
        t1.put("c", tlist);
        assertThat("withlistbyte({a:a}, c, listbyte) = {a:a,c:listbyte}", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(t1));

        IValue res2 = Operators.NBT_COMPOUND_WITH_LIST_BYTE.evaluate(new IVariable[]{nsasasbsc, sa, lints});
        CompoundTag t2 = new CompoundTag();
        t2.put("a", tlist);
        t2.putString("b", "c");
        assertThat("withlistbyte({a:a;b:c}, a, listbyte) = {a:listbyte;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithListByteSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_LIST_BYTE.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithListByteSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_LIST_BYTE.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithListByte() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_LIST_BYTE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_LIST_INT -----------------------------------
     */

    @Test
    public void testNbtWithListInt() throws EvaluationException {
        CompoundTag tsasa = new CompoundTag();
        tsasa.putString("a", "a");
        IntArrayTag tlist = new IntArrayTag(new int[]{5, 4, 3});

        IValue res1 = Operators.NBT_COMPOUND_WITH_LIST_INT.evaluate(new IVariable[]{nsasa, sc, lints});
        assertThat("result is a listint", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        CompoundTag t1 = new CompoundTag();
        t1.putString("a", "a");
        t1.put("c", tlist);
        assertThat("withlistint({a:a}, c, listint) = {a:a,c:listint}", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(t1));

        IValue res2 = Operators.NBT_COMPOUND_WITH_LIST_INT.evaluate(new IVariable[]{nsasasbsc, sa, lints});
        CompoundTag t2 = new CompoundTag();
        t2.put("a", tlist);
        t2.putString("b", "c");
        assertThat("withlistint({a:a;b:c}, a, listint) = {a:listint;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithListIntSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_LIST_INT.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithListIntSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_LIST_INT.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithListInt() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_LIST_INT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_LIST_LONG -----------------------------------
     */

    @Test
    public void testNbtWithListLong() throws EvaluationException {
        CompoundTag tsasa = new CompoundTag();
        tsasa.putString("a", "a");
        LongArrayTag tlist = new LongArrayTag(new long[]{5, 4, 3});

        IValue res1 = Operators.NBT_COMPOUND_WITH_LIST_LONG.evaluate(new IVariable[]{nsasa, sc, llongs});
        assertThat("result is a listlong", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        CompoundTag t1 = new CompoundTag();
        t1.putString("a", "a");
        t1.put("c", tlist);
        assertThat("withlistlong({a:a}, c, listlong) = {a:a,c:listlong}", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(t1));

        IValue res2 = Operators.NBT_COMPOUND_WITH_LIST_LONG.evaluate(new IVariable[]{nsasasbsc, sa, llongs});
        CompoundTag t2 = new CompoundTag();
        t2.put("a", tlist);
        t2.putString("b", "c");
        assertThat("withlistlong({a:a;b:c}, a, listlong) = {a:listlong;b:c}", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(t2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithListLongSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_LIST_LONG.evaluate(new IVariable[]{nempty, sa, sa});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtWithListLongSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_LIST_LONG.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtWithListLong() throws EvaluationException {
        Operators.NBT_COMPOUND_WITH_LIST_LONG.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- SUBSET -----------------------------------
     */

    @Test
    public void testNbtSubset() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_SUBSET.evaluate(new IVariable[]{nempty, nall});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("subset({}, all) = true", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));

        IValue res2 = Operators.NBT_COMPOUND_SUBSET.evaluate(new IVariable[]{nall, nempty});
        assertThat("subset(all, {}) = false", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(false));

        IValue res3 = Operators.NBT_COMPOUND_SUBSET.evaluate(new IVariable[]{nsome, nall});
        assertThat("subset(some, all) = true", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(true));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtSubsetSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_SUBSET.evaluate(new IVariable[]{nempty, nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtSubsetSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_SUBSET.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtSubset() throws EvaluationException {
        Operators.NBT_COMPOUND_SUBSET.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- UNION -----------------------------------
     */

    @Test
    public void testNbtUnion() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_UNION.evaluate(new IVariable[]{nempty, nall});
        assertThat("result is an nbt tag", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("union({}, all) = all", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(nall.getValue().getRawValue().get()));

        IValue res2 = Operators.NBT_COMPOUND_UNION.evaluate(new IVariable[]{nall, nempty});
        assertThat("union(all, {}) = all", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(nall.getValue().getRawValue().get()));

        IValue res3 = Operators.NBT_COMPOUND_UNION.evaluate(new IVariable[]{nsome, nall});
        assertThat("union(some, all) = all", ((ValueTypeNbt.ValueNbt) res3).getRawValue().get(), is(nall.getValue().getRawValue().get()));

        IValue res4 = Operators.NBT_COMPOUND_UNION.evaluate(new IVariable[]{nsasa, nsbsb});
        CompoundTag tsasasbsb = new CompoundTag();
        tsasasbsb.putString("a", "a");
        tsasasbsb.putString("b", "b");
        assertThat("union(sasa, sbsb) = sasasbsb", ((ValueTypeNbt.ValueNbt) res4).getRawValue().get(), is(tsasasbsb));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtUnionSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_UNION.evaluate(new IVariable[]{nempty, nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtUnionSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_UNION.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtUnion() throws EvaluationException {
        Operators.NBT_COMPOUND_UNION.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- INTERSECTION -----------------------------------
     */

    @Test
    public void testNbtIntersection() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_INTERSECTION.evaluate(new IVariable[]{nempty, nall});
        assertThat("result is an nbt tag", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("intersection({}, all) = {}", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(new CompoundTag()));

        IValue res2 = Operators.NBT_COMPOUND_INTERSECTION.evaluate(new IVariable[]{nall, nempty});
        assertThat("intersection(all, {}) = {}", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(new CompoundTag()));

        IValue res3 = Operators.NBT_COMPOUND_INTERSECTION.evaluate(new IVariable[]{nsome, nall});
        assertThat("intersection(some, all) = some", ((ValueTypeNbt.ValueNbt) res3).getRawValue().get(), is(nsome.getValue().getRawValue().get()));

        IValue res4 = Operators.NBT_COMPOUND_INTERSECTION.evaluate(new IVariable[]{nsasa, nsbsb});
        assertThat("intersection(sasa, sbsb) = {}", ((ValueTypeNbt.ValueNbt) res4).getRawValue().get(), is(new CompoundTag()));

        IValue res5 = Operators.NBT_COMPOUND_INTERSECTION.evaluate(new IVariable[]{nsasa, nsasasbsc});
        assertThat("intersection(sasa, sasasbsc) = {}", ((ValueTypeNbt.ValueNbt) res5).getRawValue().get(), is(nsasa.getValue().getRawValue().get()));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtIntersectionSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_INTERSECTION.evaluate(new IVariable[]{nempty, nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtIntersectionSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_INTERSECTION.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtIntersection() throws EvaluationException {
        Operators.NBT_COMPOUND_INTERSECTION.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- MINUS -----------------------------------
     */

    @Test
    public void testNbtMinus() throws EvaluationException {
        IValue res1 = Operators.NBT_COMPOUND_MINUS.evaluate(new IVariable[]{nempty, nall});
        assertThat("result is an nbt tag", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("minus({}, all) = {}", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(new CompoundTag()));

        IValue res2 = Operators.NBT_COMPOUND_MINUS.evaluate(new IVariable[]{nall, nempty});
        assertThat("minus(all, {}) = all", ((ValueTypeNbt.ValueNbt) res2).getRawValue().get(), is(nall.getValue().getRawValue().get()));

        IValue res3 = Operators.NBT_COMPOUND_MINUS.evaluate(new IVariable[]{nsome, nall});
        assertThat("minus(some, all) = {}", ((ValueTypeNbt.ValueNbt) res3).getRawValue().get(), is(new CompoundTag()));

        IValue res4 = Operators.NBT_COMPOUND_MINUS.evaluate(new IVariable[]{nsasa, nsbsb});
        assertThat("minus(sasa, sbsb) = sasa", ((ValueTypeNbt.ValueNbt) res4).getRawValue().get(), is(nsasa.getValue().getRawValue().get()));

        IValue res5 = Operators.NBT_COMPOUND_MINUS.evaluate(new IVariable[]{nsasasbsc, nsasa});
        CompoundTag tsbsc = new CompoundTag();
        tsbsc.putString("b", "c");
        assertThat("minus(sasasbsc, sasa) = {}", ((ValueTypeNbt.ValueNbt) res5).getRawValue().get(), is(tsbsc));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtMinusSizeLarge() throws EvaluationException {
        Operators.NBT_COMPOUND_MINUS.evaluate(new IVariable[]{nempty, nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtMinusSizeSmall() throws EvaluationException {
        Operators.NBT_COMPOUND_MINUS.evaluate(new IVariable[]{nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtMinus() throws EvaluationException {
        Operators.NBT_COMPOUND_MINUS.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- AS_BOOLEAN -----------------------------------
     */

    @Test
    public void testNbtAsBoolean() throws EvaluationException {
        IValue res1 = Operators.NBT_AS_BOOLEAN.evaluate(new IVariable[]{nboolean});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("as_boolean(true) = true", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));

        IValue res2 = Operators.NBT_AS_BOOLEAN.evaluate(new IVariable[]{nempty});
        assertThat("as_boolean(empty) = false", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsBooleanSizeLarge() throws EvaluationException {
        Operators.NBT_AS_BOOLEAN.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsBooleanSizeSmall() throws EvaluationException {
        Operators.NBT_AS_BOOLEAN.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtAsBoolean() throws EvaluationException {
        Operators.NBT_AS_BOOLEAN.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- AS_BYTE -----------------------------------
     */

    @Test
    public void testNbtAsByte() throws EvaluationException {
        IValue res1 = Operators.NBT_AS_BYTE.evaluate(new IVariable[]{nbyte});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("as_byte(1) = 1", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(1));

        IValue res2 = Operators.NBT_AS_BYTE.evaluate(new IVariable[]{nempty});
        assertThat("as_byte(empty) = 0", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(0));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsByteSizeLarge() throws EvaluationException {
        Operators.NBT_AS_BYTE.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsByteSizeSmall() throws EvaluationException {
        Operators.NBT_AS_BYTE.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtAsByte() throws EvaluationException {
        Operators.NBT_AS_BYTE.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- AS_SHORT -----------------------------------
     */

    @Test
    public void testNbtAsShort() throws EvaluationException {
        IValue res1 = Operators.NBT_AS_SHORT.evaluate(new IVariable[]{nshort});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("as_short(2) = 2", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(2));

        IValue res2 = Operators.NBT_AS_SHORT.evaluate(new IVariable[]{nempty});
        assertThat("as_short(empty) = 0", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(0));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsShortSizeLarge() throws EvaluationException {
        Operators.NBT_AS_SHORT.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsShortSizeSmall() throws EvaluationException {
        Operators.NBT_AS_SHORT.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtAsShort() throws EvaluationException {
        Operators.NBT_AS_SHORT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- AS_INT -----------------------------------
     */

    @Test
    public void testNbtAsInt() throws EvaluationException {
        IValue res1 = Operators.NBT_AS_INT.evaluate(new IVariable[]{nint});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("as_int(3) = 3", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(3));

        IValue res2 = Operators.NBT_AS_INT.evaluate(new IVariable[]{nempty});
        assertThat("as_int(empty) = 0", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(0));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsIntSizeLarge() throws EvaluationException {
        Operators.NBT_AS_INT.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsIntSizeSmall() throws EvaluationException {
        Operators.NBT_AS_INT.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtAsInt() throws EvaluationException {
        Operators.NBT_AS_INT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- AS_LONG -----------------------------------
     */

    @Test
    public void testNbtAsLong() throws EvaluationException {
        IValue res1 = Operators.NBT_AS_LONG.evaluate(new IVariable[]{nlong});
        assertThat("result is a long", res1, instanceOf(ValueTypeLong.ValueLong.class));
        assertThat("as_long(4) = 4", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(4L));

        IValue res2 = Operators.NBT_AS_LONG.evaluate(new IVariable[]{nempty});
        assertThat("as_long(empty) = 0", ((ValueTypeLong.ValueLong) res2).getRawValue(), is(0L));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsLongSizeLarge() throws EvaluationException {
        Operators.NBT_AS_LONG.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsLongSizeSmall() throws EvaluationException {
        Operators.NBT_AS_LONG.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtAsLong() throws EvaluationException {
        Operators.NBT_AS_LONG.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- AS_DOUBLE -----------------------------------
     */

    @Test
    public void testNbtAsDouble() throws EvaluationException {
        IValue res1 = Operators.NBT_AS_DOUBLE.evaluate(new IVariable[]{ndouble});
        assertThat("result is a double", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("as_double(5.5) = 5.5", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(5.5D));

        IValue res2 = Operators.NBT_AS_DOUBLE.evaluate(new IVariable[]{nempty});
        assertThat("as_double(empty) = 0", ((ValueTypeDouble.ValueDouble) res2).getRawValue(), is(0D));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsDoubleSizeLarge() throws EvaluationException {
        Operators.NBT_AS_DOUBLE.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsDoubleSizeSmall() throws EvaluationException {
        Operators.NBT_AS_DOUBLE.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtAsDouble() throws EvaluationException {
        Operators.NBT_AS_DOUBLE.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- AS_FLOAT -----------------------------------
     */

    @Test
    public void testNbtAsFloat() throws EvaluationException {
        IValue res1 = Operators.NBT_AS_FLOAT.evaluate(new IVariable[]{nfloat});
        assertThat("result is a double", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("as_float(6.5) = 6.5", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(6.5D));

        IValue res2 = Operators.NBT_AS_FLOAT.evaluate(new IVariable[]{nempty});
        assertThat("as_float(empty) = 0", ((ValueTypeDouble.ValueDouble) res2).getRawValue(), is(0D));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsFloatSizeLarge() throws EvaluationException {
        Operators.NBT_AS_FLOAT.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsFloatSizeSmall() throws EvaluationException {
        Operators.NBT_AS_FLOAT.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtAsFloat() throws EvaluationException {
        Operators.NBT_AS_FLOAT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- AS_STRING -----------------------------------
     */

    @Test
    public void testNbtAsString() throws EvaluationException {
        IValue res1 = Operators.NBT_AS_STRING.evaluate(new IVariable[]{nstring});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("as_string(7) = 7", ((ValueTypeString.ValueString) res1).getRawValue(), is("7"));

        IValue res2 = Operators.NBT_AS_STRING.evaluate(new IVariable[]{nempty});
        assertThat("as_string(empty) = ", ((ValueTypeString.ValueString) res2).getRawValue(), is(""));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsStringSizeLarge() throws EvaluationException {
        Operators.NBT_AS_STRING.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsStringSizeSmall() throws EvaluationException {
        Operators.NBT_AS_STRING.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtAsString() throws EvaluationException {
        Operators.NBT_AS_STRING.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- AS_TAG_LIST -----------------------------------
     */

    @Test
    public void testNbtAsTagList() throws EvaluationException {
        CompoundTag subCompound = new CompoundTag();
        subCompound.putString("hello", "world");

        IValue res1 = Operators.NBT_AS_TAG_LIST.evaluate(new IVariable[]{ntaglist});
        assertThat("result is a taglist", res1, instanceOf(ValueTypeList.ValueList.class));
        assertThat("as_taglist(...).type = nbt", ((ValueTypeList.ValueList) res1).getRawValue().getValueType(), is(ValueTypes.NBT));
        assertThat("as_taglist(...).length = 2", ((ValueTypeList.ValueList) res1).getRawValue().getLength(), is(2));
        assertThat("as_taglist(...)[0] = tag", ((ValueTypeList.ValueList) res1).getRawValue().get(0), is(ValueTypeNbt.ValueNbt.of(subCompound)));
        assertThat("as_taglist(...)[1] = tag", ((ValueTypeList.ValueList) res1).getRawValue().get(1), is(ValueTypeNbt.ValueNbt.of(subCompound)));

        IValue res2 = Operators.NBT_AS_TAG_LIST.evaluate(new IVariable[]{nempty});
        assertThat("as_taglist(...).length = 0", ((ValueTypeList.ValueList) res2).getRawValue().getLength(), is(0));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsTagListSizeLarge() throws EvaluationException {
        Operators.NBT_AS_TAG_LIST.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsTagListSizeSmall() throws EvaluationException {
        Operators.NBT_AS_TAG_LIST.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtAsTagList() throws EvaluationException {
        Operators.NBT_AS_TAG_LIST.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- AS_BYTE_LIST -----------------------------------
     */

    @Test
    public void testNbtAsByteList() throws EvaluationException {
        IValue res1 = Operators.NBT_AS_BYTE_LIST.evaluate(new IVariable[]{nbytelist});
        assertThat("result is a bytelist", res1, instanceOf(ValueTypeList.ValueList.class));
        assertThat("as_ bytelist(...).type = integer", ((ValueTypeList.ValueList) res1).getRawValue().getValueType(), is(ValueTypes.INTEGER));
        assertThat("as_ bytelist(...).length = 3", ((ValueTypeList.ValueList) res1).getRawValue().getLength(), is(3));
        assertThat("as_ bytelist(...)[0] = byte", ((ValueTypeList.ValueList) res1).getRawValue().get(0), is(ValueTypeInteger.ValueInteger.of(0)));
        assertThat("as_ bytelist(...)[1] = byte", ((ValueTypeList.ValueList) res1).getRawValue().get(1), is(ValueTypeInteger.ValueInteger.of(1)));
        assertThat("as_ bytelist(...)[2] = byte", ((ValueTypeList.ValueList) res1).getRawValue().get(2), is(ValueTypeInteger.ValueInteger.of(2)));

        IValue res2 = Operators.NBT_AS_BYTE_LIST.evaluate(new IVariable[]{nempty});
        assertThat("as_ bytelist(...).length = 0", ((ValueTypeList.ValueList) res2).getRawValue().getLength(), is(0));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsByteListSizeLarge() throws EvaluationException {
        Operators.NBT_AS_BYTE_LIST.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsByteListSizeSmall() throws EvaluationException {
        Operators.NBT_AS_BYTE_LIST.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtAsByteList() throws EvaluationException {
        Operators.NBT_AS_BYTE_LIST.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- AS_INT_LIST -----------------------------------
     */

    @Test
    public void testNbtAsIntList() throws EvaluationException {
        IValue res1 = Operators.NBT_AS_INT_LIST.evaluate(new IVariable[]{nintlist});
        assertThat("result is a intlist", res1, instanceOf(ValueTypeList.ValueList.class));
        assertThat("as_ intlist(...).type = integer", ((ValueTypeList.ValueList) res1).getRawValue().getValueType(), is(ValueTypes.INTEGER));
        assertThat("as_ intlist(...).length = 3", ((ValueTypeList.ValueList) res1).getRawValue().getLength(), is(3));
        assertThat("as_ intlist(...)[0] = int", ((ValueTypeList.ValueList) res1).getRawValue().get(0), is(ValueTypeInteger.ValueInteger.of(0)));
        assertThat("as_ intlist(...)[1] = int", ((ValueTypeList.ValueList) res1).getRawValue().get(1), is(ValueTypeInteger.ValueInteger.of(1)));
        assertThat("as_ intlist(...)[2] = int", ((ValueTypeList.ValueList) res1).getRawValue().get(2), is(ValueTypeInteger.ValueInteger.of(2)));

        IValue res2 = Operators.NBT_AS_INT_LIST.evaluate(new IVariable[]{nempty});
        assertThat("as_ intlist(...).length = 0", ((ValueTypeList.ValueList) res2).getRawValue().getLength(), is(0));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsIntListSizeLarge() throws EvaluationException {
        Operators.NBT_AS_INT_LIST.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsIntListSizeSmall() throws EvaluationException {
        Operators.NBT_AS_INT_LIST.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtAsIntList() throws EvaluationException {
        Operators.NBT_AS_INT_LIST.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- AS_LONG_LIST -----------------------------------
     */

    @Test
    public void testNbtAsLongList() throws EvaluationException {
        IValue res1 = Operators.NBT_AS_LONG_LIST.evaluate(new IVariable[]{nlonglist});
        assertThat("result is a longlist", res1, instanceOf(ValueTypeList.ValueList.class));
        assertThat("as_ longlist(...).type = long", ((ValueTypeList.ValueList) res1).getRawValue().getValueType(), is(ValueTypes.LONG));
        assertThat("as_ longlist(...).length = 3", ((ValueTypeList.ValueList) res1).getRawValue().getLength(), is(3));
        assertThat("as_ longlist(...)[0] = long", ((ValueTypeList.ValueList) res1).getRawValue().get(0), is(ValueTypeLong.ValueLong.of(0)));
        assertThat("as_ longlist(...)[1] = long", ((ValueTypeList.ValueList) res1).getRawValue().get(1), is(ValueTypeLong.ValueLong.of(1)));
        assertThat("as_ longlist(...)[2] = long", ((ValueTypeList.ValueList) res1).getRawValue().get(2), is(ValueTypeLong.ValueLong.of(2)));

        IValue res2 = Operators.NBT_AS_LONG_LIST.evaluate(new IVariable[]{nempty});
        assertThat("as_ longlist(...).length = 0", ((ValueTypeList.ValueList) res2).getRawValue().getLength(), is(0));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsLongListSizeLarge() throws EvaluationException {
        Operators.NBT_AS_LONG_LIST.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtAsLongListSizeSmall() throws EvaluationException {
        Operators.NBT_AS_LONG_LIST.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtAsLongList() throws EvaluationException {
        Operators.NBT_AS_LONG_LIST.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FROM_BOOLEAN -----------------------------------
     */

    @Test
    public void testNbtFromBoolean() throws EvaluationException {
        IValue res1 = Operators.NBT_FROM_BOOLEAN.evaluate(new IVariable[]{new DummyVariableBoolean(ValueTypeBoolean.ValueBoolean.of(true))});
        assertThat("result is nbt", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("from_boolean(true) = nbt(true)", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(nboolean.getValue().getRawValue().get()));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromBooleanSizeLarge() throws EvaluationException {
        Operators.NBT_FROM_BOOLEAN.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromBooleanSizeSmall() throws EvaluationException {
        Operators.NBT_FROM_BOOLEAN.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtFromBoolean() throws EvaluationException {
        Operators.NBT_FROM_BOOLEAN.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FROM_BYTE -----------------------------------
     */

    @Test
    public void testNbtFromByte() throws EvaluationException {
        IValue res1 = Operators.NBT_FROM_BYTE.evaluate(new IVariable[]{new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1))});
        assertThat("result is nbt", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("from_byte(1) = nbt(1)", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(nbyte.getValue().getRawValue().get()));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromByteSizeLarge() throws EvaluationException {
        Operators.NBT_FROM_BYTE.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromByteSizeSmall() throws EvaluationException {
        Operators.NBT_FROM_BYTE.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtFromByte() throws EvaluationException {
        Operators.NBT_FROM_BYTE.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FROM_SHORT -----------------------------------
     */

    @Test
    public void testNbtFromShort() throws EvaluationException {
        IValue res1 = Operators.NBT_FROM_SHORT.evaluate(new IVariable[]{new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(2))});
        assertThat("result is nbt", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("from_short(2) = nbt(2)", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(nshort.getValue().getRawValue().get()));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromShortSizeLarge() throws EvaluationException {
        Operators.NBT_FROM_SHORT.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromShortSizeSmall() throws EvaluationException {
        Operators.NBT_FROM_SHORT.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtFromShort() throws EvaluationException {
        Operators.NBT_FROM_SHORT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FROM_INT -----------------------------------
     */

    @Test
    public void testNbtFromInt() throws EvaluationException {
        IValue res1 = Operators.NBT_FROM_INT.evaluate(new IVariable[]{new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(3))});
        assertThat("result is nbt", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("from_int(3) = nbt(3)", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(nint.getValue().getRawValue().get()));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromIntSizeLarge() throws EvaluationException {
        Operators.NBT_FROM_INT.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromIntSizeSmall() throws EvaluationException {
        Operators.NBT_FROM_INT.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtFromInt() throws EvaluationException {
        Operators.NBT_FROM_INT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FROM_LONG -----------------------------------
     */

    @Test
    public void testNbtFromLong() throws EvaluationException {
        IValue res1 = Operators.NBT_FROM_LONG.evaluate(new IVariable[]{new DummyVariable(ValueTypes.LONG, ValueTypeLong.ValueLong.of(4))});
        assertThat("result is nbt", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("from_long(4) = nbt(4)", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(nlong.getValue().getRawValue().get()));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromLongSizeLarge() throws EvaluationException {
        Operators.NBT_FROM_LONG.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromLongSizeSmall() throws EvaluationException {
        Operators.NBT_FROM_LONG.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtFromLong() throws EvaluationException {
        Operators.NBT_FROM_LONG.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FROM_DOUBLE -----------------------------------
     */

    @Test
    public void testNbtFromDouble() throws EvaluationException {
        IValue res1 = Operators.NBT_FROM_DOUBLE.evaluate(new IVariable[]{new DummyVariable(ValueTypes.DOUBLE, ValueTypeDouble.ValueDouble.of(5.5D))});
        assertThat("result is nbt", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("from_double(5.5) = nbt(5.5)", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(ndouble.getValue().getRawValue().get()));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromDoubleSizeLarge() throws EvaluationException {
        Operators.NBT_FROM_DOUBLE.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromDoubleSizeSmall() throws EvaluationException {
        Operators.NBT_FROM_DOUBLE.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtFromDouble() throws EvaluationException {
        Operators.NBT_FROM_DOUBLE.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FROM_FLOAT -----------------------------------
     */

    @Test
    public void testNbtFromFloat() throws EvaluationException {
        IValue res1 = Operators.NBT_FROM_FLOAT.evaluate(new IVariable[]{new DummyVariable(ValueTypes.DOUBLE, ValueTypeDouble.ValueDouble.of(6.5D))});
        assertThat("result is nbt", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("from_float(6.5) = nbt(6.5)", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(nfloat.getValue().getRawValue().get()));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromFloatSizeLarge() throws EvaluationException {
        Operators.NBT_FROM_FLOAT.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromFloatSizeSmall() throws EvaluationException {
        Operators.NBT_FROM_FLOAT.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtFromFloat() throws EvaluationException {
        Operators.NBT_FROM_FLOAT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FROM_STRING -----------------------------------
     */

    @Test
    public void testNbtFromString() throws EvaluationException {
        IValue res1 = Operators.NBT_FROM_STRING.evaluate(new IVariable[]{new DummyVariable(ValueTypes.STRING, ValueTypeString.ValueString.of("7"))});
        assertThat("result is nbt", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("from_string(7) = nbt(7)", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(nstring.getValue().getRawValue().get()));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromStringSizeLarge() throws EvaluationException {
        Operators.NBT_FROM_STRING.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromStringSizeSmall() throws EvaluationException {
        Operators.NBT_FROM_STRING.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtFromString() throws EvaluationException {
        Operators.NBT_FROM_STRING.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FROM_TAG_LIST -----------------------------------
     */

    @Test
    public void testNbtFromTagList() throws EvaluationException {
        CompoundTag subCompound = new CompoundTag();
        subCompound.putString("hello", "world");
        IValue res1 = Operators.NBT_FROM_TAG_LIST.evaluate(new IVariable[]{new DummyVariableList(ValueTypeList.ValueList.ofAll(
                ValueTypeNbt.ValueNbt.of(subCompound),
                ValueTypeNbt.ValueNbt.of(subCompound)
        ))});
        assertThat("result is nbt", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("from_taglist(7) = nbt(7)", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(ntaglist.getValue().getRawValue().get()));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromTagListSizeLarge() throws EvaluationException {
        Operators.NBT_FROM_TAG_LIST.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromTagListSizeSmall() throws EvaluationException {
        Operators.NBT_FROM_TAG_LIST.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtFromTagList() throws EvaluationException {
        Operators.NBT_FROM_TAG_LIST.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FROM_BYTE_LIST -----------------------------------
     */

    @Test
    public void testNbtFromByteList() throws EvaluationException {
        CompoundTag subCompound = new CompoundTag();
        subCompound.putString("hello", "world");
        IValue res1 = Operators.NBT_FROM_BYTE_LIST.evaluate(new IVariable[]{new DummyVariableList(ValueTypeList.ValueList.ofAll(
                ValueTypeInteger.ValueInteger.of(0),
                ValueTypeInteger.ValueInteger.of(1),
                ValueTypeInteger.ValueInteger.of(2)
        ))});
        assertThat("result is nbt", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("from_bytelist(7) = nbt(7)", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(nbytelist.getValue().getRawValue().get()));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromByteListSizeLarge() throws EvaluationException {
        Operators.NBT_FROM_BYTE_LIST.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromByteListSizeSmall() throws EvaluationException {
        Operators.NBT_FROM_BYTE_LIST.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtFromByteList() throws EvaluationException {
        Operators.NBT_FROM_BYTE_LIST.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FROM_INT_LIST -----------------------------------
     */

    @Test
    public void testNbtFromIntList() throws EvaluationException {
        CompoundTag subCompound = new CompoundTag();
        subCompound.putString("hello", "world");
        IValue res1 = Operators.NBT_FROM_INT_LIST.evaluate(new IVariable[]{new DummyVariableList(ValueTypeList.ValueList.ofAll(
                ValueTypeInteger.ValueInteger.of(0),
                ValueTypeInteger.ValueInteger.of(1),
                ValueTypeInteger.ValueInteger.of(2)
        ))});
        assertThat("result is nbt", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("from_intlist(7) = nbt(7)", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(nintlist.getValue().getRawValue().get()));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromIntListSizeLarge() throws EvaluationException {
        Operators.NBT_FROM_INT_LIST.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromIntListSizeSmall() throws EvaluationException {
        Operators.NBT_FROM_INT_LIST.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtFromIntList() throws EvaluationException {
        Operators.NBT_FROM_INT_LIST.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FROM_LONG_LIST -----------------------------------
     */

    @Test
    public void testNbtFromLongList() throws EvaluationException {
        CompoundTag subCompound = new CompoundTag();
        subCompound.putString("hello", "world");
        IValue res1 = Operators.NBT_FROM_LONG_LIST.evaluate(new IVariable[]{new DummyVariableList(ValueTypeList.ValueList.ofAll(
                ValueTypeLong.ValueLong.of(0),
                ValueTypeLong.ValueLong.of(1),
                ValueTypeLong.ValueLong.of(2)
        ))});
        assertThat("result is nbt", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("from_longlist(7) = nbt(7)", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(nlonglist.getValue().getRawValue().get()));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromLongListSizeLarge() throws EvaluationException {
        Operators.NBT_FROM_LONG_LIST.evaluate(new IVariable[]{nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtFromLongListSizeSmall() throws EvaluationException {
        Operators.NBT_FROM_LONG_LIST.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtFromLongList() throws EvaluationException {
        Operators.NBT_FROM_LONG_LIST.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- PATH_MATCH_FIRST -----------------------------------
     */

    @Test
    public void testNbtPathMatchFirst() throws EvaluationException {
        CompoundTag tag2 = new CompoundTag();
        CompoundTag tag3 = new CompoundTag();
        StringTag tag4 = StringTag.valueOf("x");
        tag2.put("a", tag3);
        tag3.put("b", tag4);

        IValue res1 = Operators.NBT_PATH_MATCH_FIRST.evaluate(new IVariable[]{
                new DummyVariable(ValueTypes.STRING, ValueTypeString.ValueString.of("$.a.b")),
                new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(tag2))
        });
        assertThat("result is nbt", res1, instanceOf(ValueTypeNbt.ValueNbt.class));
        assertThat("path_match_first(...) = x", ((ValueTypeNbt.ValueNbt) res1).getRawValue().get(), is(tag4));

        IValue res2 = Operators.NBT_PATH_MATCH_FIRST.evaluate(new IVariable[]{
                new DummyVariable(ValueTypes.STRING, ValueTypeString.ValueString.of("$.a.b")),
                nempty
        });
        assertThat("path_match_first(empty) = empty", ((ValueTypeNbt.ValueNbt) res2).getRawValue().isPresent(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtPathMatchFirstInvalidPathExpression() throws EvaluationException {
        Operators.NBT_PATH_MATCH_FIRST.evaluate(new IVariable[]{nstring, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtPathMatchFirstSizeLarge() throws EvaluationException {
        Operators.NBT_PATH_MATCH_FIRST.evaluate(new IVariable[]{nstring, nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtPathMatchFirstSizeSmall() throws EvaluationException {
        Operators.NBT_PATH_MATCH_FIRST.evaluate(new IVariable[]{nstring});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtPathMatchFirst() throws EvaluationException {
        Operators.NBT_PATH_MATCH_FIRST.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- PATH_MATCH_ALL -----------------------------------
     */

    @Test
    public void testNbtPathMatchAll() throws EvaluationException {
        CompoundTag tag2 = new CompoundTag();
        CompoundTag tag3 = new CompoundTag();
        StringTag tag4 = StringTag.valueOf("x");
        StringTag tag5 = StringTag.valueOf("y");
        tag2.put("a", tag3);
        tag3.put("b", tag4);
        tag3.put("c", tag5);

        IValue res1 = Operators.NBT_PATH_MATCH_ALL.evaluate(new IVariable[]{
                new DummyVariable(ValueTypes.STRING, ValueTypeString.ValueString.of("$.a*")),
                new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(tag2))
        });
        assertThat("result is list", res1, instanceOf(ValueTypeList.ValueList.class));
        assertThat("path_match_all(...).type = NBT", ((ValueTypeList.ValueList) res1).getRawValue().getValueType(), is(ValueTypes.NBT));
        assertThat("path_match_all(...).length = 2", ((ValueTypeList.ValueList) res1).getRawValue().getLength(), is(2));
        assertThat("path_match_all(...)[0] = x", ((ValueTypeList.ValueList) res1).getRawValue().get(0), is(ValueTypeNbt.ValueNbt.of(tag4)));
        assertThat("path_match_all(...)[1] = y", ((ValueTypeList.ValueList) res1).getRawValue().get(1), is(ValueTypeNbt.ValueNbt.of(tag5)));

        IValue res2 = Operators.NBT_PATH_MATCH_ALL.evaluate(new IVariable[]{
                new DummyVariable(ValueTypes.STRING, ValueTypeString.ValueString.of("$.a*")),
                nempty
        });
        assertThat("path_match_all(empty).length = 0", ((ValueTypeList.ValueList) res2).getRawValue().getLength(), is(0));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtPathMatchAllInvalidPathExpression() throws EvaluationException {
        Operators.NBT_PATH_MATCH_ALL.evaluate(new IVariable[]{nstring, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtPathMatchAllSizeLarge() throws EvaluationException {
        Operators.NBT_PATH_MATCH_ALL.evaluate(new IVariable[]{nstring, nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtPathMatchAllSizeSmall() throws EvaluationException {
        Operators.NBT_PATH_MATCH_ALL.evaluate(new IVariable[]{nstring});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtPathMatchAll() throws EvaluationException {
        Operators.NBT_PATH_MATCH_ALL.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- PATH_TEST -----------------------------------
     */

    @Test
    public void testNbtPathTest() throws EvaluationException {
        CompoundTag tag2 = new CompoundTag();
        CompoundTag tag3 = new CompoundTag();
        StringTag tag4 = StringTag.valueOf("x");
        tag2.put("a", tag3);
        tag3.put("b", tag4);

        IValue res1 = Operators.NBT_PATH_TEST.evaluate(new IVariable[]{
                new DummyVariable(ValueTypes.STRING, ValueTypeString.ValueString.of("$.a.b")),
                new DummyVariableNbt(ValueTypeNbt.ValueNbt.of(tag2))
        });
        assertThat("result is boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("path_test(...) = true", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));

        IValue res2 = Operators.NBT_PATH_TEST.evaluate(new IVariable[]{
                new DummyVariable(ValueTypes.STRING, ValueTypeString.ValueString.of("$.a.b")),
                nempty
        });
        assertThat("path_test(empty) = false", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtPathTestInvalidPathExpression() throws EvaluationException {
        Operators.NBT_PATH_TEST.evaluate(new IVariable[]{nstring, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtPathTestSizeLarge() throws EvaluationException {
        Operators.NBT_PATH_TEST.evaluate(new IVariable[]{nstring, nempty, nempty});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputNbtPathTestSizeSmall() throws EvaluationException {
        Operators.NBT_PATH_TEST.evaluate(new IVariable[]{nstring});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNbtPathTest() throws EvaluationException {
        Operators.NBT_PATH_TEST.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

}
