package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionParameter;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionOperator;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTrees;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.item.ExpressionVariableFacade;
import org.cyclops.integrateddynamics.core.item.OperatorVariableFacade;
import org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElement;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.PartTypePanelDisplay;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.List;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.*;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsExpressions {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    private static ExpressionNode op(IOperator operator, ExpressionNode... children) {
        return new ExpressionNode.Operator(operator, List.of(children));
    }

    private static ExpressionNode c(int value) {
        return new ExpressionNode.Const(ValueTypeInteger.ValueInteger.of(value));
    }

    private static ExpressionNode ref(ItemStack variable, GameTestHelper helper) {
        return new ExpressionNode.Ref(getVariableFacade(helper.getLevel(), variable).getId());
    }

    /**
     * Places a cable with a redstone reader that reads 15 from a redstone block,
     * a variable store north of it, and a display panel on a second cable east of it.
     * @return The redstone reader variable, already inserted in the variable store.
     */
    private static ItemStack setupRedstoneNetwork(GameTestHelper helper) {
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST, PartTypes.DISPLAY_PANEL, new ItemStack(PartTypes.DISPLAY_PANEL.getItem()));
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_VARIABLE_STORE.get());
        helper.setBlock(POS.south(), Blocks.REDSTONE_BLOCK);
        ItemStack variableRedstone = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH), Aspects.Read.Redstone.INTEGER_VALUE);
        BlockEntityVariablestore variableStore = helper.getBlockEntity(POS.north());
        variableStore.getInventory().setItem(0, variableRedstone);
        return variableRedstone;
    }

    private static Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> display(GameTestHelper helper, ItemStack variable) {
        return placeVariableInDisplayPanel(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST), variable);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testExpressionRefAndConstantsInDisplayPanel(GameTestHelper helper) {
        ItemStack variableRedstone = setupRedstoneNetwork(helper);

        // (#redstone + 3) * 2, as a single card without intermediate cards
        ItemStack variableExpression = createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.ARITHMETIC_MULTIPLICATION, op(Operators.ARITHMETIC_ADDITION, ref(variableRedstone, helper), c(3)), c(2))));
        helper.assertTrue(getVariableFacade(helper.getLevel(), variableExpression) instanceof ExpressionVariableFacade, "Not an expression variable");

        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = display(helper, variableExpression);

        helper.succeedWhen(() -> assertValueEqual(partAndState.getRight().getDisplayValue(), ValueTypeInteger.ValueInteger.of(36)));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testExpressionEquivalentToCardChain(GameTestHelper helper) {
        ItemStack variableRedstone = setupRedstoneNetwork(helper);
        BlockEntityVariablestore variableStore = helper.getBlockEntity(POS.north());

        // Chain of classic cards: constant 3, then #redstone + #constant
        ItemStack variableConstant = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(3));
        variableStore.getInventory().setItem(1, variableConstant);
        ItemStack variableChain = createVariableForOperator(helper.getLevel(), Operators.ARITHMETIC_ADDITION, new int[]{
                getVariableFacade(helper.getLevel(), variableRedstone).getId(),
                getVariableFacade(helper.getLevel(), variableConstant).getId()
        });
        variableStore.getInventory().setItem(2, variableChain);

        // Single expression card
        ItemStack variableExpression = createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.ARITHMETIC_ADDITION, ref(variableRedstone, helper), c(3))));

        // Compare both: #chain == expression
        variableStore.getInventory().setItem(3, variableExpression);
        ItemStack variableEquals = createVariableForOperator(helper.getLevel(), Operators.RELATIONAL_EQUALS, new int[]{
                getVariableFacade(helper.getLevel(), variableChain).getId(),
                getVariableFacade(helper.getLevel(), variableExpression).getId()
        });
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = display(helper, variableEquals);

        helper.succeedWhen(() -> assertValueEqual(partAndState.getRight().getDisplayValue(), ValueTypeBoolean.ValueBoolean.of(true)));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testExpressionOperatorFilterInventory(GameTestHelper helper) {
        // Chest with mixed items
        helper.setBlock(POS.west(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(POS.west());
        chest.setItem(0, new ItemStack(Items.DIAMOND));
        ItemStack named = new ItemStack(Items.STICK);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("named"));
        chest.setItem(1, named);
        ItemStack namedLore = new ItemStack(Items.STICK);
        namedLore.set(DataComponents.CUSTOM_NAME, Component.literal("named"));
        namedLore.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("lore"))));
        chest.setItem(2, namedLore);

        // Network with inventory reader, variable store and display panel
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.INVENTORY_READER, new ItemStack(PartTypes.INVENTORY_READER.getItem()));
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST, PartTypes.DISPLAY_PANEL, new ItemStack(PartTypes.DISPLAY_PANEL.getItem()));
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_VARIABLE_STORE.get());
        BlockEntityVariablestore variableStore = helper.getBlockEntity(POS.north());
        ItemStack variableItems = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Inventory.LIST_ITEMSTACKS);
        variableStore.getInventory().setItem(0, variableItems);

        // item -> size(nbt(item)) > 1
        ItemStack variablePredicate = createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.RELATIONAL_GT, op(Operators.NBT_COMPOUND_SIZE, op(Operators.OBJECT_ITEMSTACK_DATA, new ExpressionNode.Param(0))), c(1)),
                List.of(new ExpressionParameter("item", ValueTypes.OBJECT_ITEMSTACK))));
        variableStore.getInventory().setItem(1, variablePredicate);

        // Classic filter card with the expression operator as predicate
        ItemStack variableFiltered = createVariableForOperator(helper.getLevel(), Operators.OPERATOR_FILTER, new int[]{
                getVariableFacade(helper.getLevel(), variablePredicate).getId(),
                getVariableFacade(helper.getLevel(), variableItems).getId()
        });
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = display(helper, variableFiltered);

        helper.succeedWhen(() -> {
            IValue value = partAndState.getRight().getDisplayValue();
            helper.assertTrue(value instanceof ValueTypeList.ValueList, "Display value is not a list: " + value);
            try {
                List<IValue> items = new java.util.ArrayList<>();
                for (Object item : ((ValueTypeList.ValueList) value).getRawValue()) {
                    items.add((IValue) item);
                }
                helper.assertTrue(items.size() == 1, "Expected 1 filtered item, got " + items);
                helper.assertTrue(ItemStack.matches(((ValueObjectTypeItemStack.ValueItemStack) items.get(0)).getRawValue(), namedLore),
                        "Unexpected filtered item " + items.get(0));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testExpressionOperatorWithRefInApply(GameTestHelper helper) {
        ItemStack variableRedstone = setupRedstoneNetwork(helper);
        BlockEntityVariablestore variableStore = helper.getBlockEntity(POS.north());

        // x -> x + #redstone
        ItemStack variableOperator = createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.ARITHMETIC_ADDITION, new ExpressionNode.Param(0), ref(variableRedstone, helper)),
                List.of(new ExpressionParameter("x", ValueTypes.INTEGER))));
        variableStore.getInventory().setItem(1, variableOperator);

        // apply(op, 10), nested in a parameterless expression
        ItemStack variableApplied = createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.OPERATOR_APPLY, ref(variableOperator, helper), c(10))));
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = display(helper, variableApplied);

        helper.succeedWhen(() -> assertValueEqual(partAndState.getRight().getDisplayValue(), ValueTypeInteger.ValueInteger.of(25)));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testExpressionOperatorDisplayed(GameTestHelper helper) {
        setupRedstoneNetwork(helper);

        // An expression operator on a display panel is materialized and serialized to clients
        ItemStack variableOperator = createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.ARITHMETIC_ADDITION, new ExpressionNode.Param(0), c(1)),
                List.of(new ExpressionParameter("x", ValueTypes.INTEGER))));
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = display(helper, variableOperator);

        helper.succeedWhen(() -> {
            IValue value = partAndState.getRight().getDisplayValue();
            helper.assertTrue(value instanceof ValueTypeOperator.ValueOperator, "Not an operator: " + value);
            IOperator operator = ((ValueTypeOperator.ValueOperator) value).getRawValue();
            helper.assertTrue(operator instanceof ExpressionOperator, "Not an expression operator: " + operator);
            try {
                assertValueEqual(ValueHelpers.evaluateOperator(operator, ValueTypeInteger.ValueInteger.of(4)), ValueTypeInteger.ValueInteger.of(5));
                Tag tag = ValueHelpers.serializeRaw(org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext.of(helper.getLevel()), value);
                IValue read = ValueHelpers.deserializeRaw(org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext.of(helper.getLevel()), ValueTypes.OPERATOR, tag);
                assertValueEqual(ValueHelpers.evaluateOperator(((ValueTypeOperator.ValueOperator) read).getRawValue(), ValueTypeInteger.ValueInteger.of(4)), ValueTypeInteger.ValueInteger.of(5));
            } catch (EvaluationException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testExpressionPersistence(GameTestHelper helper) {
        ItemStack variableRedstone = setupRedstoneNetwork(helper);

        ItemStack variableExpression = createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.ARITHMETIC_ADDITION, ref(variableRedstone, helper), c(3))));

        // Save and load the item, as happens on world reload
        Tag saved = variableExpression.save(helper.getLevel().registryAccess());
        ItemStack loaded = ItemStack.parse(helper.getLevel().registryAccess(), saved).orElseThrow();
        IVariableFacade facadeOriginal = getVariableFacade(helper.getLevel(), variableExpression);
        IVariableFacade facadeLoaded = getVariableFacade(helper.getLevel(), loaded);
        helper.assertTrue(facadeLoaded instanceof ExpressionVariableFacade, "Loaded variable is not an expression variable");
        helper.assertTrue(facadeOriginal.getId() == facadeLoaded.getId(), "Ids differ");
        helper.assertTrue(((ExpressionVariableFacade) facadeOriginal).getExpression().equals(((ExpressionVariableFacade) facadeLoaded).getExpression()),
                "Expressions differ");

        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = display(helper, loaded);
        helper.succeedWhen(() -> assertValueEqual(partAndState.getRight().getDisplayValue(), ValueTypeInteger.ValueInteger.of(18)));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testExpressionMissingReference(GameTestHelper helper) {
        setupRedstoneNetwork(helper);

        // References a variable that is not in the network
        ItemStack variableExpression = createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.ARITHMETIC_ADDITION, new ExpressionNode.Ref(123456), c(3))));
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = display(helper, variableExpression);

        helper.succeedWhen(() -> {
            helper.assertTrue(partAndState.getRight().getDisplayValue() == null, "Display value should be empty");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testLogicProgrammerCompatibility(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack variableA = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(1));
        ItemStack variableB = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(2));

        // A plain operator with two cards is written as a classic operator variable
        OperatorLPElement element = new OperatorLPElement(Operators.ARITHMETIC_ADDITION);
        element.activate();
        element.onInputSlotUpdated(player, 0, variableA);
        element.onInputSlotUpdated(player, 1, variableB);
        helper.assertTrue(element.canWriteElementPre(), "Element can not be written");
        helper.assertTrue(element.validate() == null, "Element has an error: " + element.validate());
        ItemStack written = element.writeElement(player, new ItemStack(RegistryEntries.ITEM_VARIABLE.get()));
        IVariableFacade writtenFacade = getVariableFacade(helper.getLevel(), written);
        helper.assertTrue(writtenFacade instanceof OperatorVariableFacade, "Plain operator was not written as an operator variable");

        // Replacing a card with an inline constant writes an expression variable
        element.getEditor().setChild(1, new ExpressionNode.Const(ValueTypeInteger.ValueInteger.of(5)));
        element.onInputSlotUpdated(player, 1, ItemStack.EMPTY);
        helper.assertTrue(element.canWriteElementPre(), "Element with constant can not be written");
        ItemStack writtenExpression = element.writeElement(player, new ItemStack(RegistryEntries.ITEM_VARIABLE.get()));
        IVariableFacade writtenExpressionFacade = getVariableFacade(helper.getLevel(), writtenExpression);
        helper.assertTrue(writtenExpressionFacade instanceof ExpressionVariableFacade, "Expression was not written as an expression variable");

        // Loading an old operator card yields a tree
        OperatorLPElement element2 = new OperatorLPElement(Operators.ARITHMETIC_ADDITION);
        element2.activate();
        helper.assertTrue(element2.isFor(writtenFacade), "Element is not for the operator variable");
        element2.loadElement(writtenFacade);
        helper.assertTrue(ExpressionTrees.isPlainOperator(element2.getEditor().getExpression()), "Loaded operator is not a plain tree");
        helper.assertTrue(element2.canWriteElementPre(), "Loaded element can not be written");

        // Loading an expression card yields the same tree
        OperatorLPElement element3 = new OperatorLPElement(Operators.ARITHMETIC_ADDITION);
        element3.activate();
        helper.assertTrue(element3.isFor(writtenExpressionFacade), "Element is not for the expression variable");
        element3.loadElement(writtenExpressionFacade);
        helper.assertTrue(element3.getEditor().getExpression().equals(((ExpressionVariableFacade) writtenExpressionFacade).getExpression()),
                "Loaded expression differs");

        // Other operators do not claim the card
        helper.assertTrue(!new OperatorLPElement(Operators.ARITHMETIC_SUBTRACTION).isFor(writtenExpressionFacade), "Wrong element claims the card");

        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testLogicProgrammerTypeError(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack variableString = createVariableForValue(helper.getLevel(), ValueTypes.STRING,
                org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString.ValueString.of("abc"));

        OperatorLPElement element = new OperatorLPElement(Operators.ARITHMETIC_ADDITION);
        element.activate();
        element.getEditor().setChild(0, new ExpressionNode.Const(ValueTypeInteger.ValueInteger.of(1)));
        element.onInputSlotUpdated(player, 1, variableString);
        helper.assertTrue(element.canWriteElementPre(), "Element can not be written");
        helper.assertTrue(element.validate() != null, "Element should have a type error");
        helper.assertTrue(element.getEditor().check().getError(new int[0], 1) != null, "Error is not on slot 2");

        helper.succeed();
    }

}
