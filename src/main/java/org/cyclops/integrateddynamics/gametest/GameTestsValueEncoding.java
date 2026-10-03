package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyPositionedInventory;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.evaluate.variable.Variable;
import org.cyclops.integrateddynamics.core.helper.ValueEncodingHelpers;
import org.cyclops.integrateddynamics.core.logicprogrammer.DecodeLPElement;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.assertValueEqual;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.getVariableFacade;

public class GameTestsValueEncoding {

    public static final String TEMPLATE_EMPTY = "integrateddynamics:empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    private static final int MAX_LENGTH = 65536;

    protected static ValueDeseralizationContext context(GameTestHelper helper) {
        return ValueDeseralizationContext.of(helper.getLevel());
    }

    protected static IValue itemStackList() {
        return ValueTypeList.ValueList.ofAll(
                ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.DIAMOND, 3)),
                ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.STICK))
        );
    }

    protected static IValue positionedList(GameTestHelper helper) {
        return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyPositionedInventory(
                DimPos.of(helper.getLevel(), helper.absolutePos(POS)), Direction.UP));
    }

    /**
     * Encoding refuses values that are not materialized, so a handcrafted one has to bypass it.
     */
    protected static String handcraftEncoded(GameTestHelper helper, IValue value) {
        TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, context(helper).holderLookupProvider());
        ValueHelpers.serialize(valueOutput, value);
        return valueOutput.buildResult().toString();
    }

    /**
     * Decode a value into a variable card, the way the decode element of the logic programmer does.
     */
    protected static ItemStack decodeIntoCard(GameTestHelper helper, String encoded) throws ValueEncodingHelpers.ValueEncodingException {
        DecodeLPElement element = new DecodeLPElement();
        element.setValue(ValueEncodingHelpers.decode(context(helper), encoded, MAX_LENGTH));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        return element.writeElement(player, new ItemStack(RegistryEntries.ITEM_VARIABLE.get()));
    }

    protected static void assertCardHolds(GameTestHelper helper, ItemStack card, IValue value) {
        IVariableFacade facade = getVariableFacade(helper.getLevel(), card);
        helper.assertTrue(facade.isValid(), "The decoded variable card is invalid");
        helper.assertTrue(facade instanceof IValueTypeVariableFacade, "The decoded variable card does not hold a value");
        assertValueEqual(helper, ((IValueTypeVariableFacade<?>) facade).getValue(), value);
    }

    protected static IValue encodeOperator(IValue value) throws EvaluationException {
        return Operators.GENERAL_ENCODE.evaluate(new Variable<>(value));
    }

    protected static IValue encodeCompressedOperator(IValue value) throws EvaluationException {
        return Operators.GENERAL_ENCODE_COMPRESSED.evaluate(new Variable<>(value));
    }

    protected static IValue decodeOperator(String encoded) throws EvaluationException {
        return Operators.GENERAL_DECODE.evaluate(new Variable<>(ValueTypeString.ValueString.of(encoded)));
    }

    // ------------------------------- Decode element -------------------------------

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDecodeElementInteger(GameTestHelper helper) throws ValueEncodingHelpers.ValueEncodingException {
        IValue value = ValueTypeInteger.ValueInteger.of(42);
        assertCardHolds(helper, decodeIntoCard(helper, ValueEncodingHelpers.encode(context(helper), value, MAX_LENGTH)), value);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDecodeElementString(GameTestHelper helper) throws ValueEncodingHelpers.ValueEncodingException {
        IValue value = ValueTypeString.ValueString.of("Hello world");
        assertCardHolds(helper, decodeIntoCard(helper, ValueEncodingHelpers.encode(context(helper), value, MAX_LENGTH)), value);
        helper.succeed();
    }

    /**
     * Item stacks are only serializable with a registry context, so they can not be covered by a unit test.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testDecodeElementItemStackList(GameTestHelper helper) throws ValueEncodingHelpers.ValueEncodingException {
        IValue value = itemStackList();
        assertCardHolds(helper, decodeIntoCard(helper, ValueEncodingHelpers.encode(context(helper), value, MAX_LENGTH)), value);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDecodeElementCompressed(GameTestHelper helper) throws ValueEncodingHelpers.ValueEncodingException {
        IValue value = ValueTypeInteger.ValueInteger.of(123);
        assertCardHolds(helper, decodeIntoCard(helper, ValueEncodingHelpers.encodeCompressed(context(helper), value, MAX_LENGTH)), value);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDecodeElementRequiresValue(GameTestHelper helper) {
        DecodeLPElement element = new DecodeLPElement();
        helper.assertFalse(element.canWriteElementPre(), "An empty decode element can be written");
        helper.assertTrue(element.validate() != null, "An empty decode element has no error");

        element.setValue(ValueTypeInteger.ValueInteger.of(1));
        helper.assertTrue(element.canWriteElementPre(), "A filled decode element can not be written");
        helper.assertTrue(element.validate() == null, "A filled decode element has an error");

        element.deactivate();
        helper.assertFalse(element.canWriteElementPre(), "A deactivated decode element can be written");
        helper.succeed();
    }

    // ------------------------------- Materialization -------------------------------

    /**
     * A list that reads an inventory at a position must not be decodable,
     * as it would otherwise allow reading inventories anywhere.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testDecodeRejectsPositionedList(GameTestHelper helper) {
        try {
            ValueEncodingHelpers.decode(context(helper), handcraftEncoded(helper, positionedList(helper)), MAX_LENGTH);
            helper.fail("A positioned list was decoded");
        } catch (ValueEncodingHelpers.ValueEncodingException e) {
            // Expected
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testEncodeRejectsPositionedList(GameTestHelper helper) {
        try {
            ValueEncodingHelpers.encode(context(helper), positionedList(helper), MAX_LENGTH);
            helper.fail("A positioned list was encoded, which could never be decoded");
        } catch (ValueEncodingHelpers.ValueEncodingException e) {
            // Expected
        }
        helper.succeed();
    }

    // ------------------------------- Operators -------------------------------

    @GameTest(template = TEMPLATE_EMPTY)
    public void testEncodeDecodeOperators(GameTestHelper helper) throws EvaluationException {
        IValue value = itemStackList();
        IValue encoded = encodeOperator(value);
        helper.assertTrue(encoded.getType() == ValueTypes.STRING, "Encoding does not give a string");
        assertValueEqual(helper, decodeOperator(((ValueTypeString.ValueString) encoded).getRawValue()), value);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testEncodeCompressedDecodeOperators(GameTestHelper helper) throws EvaluationException {
        IValue value = itemStackList();
        String encoded = ((ValueTypeString.ValueString) encodeCompressedOperator(value)).getRawValue();
        helper.assertTrue(encoded.startsWith(ValueEncodingHelpers.PREFIX_COMPRESSED), "Encoding is not compressed");
        assertValueEqual(helper, decodeOperator(encoded), value);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testEncodeCompressedOperatorMaterializes(GameTestHelper helper) throws EvaluationException {
        helper.setBlock(POS, Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(POS, ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.DIAMOND, 3));

        IValue positioned = positionedList(helper);
        String encoded = ((ValueTypeString.ValueString) encodeCompressedOperator(positioned)).getRawValue();
        assertValueEqual(helper, decodeOperator(encoded), ValueTypes.LIST.materialize((ValueTypeList.ValueList) positioned));
        helper.succeed();
    }

    /**
     * The encode operator materializes its input, so a list that reads an inventory can be encoded,
     * and decodes into the contents that the inventory had at that moment.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testEncodeOperatorMaterializes(GameTestHelper helper) throws EvaluationException {
        helper.setBlock(POS, Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(POS, ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.DIAMOND, 3));
        chest.setItem(1, new ItemStack(Items.STICK));

        IValue positioned = positionedList(helper);
        IValue encoded = encodeOperator(positioned);
        IValue decoded = decodeOperator(((ValueTypeString.ValueString) encoded).getRawValue());
        assertValueEqual(helper, decoded, ValueTypes.LIST.materialize((ValueTypeList.ValueList) positioned));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testEncodeOperatorTooLarge(GameTestHelper helper) {
        int maxLength = GeneralConfig.valueEncodingMaxLength;
        GeneralConfig.valueEncodingMaxLength = 10;
        try {
            encodeOperator(ValueTypeString.ValueString.of("This is longer than ten characters"));
            helper.fail("A value beyond the maximum length was encoded");
        } catch (EvaluationException e) {
            // Expected
        } finally {
            GeneralConfig.valueEncodingMaxLength = maxLength;
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDecodeOperatorCompressed(GameTestHelper helper) throws EvaluationException, ValueEncodingHelpers.ValueEncodingException {
        IValue value = ValueTypeString.ValueString.of("Hello world");
        assertValueEqual(helper, decodeOperator(ValueEncodingHelpers.encodeCompressed(context(helper), value, MAX_LENGTH)), value);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDecodeOperatorRejectsGarbage(GameTestHelper helper) {
        try {
            decodeOperator("this is not a value");
            helper.fail("Garbage was decoded into a value");
        } catch (EvaluationException e) {
            // Expected
        }
        helper.succeed();
    }

    /**
     * Strings can be built with operators, so the operator must not accept what the decode element refuses.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testDecodeOperatorRejectsPositionedList(GameTestHelper helper) {
        try {
            decodeOperator(handcraftEncoded(helper, positionedList(helper)));
            helper.fail("A positioned list was decoded into a value");
        } catch (EvaluationException e) {
            // Expected
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDecodeOperatorDisabled(GameTestHelper helper) throws ValueEncodingHelpers.ValueEncodingException {
        String encoded = ValueEncodingHelpers.encode(context(helper), ValueTypeInteger.ValueInteger.of(1), MAX_LENGTH);
        boolean enabled = GeneralConfig.valueDecodingEnabled;
        GeneralConfig.valueDecodingEnabled = false;
        try {
            decodeOperator(encoded);
            helper.fail("A value was decoded while decoding is disabled");
        } catch (EvaluationException e) {
            // Expected
        } finally {
            GeneralConfig.valueDecodingEnabled = enabled;
        }
        helper.succeed();
    }

}
