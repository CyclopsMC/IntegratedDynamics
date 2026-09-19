package org.cyclops.integrateddynamics.core.helper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.cyclopscore.helper.CyclopsCoreInstance;
import org.cyclops.integrateddynamics.ModBaseMocked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.core.evaluate.operator.CurriedOperator;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.operator.PositionedOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueDeseralizationContextMocked;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyFactories;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.Variable;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test the copying of values to, and pasting of values from, the clipboard.
 * @author rubensworks
 */
public class TestVariableClipboardHelpers {

    static { CyclopsCoreInstance.MOD = new ModBaseMocked(); }

    private static final int MAX_LENGTH = 65536;
    private static final ResourceLocation POSITIONED_SERIALIZER =
            ResourceLocation.fromNamespaceAndPath("integrateddynamics", "test_positioned");

    public static final ValueDeseralizationContext CONTEXT = ValueDeseralizationContextMocked.get();

    @BeforeClass
    public static void before() {
        ValueTypeListProxyFactories.load();
        Operators.load();
        Operators.REGISTRY.registerSerializer(new PositionedOperator.Serializer(
                PositionedOperator.class, POSITIONED_SERIALIZER));
    }

    protected void assertRoundTrip(IValue value) throws VariableClipboardHelpers.VariableClipboardException {
        String readable = VariableClipboardHelpers.serialize(CONTEXT, value);
        assertThat("the readable form round-trips",
                VariableClipboardHelpers.deserialize(CONTEXT, readable, MAX_LENGTH), is(value));

        String compressed = VariableClipboardHelpers.serializeCompressed(CONTEXT, value);
        assertThat("the compressed form is prefixed",
                compressed, startsWith(VariableClipboardHelpers.PREFIX_COMPRESSED));
        assertThat("the compressed form is a single line", compressed.contains("\n"), is(false));
        assertThat("the compressed form round-trips",
                VariableClipboardHelpers.deserialize(CONTEXT, compressed, MAX_LENGTH), is(value));
    }

    @Test
    public void testBoolean() throws VariableClipboardHelpers.VariableClipboardException {
        assertRoundTrip(ValueTypeBoolean.ValueBoolean.of(true));
    }

    @Test
    public void testInteger() throws VariableClipboardHelpers.VariableClipboardException {
        assertRoundTrip(ValueTypeInteger.ValueInteger.of(42));
    }

    @Test
    public void testString() throws VariableClipboardHelpers.VariableClipboardException {
        assertRoundTrip(ValueTypeString.ValueString.of("abc"));
    }

    @Test
    public void testMaterializedList() throws VariableClipboardHelpers.VariableClipboardException {
        assertRoundTrip(ValueTypeList.ValueList.ofAll(
                ValueTypeInteger.ValueInteger.of(1),
                ValueTypeInteger.ValueInteger.of(2),
                ValueTypeInteger.ValueInteger.of(3)
        ));
    }

    @Test
    public void testCurriedOperator() throws VariableClipboardHelpers.VariableClipboardException {
        assertRoundTrip(ValueTypeOperator.ValueOperator.of(new CurriedOperator(
                Operators.ARITHMETIC_ADDITION,
                new Variable<>(ValueTypeInteger.ValueInteger.of(1))
        )));
    }

    @Test
    public void testReadableAndCompressedAreEqual() throws VariableClipboardHelpers.VariableClipboardException {
        IValue value = ValueTypeString.ValueString.of("Hello world");
        String readable = VariableClipboardHelpers.serialize(CONTEXT, value);
        String compressed = VariableClipboardHelpers.serializeCompressed(CONTEXT, value);
        assertThat("both forms are different strings", compressed, is(not(readable)));
        assertThat("both forms deserialize to the same value",
                VariableClipboardHelpers.deserialize(CONTEXT, compressed, MAX_LENGTH),
                is(VariableClipboardHelpers.deserialize(CONTEXT, readable, MAX_LENGTH)));
    }

