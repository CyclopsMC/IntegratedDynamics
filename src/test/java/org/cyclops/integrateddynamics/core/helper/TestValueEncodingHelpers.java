package org.cyclops.integrateddynamics.core.helper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import org.cyclops.cyclopscore.helper.CyclopsCoreInstance;
import org.cyclops.integrateddynamics.GeneralConfig;
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
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyAppend;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyFactories;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.Variable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test the encoding and decoding of values.
 * @author rubensworks
 */
public class TestValueEncodingHelpers {

    static { CyclopsCoreInstance.MOD = new ModBaseMocked(); }

    private static final int MAX_LENGTH = 65536;
    private static final Identifier POSITIONED_SERIALIZER =
            Identifier.fromNamespaceAndPath("integrateddynamics", "test_positioned");

    public static final ValueDeseralizationContext CONTEXT = ValueDeseralizationContextMocked.get();

    @BeforeAll
    public static void before() {
        ValueTypeListProxyFactories.load();
        Operators.load();
        Operators.REGISTRY.registerSerializer(new PositionedOperator.Serializer(
                PositionedOperator.class, POSITIONED_SERIALIZER));
    }

    protected void assertRoundTrip(IValue value) throws ValueEncodingHelpers.ValueEncodingException {
        String readable = ValueEncodingHelpers.encode(CONTEXT, value, MAX_LENGTH);
        assertThat("the readable form round-trips",
                ValueEncodingHelpers.decode(CONTEXT, readable, MAX_LENGTH), is(value));

        String compressed = ValueEncodingHelpers.encodeCompressed(CONTEXT, value, MAX_LENGTH);
        assertThat("the compressed form is prefixed",
                compressed, startsWith(ValueEncodingHelpers.PREFIX_COMPRESSED));
        assertThat("the compressed form is a single line", compressed.contains("\n"), is(false));
        assertThat("the compressed form round-trips",
                ValueEncodingHelpers.decode(CONTEXT, compressed, MAX_LENGTH), is(value));
    }

    @Test
    public void testBoolean() throws ValueEncodingHelpers.ValueEncodingException {
        assertRoundTrip(ValueTypeBoolean.ValueBoolean.of(true));
    }

    @Test
    public void testInteger() throws ValueEncodingHelpers.ValueEncodingException {
        assertRoundTrip(ValueTypeInteger.ValueInteger.of(42));
    }

    @Test
    public void testString() throws ValueEncodingHelpers.ValueEncodingException {
        assertRoundTrip(ValueTypeString.ValueString.of("abc"));
    }

    @Test
    public void testMaterializedList() throws ValueEncodingHelpers.ValueEncodingException {
        assertRoundTrip(ValueTypeList.ValueList.ofAll(
                ValueTypeInteger.ValueInteger.of(1),
                ValueTypeInteger.ValueInteger.of(2),
                ValueTypeInteger.ValueInteger.of(3)
        ));
    }

    @Test
    public void testCurriedOperator() throws ValueEncodingHelpers.ValueEncodingException {
        assertRoundTrip(ValueTypeOperator.ValueOperator.of(new CurriedOperator(
                Operators.ARITHMETIC_ADDITION,
                new Variable<>(ValueTypeInteger.ValueInteger.of(1))
        )));
    }

    @Test
    public void testReadableAndCompressedAreEqual() throws ValueEncodingHelpers.ValueEncodingException {
        IValue value = ValueTypeString.ValueString.of("Hello world");
        String readable = ValueEncodingHelpers.encode(CONTEXT, value, MAX_LENGTH);
        String compressed = ValueEncodingHelpers.encodeCompressed(CONTEXT, value, MAX_LENGTH);
        assertThat("both forms are different strings", compressed, is(not(readable)));
        assertThat("both forms decode to the same value",
                ValueEncodingHelpers.decode(CONTEXT, compressed, MAX_LENGTH),
                is(ValueEncodingHelpers.decode(CONTEXT, readable, MAX_LENGTH)));
    }

    @Test
    public void testWhitespaceIsIgnored() throws ValueEncodingHelpers.ValueEncodingException {
        IValue value = ValueTypeInteger.ValueInteger.of(7);
        assertThat("surrounding whitespace is trimmed", ValueEncodingHelpers.decode(CONTEXT,
                "  " + ValueEncodingHelpers.encode(CONTEXT, value, MAX_LENGTH) + "\n", MAX_LENGTH), is(value));
    }

    @Test
    public void testEncodeRejectsTooLarge() {
        try {
            ValueEncodingHelpers.encode(CONTEXT, ValueTypeString.ValueString.of("abcdefghij"), 10);
            fail("Expected a value beyond the maximum length to not be encoded");
        } catch (ValueEncodingHelpers.ValueEncodingException e) {
            // Expected
        }
    }

