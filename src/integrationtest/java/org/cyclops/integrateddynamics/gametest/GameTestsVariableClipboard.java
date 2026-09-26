package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyPositionedInventory;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.helper.VariableClipboardHelpers;
import org.cyclops.integrateddynamics.core.logicprogrammer.ClipboardLPElement;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.assertValueEqual;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.getVariableFacade;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsVariableClipboard {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    private static final int MAX_LENGTH = 65536;

    /**
     * Paste a value into a variable card, the way the clipboard element of the logic programmer does.
     */
    protected static ItemStack pasteIntoCard(GameTestHelper helper, IValue value) throws VariableClipboardHelpers.VariableClipboardException {
        ValueDeseralizationContext context = ValueDeseralizationContext.of(helper.getLevel());
        String clipboard = VariableClipboardHelpers.serialize(context, value);

        ClipboardLPElement element = new ClipboardLPElement();
        element.setValue(VariableClipboardHelpers.deserialize(context, clipboard, MAX_LENGTH));

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        return element.writeElement(player, new ItemStack(RegistryEntries.ITEM_VARIABLE.get()));
    }

    protected static void assertPastedCardHolds(GameTestHelper helper, IValue value) throws VariableClipboardHelpers.VariableClipboardException {
        ItemStack card = pasteIntoCard(helper, value);
        IVariableFacade facade = getVariableFacade(helper.getLevel(), card);
        helper.assertTrue(facade.isValid(), "The pasted variable card is invalid");
        helper.assertTrue(facade instanceof IValueTypeVariableFacade, "The pasted variable card does not hold a value");
        assertValueEqual(((IValueTypeVariableFacade<?>) facade).getValue(), value);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPasteInteger(GameTestHelper helper) throws VariableClipboardHelpers.VariableClipboardException {
        assertPastedCardHolds(helper, ValueTypeInteger.ValueInteger.of(42));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPasteString(GameTestHelper helper) throws VariableClipboardHelpers.VariableClipboardException {
        assertPastedCardHolds(helper, ValueTypeString.ValueString.of("Hello world"));
        helper.succeed();
    }

    /**
     * Item stacks are only serializable with a registry context, so they can not be covered by a unit test.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testPasteMaterializedItemStackList(GameTestHelper helper) throws VariableClipboardHelpers.VariableClipboardException {
        assertPastedCardHolds(helper, ValueTypeList.ValueList.ofAll(
                ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.DIAMOND, 3)),
                ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.STICK))
        ));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPasteCompressed(GameTestHelper helper) throws VariableClipboardHelpers.VariableClipboardException {
        ValueDeseralizationContext context = ValueDeseralizationContext.of(helper.getLevel());
        IValue value = ValueTypeInteger.ValueInteger.of(123);
        String compressed = VariableClipboardHelpers.serializeCompressed(context, value);

        ClipboardLPElement element = new ClipboardLPElement();
        element.setValue(VariableClipboardHelpers.deserialize(context, compressed, MAX_LENGTH));
        ItemStack card = element.writeElement(helper.makeMockPlayer(GameType.SURVIVAL),
                new ItemStack(RegistryEntries.ITEM_VARIABLE.get()));

        IVariableFacade facade = getVariableFacade(helper.getLevel(), card);
        helper.assertTrue(facade.isValid(), "The pasted variable card is invalid");
        assertValueEqual(((IValueTypeVariableFacade<?>) facade).getValue(), value);
        helper.succeed();
    }

    /**
     * A list that reads an inventory at a position must not be shareable,
     * as it would otherwise allow reading inventories anywhere.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testRejectPositionedList(GameTestHelper helper) {
        ValueDeseralizationContext context = ValueDeseralizationContext.of(helper.getLevel());
        IValue value = ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyPositionedInventory(
                DimPos.of(helper.getLevel(), helper.absolutePos(POS)), Direction.UP));

        String clipboard = VariableClipboardHelpers.serialize(context, value);
        try {
            VariableClipboardHelpers.deserialize(context, clipboard, MAX_LENGTH);
            helper.fail("A positioned list was accepted from the clipboard");
        } catch (VariableClipboardHelpers.VariableClipboardException e) {
            // Expected
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testClipboardElementRequiresValue(GameTestHelper helper) {
        ClipboardLPElement element = new ClipboardLPElement();
        helper.assertFalse(element.canWriteElementPre(), "An empty clipboard element can be written");
        helper.assertTrue(element.validate() != null, "An empty clipboard element has no error");

        element.setValue(ValueTypeInteger.ValueInteger.of(1));
        helper.assertTrue(element.canWriteElementPre(), "A filled clipboard element can not be written");
        helper.assertTrue(element.validate() == null, "A filled clipboard element has an error");

        element.deactivate();
        helper.assertFalse(element.canWriteElementPre(), "A deactivated clipboard element can be written");
        helper.succeed();
    }

}