    @Test
    public void testWhitespaceIsIgnored() throws VariableClipboardHelpers.VariableClipboardException {
        IValue value = ValueTypeInteger.ValueInteger.of(7);
        assertThat("surrounding whitespace is trimmed", VariableClipboardHelpers.deserialize(CONTEXT,
                "  " + VariableClipboardHelpers.serialize(CONTEXT, value) + "\n", MAX_LENGTH), is(value));
    }

    protected void assertRejected(String input, int maxLength) {
        try {
            VariableClipboardHelpers.deserialize(CONTEXT, input, maxLength);
            fail("Expected the payload to be rejected: " + input);
        } catch (VariableClipboardHelpers.VariableClipboardException e) {
            // Expected
        }
    }

    @Test
    public void testRejectGarbage() {
        assertRejected("this is not a value", MAX_LENGTH);
    }

    @Test
    public void testRejectEmpty() {
        assertRejected("", MAX_LENGTH);
    }

    @Test
    public void testRejectMalformedSnbt() {
        assertRejected("{version:1,valueType:", MAX_LENGTH);
    }

    @Test
    public void testRejectMalformedCompressed() {
        assertRejected(VariableClipboardHelpers.PREFIX_COMPRESSED + "not-base-64-%%%", MAX_LENGTH);
    }

    @Test
    public void testRejectTruncatedCompressed() throws VariableClipboardHelpers.VariableClipboardException {
        String compressed = VariableClipboardHelpers.serializeCompressed(CONTEXT, ValueTypeInteger.ValueInteger.of(1));
        assertRejected(compressed.substring(0, compressed.length() - 10), MAX_LENGTH);
    }

    @Test
    public void testRejectNewerVersion() {
        CompoundTag tag = VariableClipboardHelpers.serializeToTag(CONTEXT, ValueTypeInteger.ValueInteger.of(1));
        tag.putInt(VariableClipboardHelpers.KEY_VERSION, VariableClipboardHelpers.VERSION + 1);
        assertRejected(tag.toString(), MAX_LENGTH);
    }

    @Test
    public void testRejectUnknownValueType() {
        CompoundTag tag = VariableClipboardHelpers.serializeToTag(CONTEXT, ValueTypeInteger.ValueInteger.of(1));
        tag.putString("valueType", "integrateddynamics:nonexistent");
        assertRejected(tag.toString(), MAX_LENGTH);
    }

    @Test
    public void testRejectTooLargeReadable() {
        String readable = VariableClipboardHelpers.serialize(CONTEXT, ValueTypeString.ValueString.of("abcdefghij"));
        assertRejected(readable, readable.length() - 1);
    }