    @Test
    public void testEncodeRejectsUnmaterialized() {
        // An appended list refers to its source list, so it must be materialized before it can be encoded
        IValue value = ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyAppend(
                ValueTypeList.ValueList.ofAll(ValueTypeInteger.ValueInteger.of(1)).getRawValue(),
                ValueTypeInteger.ValueInteger.of(2)));
        try {
            ValueEncodingHelpers.encode(CONTEXT, value, MAX_LENGTH);
            fail("Expected a value that is not materialized to not be encoded");
        } catch (ValueEncodingHelpers.ValueEncodingException e) {
            // Expected
        }
    }

    protected void assertRejected(String input, int maxLength) {
        try {
            ValueEncodingHelpers.decode(CONTEXT, input, maxLength);
            fail("Expected the payload to be rejected: " + input);
        } catch (ValueEncodingHelpers.ValueEncodingException e) {
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
        assertRejected(ValueEncodingHelpers.PREFIX_COMPRESSED + "not-base-64-%%%", MAX_LENGTH);
    }

    @Test
    public void testRejectTruncatedCompressed() throws ValueEncodingHelpers.ValueEncodingException {
        String compressed = ValueEncodingHelpers.encodeCompressed(CONTEXT, ValueTypeInteger.ValueInteger.of(1), MAX_LENGTH);
        assertRejected(compressed.substring(0, compressed.length() - 10), MAX_LENGTH);
    }

    @Test
    public void testWritesVersions() throws ValueEncodingHelpers.ValueEncodingException {
        CompoundTag version = ValueEncodingHelpers.encodeToTag(CONTEXT, ValueTypeInteger.ValueInteger.of(1), MAX_LENGTH)
                .getCompoundOrEmpty(ValueEncodingHelpers.KEY_VERSION);
        assertThat("the Minecraft version is written",
                version.getStringOr(ValueEncodingHelpers.KEY_VERSION_MINECRAFT, "").isEmpty(), is(false));
        assertThat("the mod version is written",
                version.getStringOr(ValueEncodingHelpers.KEY_VERSION_MOD, "").isEmpty(), is(false));
    }

    @Test
    public void testIgnoresVersions() throws ValueEncodingHelpers.ValueEncodingException {
        IValue value = ValueTypeInteger.ValueInteger.of(1);

        // Versions are only informative, so neither unknown, legacy nor missing versions reject a value
        CompoundTag otherVersions = ValueEncodingHelpers.encodeToTag(CONTEXT, value, MAX_LENGTH);
        CompoundTag version = new CompoundTag();
        version.putString(ValueEncodingHelpers.KEY_VERSION_MINECRAFT, "99.0");
        version.putString(ValueEncodingHelpers.KEY_VERSION_MOD, "99.0.0");
        otherVersions.put(ValueEncodingHelpers.KEY_VERSION, version);
        assertThat("other versions are accepted",
                ValueEncodingHelpers.decode(CONTEXT, otherVersions.toString(), MAX_LENGTH), is(value));

        CompoundTag legacyVersion = ValueEncodingHelpers.encodeToTag(CONTEXT, value, MAX_LENGTH);
        legacyVersion.putInt(ValueEncodingHelpers.KEY_VERSION, 42);
        assertThat("a legacy version is accepted",
                ValueEncodingHelpers.decode(CONTEXT, legacyVersion.toString(), MAX_LENGTH), is(value));

        CompoundTag noVersion = ValueEncodingHelpers.encodeToTag(CONTEXT, value, MAX_LENGTH);
        noVersion.remove(ValueEncodingHelpers.KEY_VERSION);
        assertThat("a missing version is accepted",
                ValueEncodingHelpers.decode(CONTEXT, noVersion.toString(), MAX_LENGTH), is(value));
    }

    @Test
    public void testRejectMissingValueType() {
        assertRejected("{v:1}", MAX_LENGTH);
        assertRejected("{valueType:\"integrateddynamics:integer\"}", MAX_LENGTH);
    }

    @Test
    public void testRejectUnknownValueType() throws ValueEncodingHelpers.ValueEncodingException {
        CompoundTag tag = ValueEncodingHelpers.encodeToTag(CONTEXT, ValueTypeInteger.ValueInteger.of(1), MAX_LENGTH);
        tag.putString("valueType", "integrateddynamics:nonexistent");
        assertRejected(tag.toString(), MAX_LENGTH);
    }

    @Test
    public void testRejectTooLargeReadable() throws ValueEncodingHelpers.ValueEncodingException {
        String readable = ValueEncodingHelpers.encode(CONTEXT, ValueTypeString.ValueString.of("abcdefghij"), MAX_LENGTH);
        assertRejected(readable, readable.length() - 1);
    }

    @Test
    public void testRejectTooLargeAfterDecompression() throws ValueEncodingHelpers.ValueEncodingException {
        // A compressed payload is short, but must still be rejected once it expands beyond the maximum
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longString.append('a');
        }
        String compressed = ValueEncodingHelpers.encodeCompressed(CONTEXT,
                ValueTypeString.ValueString.of(longString.toString()), MAX_LENGTH);
        assertThat("the compressed payload itself is below the maximum", compressed.length() < 1000, is(true));
        assertRejected(compressed, 1000);
    }

    @Test
    public void testRejectUnmaterializedList() {
        CompoundTag tag = new CompoundTag();
        tag.putString("valueType", "integrateddynamics:list");
        tag.putString(ValueEncodingHelpers.KEY_PROXY_NAME, "integrateddynamics:positioned_inventory");
        tag.put("serialized", new CompoundTag());

        assertRejected(tag.toString(), MAX_LENGTH);
    }

    @Test
    public void testRejectUnknownListProxy() {
        CompoundTag tag = new CompoundTag();
        tag.putString("valueType", "integrateddynamics:list");
        tag.putString(ValueEncodingHelpers.KEY_PROXY_NAME, "integrateddynamics:nonexistent");
        tag.put("serialized", new CompoundTag());

        assertRejected(tag.toString(), MAX_LENGTH);
    }

    @Test
    public void testRejectPositionedOperator() {
        CompoundTag operator = new CompoundTag();
        operator.putString(ValueEncodingHelpers.KEY_SERIALIZER, POSITIONED_SERIALIZER.toString());
        operator.put("value", new CompoundTag());

        CompoundTag tag = new CompoundTag();
        tag.putString("valueType", "integrateddynamics:operator");
        tag.put("v", operator);

        assertRejected(tag.toString(), MAX_LENGTH);
    }

    @Test
    public void testRejectNestedUnmaterializedValue() {
        // The offending proxy is nested inside a materialized list, so a shallow check would miss it
        CompoundTag inner = new CompoundTag();
        inner.putString("valueType", "integrateddynamics:list");
        inner.putString(ValueEncodingHelpers.KEY_PROXY_NAME, "integrateddynamics:positioned_inventory");
        inner.put("serialized", new CompoundTag());

        ListTag values = new ListTag();
        values.add(inner);
        CompoundTag materialized = new CompoundTag();
        materialized.putString("valueType", "integrateddynamics:any");
        materialized.put("values", values);

        CompoundTag tag = new CompoundTag();
        tag.putString("valueType", "integrateddynamics:list");
        tag.putString(ValueEncodingHelpers.KEY_PROXY_NAME, "integrateddynamics:materialized");
        tag.put("serialized", materialized);

        assertRejected(tag.toString(), MAX_LENGTH);
    }

    @Test
    public void testRejectMalformedProxyName() {
        CompoundTag tag = new CompoundTag();
        tag.putString("valueType", "integrateddynamics:list");
        tag.putString(ValueEncodingHelpers.KEY_PROXY_NAME, "NOT A RESOURCE LOCATION");
        tag.put("serialized", new CompoundTag());

        assertRejected(tag.toString(), MAX_LENGTH);
    }

    @Test
    public void testRejectDeeplyNested() {
        CompoundTag tag = new CompoundTag();
        tag.putString("valueType", "integrateddynamics:integer");
        CompoundTag nested = tag;
        for (int i = 0; i < GeneralConfig.valueEncodingMaxDepth + 10; i++) {
            CompoundTag next = new CompoundTag();
            nested.put("v", next);
            nested = next;
        }
        assertRejected(tag.toString(), MAX_LENGTH);
    }

    @Test
    public void testRejectNestedWithinDepthOnSmallStack() throws InterruptedException {
        // The SNBT parser may overflow the stack before reaching the max depth
        StringBuilder snbt = new StringBuilder("{valueType:\"integrateddynamics:integer\"");
        for (int i = 0; i < GeneralConfig.valueEncodingMaxDepth - 1; i++) {
            snbt.append(",v:{a:1");
        }
        for (int i = 0; i < GeneralConfig.valueEncodingMaxDepth; i++) {
            snbt.append("}");
        }
        Throwable[] thrown = new Throwable[1];
        Thread thread = new Thread(null, () -> {
            try {
                assertRejected(snbt.toString(), MAX_LENGTH);
            } catch (Throwable e) {
                thrown[0] = e;
            }
        }, "small-stack", 64 * 1024);
        thread.start();
        thread.join();
        assertThat("no error is thrown", thrown[0], is((Throwable) null));
    }

    @Test
    public void testBracketsInStringsDoNotCountAsNesting() throws ValueEncodingHelpers.ValueEncodingException {
        String brackets = "{[\"'".repeat(GeneralConfig.valueEncodingMaxDepth + 10);
        IValue value = ValueTypeString.ValueString.of(brackets);
        assertThat("brackets in strings are decoded",
                ValueEncodingHelpers.decode(CONTEXT, ValueEncodingHelpers.encode(CONTEXT, value, MAX_LENGTH), MAX_LENGTH),
                is(value));
    }

    @Test
    public void testAcceptsPlainOperatorName() throws ValueEncodingHelpers.ValueEncodingException {
        // Operators without a dedicated serializer are stored as a plain name
        CompoundTag tag = new CompoundTag();
        tag.putString("valueType", "integrateddynamics:operator");
        CompoundTag operator = new CompoundTag();
        operator.putString("v", Operators.ARITHMETIC_ADDITION.getUniqueName().toString());
        tag.put("v", operator);

        assertThat("a plain operator is accepted",
                ValueEncodingHelpers.decode(CONTEXT, tag.toString(), MAX_LENGTH),
                is(ValueTypeOperator.ValueOperator.of(Operators.ARITHMETIC_ADDITION)));
    }

}