    @Test
    public void testRejectTooLargeAfterDecompression() throws VariableClipboardHelpers.VariableClipboardException {
        // A compressed payload is short, but must still be rejected once it expands beyond the maximum
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longString.append('a');
        }
        String compressed = VariableClipboardHelpers.serializeCompressed(CONTEXT,
                ValueTypeString.ValueString.of(longString.toString()));
        assertThat("the compressed payload itself is below the maximum", compressed.length() < 1000, is(true));
        assertRejected(compressed, 1000);
    }

    @Test
    public void testRejectUnmaterializedList() {
        CompoundTag proxy = new CompoundTag();
        proxy.putString(VariableClipboardHelpers.KEY_PROXY_NAME, "integrateddynamics:positioned_inventory");
        proxy.put("serialized", new CompoundTag());

        CompoundTag tag = new CompoundTag();
        tag.putInt(VariableClipboardHelpers.KEY_VERSION, VariableClipboardHelpers.VERSION);
        tag.putString("valueType", "integrateddynamics:list");
        tag.put("value", proxy);

        assertRejected(tag.toString(), MAX_LENGTH);
    }

    @Test
    public void testRejectUnknownListProxy() {
        CompoundTag proxy = new CompoundTag();
        proxy.putString(VariableClipboardHelpers.KEY_PROXY_NAME, "integrateddynamics:nonexistent");
        proxy.put("serialized", new CompoundTag());

        CompoundTag tag = new CompoundTag();
        tag.putInt(VariableClipboardHelpers.KEY_VERSION, VariableClipboardHelpers.VERSION);
        tag.putString("valueType", "integrateddynamics:list");
        tag.put("value", proxy);

        assertRejected(tag.toString(), MAX_LENGTH);
    }

    @Test
    public void testRejectPositionedOperator() {
        CompoundTag operator = new CompoundTag();
        operator.putString(VariableClipboardHelpers.KEY_SERIALIZER, POSITIONED_SERIALIZER.toString());
        operator.put("value", new CompoundTag());

        CompoundTag tag = new CompoundTag();
        tag.putInt(VariableClipboardHelpers.KEY_VERSION, VariableClipboardHelpers.VERSION);
        tag.putString("valueType", "integrateddynamics:operator");
        tag.put("value", operator);

        assertRejected(tag.toString(), MAX_LENGTH);
    }

    @Test
    public void testRejectNestedUnmaterializedValue() {
        // The offending proxy is nested inside a materialized list, so a shallow check would miss it
        CompoundTag proxy = new CompoundTag();
        proxy.putString(VariableClipboardHelpers.KEY_PROXY_NAME, "integrateddynamics:positioned_inventory");
        proxy.put("serialized", new CompoundTag());

        CompoundTag inner = new CompoundTag();
        inner.putString("valueType", "integrateddynamics:list");
        inner.put("value", proxy);

        CompoundTag materialized = new CompoundTag();
        materialized.putString(VariableClipboardHelpers.KEY_PROXY_NAME, "integrateddynamics:materialized");
        materialized.put("serialized", inner);

        CompoundTag tag = new CompoundTag();
        tag.putInt(VariableClipboardHelpers.KEY_VERSION, VariableClipboardHelpers.VERSION);
        tag.putString("valueType", "integrateddynamics:list");
        tag.put("value", materialized);

        assertRejected(tag.toString(), MAX_LENGTH);
    }

    @Test
    public void testRejectMalformedProxyName() {
        CompoundTag proxy = new CompoundTag();
        proxy.putString(VariableClipboardHelpers.KEY_PROXY_NAME, "NOT A RESOURCE LOCATION");
        proxy.put("serialized", new CompoundTag());

        CompoundTag tag = new CompoundTag();
        tag.putInt(VariableClipboardHelpers.KEY_VERSION, VariableClipboardHelpers.VERSION);
        tag.putString("valueType", "integrateddynamics:list");
        tag.put("value", proxy);

        assertRejected(tag.toString(), MAX_LENGTH);
    }

    @Test
    public void testRejectDeeplyNested() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(VariableClipboardHelpers.KEY_VERSION, VariableClipboardHelpers.VERSION);
        tag.putString("valueType", "integrateddynamics:integer");
        CompoundTag nested = tag;
        for (int i = 0; i < VariableClipboardHelpers.MAX_DEPTH + 10; i++) {
            CompoundTag next = new CompoundTag();
            nested.put("value", next);
            nested = next;
        }
        assertRejected(tag.toString(), MAX_LENGTH);
    }

    @Test
    public void testAcceptsPlainOperatorName() throws VariableClipboardHelpers.VariableClipboardException {
        // Operators without a dedicated serializer are stored as a plain name
        CompoundTag tag = new CompoundTag();
        tag.putInt(VariableClipboardHelpers.KEY_VERSION, VariableClipboardHelpers.VERSION);
        tag.putString("valueType", "integrateddynamics:operator");
        tag.put("value", StringTag.valueOf(Operators.ARITHMETIC_ADDITION.getUniqueName().toString()));

        assertThat("a plain operator is accepted",
                VariableClipboardHelpers.deserialize(CONTEXT, tag.toString(), MAX_LENGTH),
                is(ValueTypeOperator.ValueOperator.of(Operators.ARITHMETIC_ADDITION)));
    }

}
