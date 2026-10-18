package org.cyclops.integrateddynamics.gametest;

import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionParameter;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.core.evaluate.ExpressionVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionOperator;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionParseException;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionParser;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTrees;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.item.ExpressionVariableFacade;
import org.cyclops.integrateddynamics.core.item.OperatorVariableFacade;
import org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionEditor;
import org.cyclops.integrateddynamics.core.logicprogrammer.LogicProgrammerElementTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElement;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammer;
import org.cyclops.integrateddynamics.item.ItemWrench;
import org.cyclops.integrateddynamics.part.PartTypePanelDisplay;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
                List<IValue> items = new ArrayList<>();
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
                Tag tag = ValueHelpers.serializeRaw(ValueDeseralizationContext.of(helper.getLevel()), value);
                IValue read = ValueHelpers.deserializeRaw(ValueDeseralizationContext.of(helper.getLevel()), ValueTypes.OPERATOR, tag);
                assertValueEqual(ValueHelpers.evaluateOperator(((ValueTypeOperator.ValueOperator) read).getRawValue(), ValueTypeInteger.ValueInteger.of(4)), ValueTypeInteger.ValueInteger.of(5));
            } catch (EvaluationException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static ExpressionNode p(int index) {
        return new ExpressionNode.Param(index);
    }

    private static ItemStack store(GameTestHelper helper, int slot, ItemStack variable) {
        BlockEntityVariablestore variableStore = helper.getBlockEntity(POS.north());
        variableStore.getInventory().setItem(slot, variable);
        return variable;
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testExpressionComposedFromExpressionCards(GameTestHelper helper) {
        setupRedstoneNetwork(helper);

        // #add = (a, b) -> a + b, #double = x -> x * 2
        ItemStack variableAdd = store(helper, 1, createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.ARITHMETIC_ADDITION, p(0), p(1)),
                List.of(new ExpressionParameter("a", ValueTypes.INTEGER), new ExpressionParameter("b", ValueTypes.INTEGER)))));
        ItemStack variableDouble = store(helper, 2, createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.ARITHMETIC_MULTIPLICATION, p(0), c(2)),
                List.of(new ExpressionParameter("x", ValueTypes.INTEGER)))));
        ItemStack variableList = store(helper, 3, createVariableForValue(helper.getLevel(), ValueTypes.LIST,
                ValueTypeList.ValueList.ofAll(ValueTypeInteger.ValueInteger.of(1), ValueTypeInteger.ValueInteger.of(2), ValueTypeInteger.ValueInteger.of(3))));
        // #sumDoubled = l -> reduce(#add, map(#double, l), 0)
        ItemStack variableSumDoubled = store(helper, 4, createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.OPERATOR_REDUCE, ref(variableAdd, helper), op(Operators.OPERATOR_MAP, ref(variableDouble, helper), p(0)), c(0)),
                List.of(new ExpressionParameter("l", ValueTypes.LIST)))));
        // apply(#sumDoubled, #list)
        ItemStack variableResult = createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.OPERATOR_APPLY, ref(variableSumDoubled, helper), ref(variableList, helper))));
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = display(helper, variableResult);

        helper.succeedWhen(() -> assertValueEqual(partAndState.getRight().getDisplayValue(), ValueTypeInteger.ValueInteger.of(12)));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testExpressionThreeParametersPartiallyApplied(GameTestHelper helper) {
        ItemStack variableRedstone = setupRedstoneNetwork(helper);

        // #op = (a, b, c) -> a > b ? a * c : b - c
        ItemStack variableOperator = store(helper, 1, createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.GENERAL_CHOICE, op(Operators.RELATIONAL_GT, p(0), p(1)),
                        op(Operators.ARITHMETIC_MULTIPLICATION, p(0), p(2)), op(Operators.ARITHMETIC_SUBTRACTION, p(1), p(2))),
                List.of(new ExpressionParameter("a", ValueTypes.INTEGER), new ExpressionParameter("b", ValueTypes.INTEGER),
                        new ExpressionParameter("c", ValueTypes.INTEGER)))));
        // apply(apply_2(#op, #redstone, 2), 10) = 15 * 10
        ItemStack variableResult = createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.OPERATOR_APPLY, op(Operators.OPERATOR_APPLY_2, ref(variableOperator, helper), ref(variableRedstone, helper), c(2)), c(10))));
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = display(helper, variableResult);

        helper.succeedWhen(() -> assertValueEqual(partAndState.getRight().getDisplayValue(), ValueTypeInteger.ValueInteger.of(150)));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testExpressionUpdatesWhenReferenceChanges(GameTestHelper helper) {
        ItemStack variableRedstone = setupRedstoneNetwork(helper);

        // #op = x -> x + #redstone, displayed through apply(#op, 100) in a nested expression
        ItemStack variableOperator = store(helper, 1, createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.ARITHMETIC_ADDITION, p(0), ref(variableRedstone, helper)),
                List.of(new ExpressionParameter("x", ValueTypes.INTEGER)))));
        ItemStack variableResult = createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.ARITHMETIC_MULTIPLICATION, op(Operators.OPERATOR_APPLY, ref(variableOperator, helper), c(100)), c(2))));
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = display(helper, variableResult);

        // First 15 from the redstone block, then 0 once it is removed
        helper.startSequence()
                .thenWaitUntil(() -> assertValueEqual(partAndState.getRight().getDisplayValue(), ValueTypeInteger.ValueInteger.of(230)))
                .thenExecute(() -> helper.setBlock(POS.south(), Blocks.AIR))
                .thenWaitUntil(() -> assertValueEqual(partAndState.getRight().getDisplayValue(), ValueTypeInteger.ValueInteger.of(200)))
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testExpressionReferenceCycle(GameTestHelper helper) {
        setupRedstoneNetwork(helper);

        // #a = #b + 1 and #b = #a + 1, written with fixed ids so they can refer to each other
        ItemStack variableA = createVariableForExpression(helper.getLevel(), new Expression(op(Operators.ARITHMETIC_ADDITION, c(0), c(1))));
        ItemStack variableB = createVariableForExpression(helper.getLevel(), new Expression(op(Operators.ARITHMETIC_ADDITION, c(0), c(1))));
        int idA = getVariableFacade(helper.getLevel(), variableA).getId();
        int idB = getVariableFacade(helper.getLevel(), variableB).getId();
        variableA = rewriteExpression(helper, variableA, new Expression(op(Operators.ARITHMETIC_ADDITION, new ExpressionNode.Ref(idB), c(1))));
        variableB = rewriteExpression(helper, variableB, new Expression(op(Operators.ARITHMETIC_ADDITION, new ExpressionNode.Ref(idA), c(1))));
        store(helper, 1, variableA);
        store(helper, 2, variableB);
        // A self-reference as well
        ItemStack variableSelf = createVariableForExpression(helper.getLevel(), new Expression(op(Operators.ARITHMETIC_ADDITION, c(0), c(1))));
        int idSelf = getVariableFacade(helper.getLevel(), variableSelf).getId();
        store(helper, 3, rewriteExpression(helper, variableSelf, new Expression(op(Operators.ARITHMETIC_ADDITION, new ExpressionNode.Ref(idSelf), c(1)))));

        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = display(helper, variableA.copy());

        helper.runAfterDelay(5, () -> {
            // Nothing is displayed and nothing crashed
            helper.assertTrue(partAndState.getRight().getDisplayValue() == null, "Display value should be empty");
            IVariableFacade facadeSelf = getVariableFacade(helper.getLevel(), helper.<BlockEntityVariablestore>getBlockEntity(POS.north()).getInventory().getItem(3));
            List<MutableComponent> errors = new ArrayList<>();
            INetwork network = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(Optional.of(network)).orElseThrow();
            facadeSelf.validate(network, partNetwork, errors::add, ValueTypes.INTEGER);
            helper.assertTrue(!errors.isEmpty(), "A self-reference should be a validation error");
            IVariableFacade facadeA = partNetwork.getVariableFacade(idA);
            errors.clear();
            facadeA.validate(network, partNetwork, errors::add, ValueTypes.INTEGER);
            helper.assertTrue(!errors.isEmpty(), "A reference cycle should be a validation error");
            helper.assertTrue(facadeA.getVariable(network, partNetwork) == null, "A reference cycle should not produce a variable");
            helper.succeed();
        });
    }

    private static ItemStack rewriteExpression(GameTestHelper helper, ItemStack variable, Expression expression) {
        int id = getVariableFacade(helper.getLevel(), variable).getId();
        CompoundTag tag = new CompoundTag();
        IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class)
                .write(ValueDeseralizationContext.of(helper.getLevel()), tag,
                        new ExpressionVariableFacade(id, expression), ExpressionVariableFacadeHandler.getInstance());
        ItemStack copy = variable.copy();
        copy.set(RegistryEntries.DATACOMPONENT_VARIABLE_FACADE, tag);
        return copy;
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
    public void testLogicProgrammerLoadKeepsReferenceTypes(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack variableString = createVariableForValue(helper.getLevel(), ValueTypes.STRING,
                ValueTypeString.ValueString.of("abc"));
        int idString = getVariableFacade(helper.getLevel(), variableString).getId();

        // == with a String card and a constant: the == inputs are Any, so the type is only known from the card itself
        OperatorLPElement element = new OperatorLPElement(Operators.RELATIONAL_EQUALS);
        element.activate();
        element.onInputSlotUpdated(player, 0, variableString);
        element.getEditor().setChild(1, new ExpressionNode.Const(ValueTypeInteger.ValueInteger.of(3)));
        ItemStack written = element.writeElement(player, new ItemStack(RegistryEntries.ITEM_VARIABLE.get()));
        ExpressionVariableFacade writtenFacade = (ExpressionVariableFacade) getVariableFacade(helper.getLevel(), written);
        helper.assertTrue(writtenFacade.getReferenceTypes().get(idString) == ValueTypes.STRING, "Reference type was not stored");

        // Loading restores the type, so using the reference where a number is needed is an error in the editor
        OperatorLPElement loaded = new OperatorLPElement(Operators.RELATIONAL_EQUALS);
        loaded.activate();
        loaded.loadElement(writtenFacade);
        helper.assertTrue(loaded.getEditor().getRefType(idString) == ValueTypes.STRING, "Reference type was not loaded");
        loaded.getEditor().setChild(1, new ExpressionNode.Operator(Operators.ARITHMETIC_INCREMENT, List.of(new ExpressionNode.Ref(idString))));
        helper.assertTrue(loaded.validate() != null, "The loaded String reference should not fit a number input");

        // An old operator card: the types of references in inputs that are not categories are inferred
        ItemStack variableOld = createVariableForOperator(helper.getLevel(), Operators.STRING_LENGTH, new int[]{idString});
        OperatorLPElement loadedOld = new OperatorLPElement(Operators.STRING_LENGTH);
        loadedOld.activate();
        loadedOld.loadElement(getVariableFacade(helper.getLevel(), variableOld));
        helper.assertTrue(loadedOld.getEditor().getRefType(idString) == ValueTypes.STRING, "Reference type was not inferred");

        // An old card with Any inputs: the type is unknown until the card is found in the player's inventory
        ItemStack variableOldAny = createVariableForOperator(helper.getLevel(), Operators.RELATIONAL_EQUALS, new int[]{idString, idString});
        OperatorLPElement loadedOldAny = new OperatorLPElement(Operators.RELATIONAL_EQUALS);
        loadedOldAny.activate();
        loadedOldAny.loadElement(getVariableFacade(helper.getLevel(), variableOldAny));
        helper.assertTrue(loadedOldAny.getEditor().getRefType(idString) == null, "Reference type should be unknown");
        player.getInventory().add(variableString.copy());
        loadedOldAny.findReferencedCards(player);
        helper.assertTrue(loadedOldAny.getEditor().getRefType(idString) == ValueTypes.STRING, "Reference type was not found in the inventory");

        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testLogicProgrammerTypeError(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack variableString = createVariableForValue(helper.getLevel(), ValueTypes.STRING,
                ValueTypeString.ValueString.of("abc"));

        OperatorLPElement element = new OperatorLPElement(Operators.ARITHMETIC_ADDITION);
        element.activate();
        element.getEditor().setChild(0, new ExpressionNode.Const(ValueTypeInteger.ValueInteger.of(1)));
        element.onInputSlotUpdated(player, 1, variableString);
        helper.assertTrue(element.canWriteElementPre(), "Element can not be written");
        helper.assertTrue(element.validate() != null, "Element should have a type error");
        helper.assertTrue(element.getEditor().check().getError(new int[0], 1) != null, "Error is not on slot 2");

        helper.succeed();
    }

    // --------------- Copying ---------------

    // (#redstone + 3) * 2, with the type of #redstone stored on the card
    private static ItemStack createRedstoneExpression(GameTestHelper helper, ItemStack variableRedstone) {
        return createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.ARITHMETIC_MULTIPLICATION, op(Operators.ARITHMETIC_ADDITION, ref(variableRedstone, helper), c(3)), c(2))),
                Map.of(getVariableFacade(helper.getLevel(), variableRedstone).getId(), ValueTypes.INTEGER));
    }

    private static void assertCopiedExpression(GameTestHelper helper, ItemStack original, ItemStack copy) {
        ExpressionVariableFacade originalFacade = (ExpressionVariableFacade) getVariableFacade(helper.getLevel(), original);
        IVariableFacade copyFacade = getVariableFacade(helper.getLevel(), copy);
        helper.assertTrue(copyFacade instanceof ExpressionVariableFacade, "The copy is not an expression card");
        ExpressionVariableFacade copyExpressionFacade = (ExpressionVariableFacade) copyFacade;
        helper.assertTrue(copyFacade.getId() != originalFacade.getId(), "The copy has the same id");
        helper.assertTrue(originalFacade.getExpression().equals(copyExpressionFacade.getExpression()), "The copy has another expression");
        helper.assertTrue(originalFacade.getReferenceTypes().equals(copyExpressionFacade.getReferenceTypes()),
                "The copy has other reference types");
    }

    private static ItemStack craftCopy(GameTestHelper helper, ItemStack original) {
        int originalId = getVariableFacade(helper.getLevel(), original).getId();
        List<ItemStack> cards = new GameTestsVariableCopy().craftVariableCopy(helper, original.copy(),
                new ItemStack(RegistryEntries.ITEM_VARIABLE.get()), ClickType.PICKUP);
        helper.assertValueEqual(cards.size(), 2, "Number of variable cards after crafting");
        return cards.stream()
                .filter(card -> getVariableFacade(helper.getLevel(), card).getId() != originalId)
                .findFirst()
                .orElseThrow(() -> new GameTestAssertException("No card with a new id after crafting"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testExpressionCopyInCraftingGrid(GameTestHelper helper) {
        ItemStack variableRedstone = setupRedstoneNetwork(helper);
        ItemStack original = createRedstoneExpression(helper, variableRedstone);

        ItemStack copy = craftCopy(helper, original);
        assertCopiedExpression(helper, original, copy);

        // The copy evaluates like the original
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = display(helper, copy.copy());
        helper.succeedWhen(() -> assertValueEqual(partAndState.getRight().getDisplayValue(), ValueTypeInteger.ValueInteger.of(36)));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testExpressionWithParametersCopyInCraftingGrid(GameTestHelper helper) {
        ItemStack variableRedstone = setupRedstoneNetwork(helper);
        // x -> x * 2
        ItemStack original = createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.ARITHMETIC_MULTIPLICATION, p(0), c(2)),
                List.of(new ExpressionParameter("x", ValueTypes.INTEGER))));

        ItemStack copy = store(helper, 1, craftCopy(helper, original));
        assertCopiedExpression(helper, original, copy);

        // apply(#copy, #redstone)
        ItemStack variableResult = createVariableForExpression(helper.getLevel(), new Expression(
                op(Operators.OPERATOR_APPLY, ref(copy, helper), ref(variableRedstone, helper))));
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = display(helper, variableResult);
        helper.succeedWhen(() -> assertValueEqual(partAndState.getRight().getDisplayValue(), ValueTypeInteger.ValueInteger.of(30)));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testExpressionCopyWithWrench(GameTestHelper helper) {
        ItemStack variableRedstone = setupRedstoneNetwork(helper);
        // A second display panel in the same network
        helper.setBlock(POS.east().south(), RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east().south()), Direction.EAST, PartTypes.DISPLAY_PANEL,
                new ItemStack(PartTypes.DISPLAY_PANEL.getItem()));
        PartPos source = PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST);
        PartPos target = PartPos.of(helper.getLevel(), helper.absolutePos(POS.east().south()), Direction.EAST);
        ItemStack original = createRedstoneExpression(helper, variableRedstone);
        display(helper, original.copy());

        // Copy the source panel and paste it onto the target panel
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wrench = GameTestsWrenchConfig.createWrench(ItemWrench.Mode.CONFIG);
        // The wrench occupies the selected hotbar slot, so it must be given before the blank variable card
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        GameTestsWrenchConfig.giveBlankVariables(player, 1);
        GameTestsWrenchConfig.clickPart(helper, player, wrench, source, true);
        GameTestsWrenchConfig.clickPart(helper, player, wrench, target, false);

        helper.succeedWhen(() -> {
            ItemStack pasted = GameTestsWrenchConfig.getActiveVariable(target);
            helper.assertTrue(pasted != null, "The expression card was not pasted");
            assertCopiedExpression(helper, original, pasted);
            helper.assertValueEqual(GameTestsWrenchConfig.countBlankVariables(player), 0, "The blank variable card was not consumed");
            assertValueEqual(((PartTypePanelDisplay.State) PartHelpers.getPart(target).getState()).getDisplayValue(),
                    ValueTypeInteger.ValueInteger.of(36));
        });
    }

    // --------------- Text ---------------

    @GameTest(template = TEMPLATE_EMPTY)
    public void testLogicProgrammerSetExpressionAsText(GameTestHelper helper) throws ExpressionParseException {
        ItemStack variableRedstone = setupRedstoneNetwork(helper);
        int redstoneId = getVariableFacade(helper.getLevel(), variableRedstone).getId();
        ValueDeseralizationContext values = ValueDeseralizationContext.of(helper.getLevel());
        ExpressionParser.Context context = ExpressionEditor.getParserContext(values, id -> null, label -> IntLists.EMPTY_LIST);

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack written;
        try {
            ContainerLogicProgrammer container = new ContainerLogicProgrammer(0, player.getInventory());
            container.setActiveElement(container.getOperatorElement(Operators.RELATIONAL_GT), 0, 0);

            // Typed with another root operator, so the element of that operator becomes active
            Expression expression = ExpressionParser.parse("(#" + redstoneId + " + 3) * 2", context);
            container.applyExpressionAction(ExpressionEditor.actionSet(values, expression));
            helper.assertTrue(container.getActiveElement() instanceof OperatorLPElement element
                    && element.getOperator() == Operators.ARITHMETIC_MULTIPLICATION, "The element of the new root operator is not active");
            OperatorLPElement element = (OperatorLPElement) container.getActiveElement();
            helper.assertTrue(element.getEditor().getExpression().equals(expression), "The expression was not set");

            // Something that can not be edited is ignored
            container.applyExpressionAction(ExpressionEditor.actionSet(values, new Expression(c(1))));
            helper.assertTrue(container.getActiveElement() == element && element.getEditor().getExpression().equals(expression),
                    "An expression that can not be edited was set");

            written = element.writeElement(player, new ItemStack(RegistryEntries.ITEM_VARIABLE.get()));
        } finally {
            player.getServer().getPlayerList().remove(player);
        }

        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = display(helper, written);
        helper.succeedWhen(() -> assertValueEqual(partAndState.getRight().getDisplayValue(), ValueTypeInteger.ValueInteger.of(36)));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testLogicProgrammerSetExpressionWithoutOperator(GameTestHelper helper) throws ExpressionParseException {
        ValueDeseralizationContext values = ValueDeseralizationContext.of(helper.getLevel());
        ExpressionParser.Context context = ExpressionEditor.getParserContext(values, id -> null, label -> IntLists.EMPTY_LIST);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            ContainerLogicProgrammer container = new ContainerLogicProgrammer(0, player.getInventory());

            // Typed in the empty panel, which makes the element of its root operator active
            Expression expression = ExpressionParser.parse("(x: integer) -> x * 2", context);
            container.applyExpressionAction(ExpressionEditor.actionSet(values, expression));
            helper.assertTrue(container.getActiveElement() instanceof OperatorLPElement element
                    && element.getOperator() == Operators.ARITHMETIC_MULTIPLICATION
                    && element.getEditor().getExpression().equals(expression), "The expression was not set without an active element");

            // Also while another kind of element is active
            ILogicProgrammerElement other = LogicProgrammerElementTypes.VALUETYPE.getByName(ValueTypes.INTEGER.getUniqueName());
            helper.assertTrue(other != null, "The integer element does not exist");
            container.setActiveElement(other, 0, 0);
            Expression addition = ExpressionParser.parse("1 + 2", context);
            container.applyExpressionAction(ExpressionEditor.actionSet(values, addition));
            helper.assertTrue(container.getActiveElement() instanceof OperatorLPElement element
                    && element.getOperator() == Operators.ARITHMETIC_ADDITION
                    && element.getEditor().getExpression().equals(addition), "The expression was not set while a value element was active");
        } finally {
            player.getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testLogicProgrammerSetExpressionKeepsOpenOperator(GameTestHelper helper) throws ExpressionParseException {
        ValueDeseralizationContext values = ValueDeseralizationContext.of(helper.getLevel());
        ExpressionParser.Context context = ExpressionEditor.getParserContext(values, id -> null, label -> IntLists.EMPTY_LIST);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            ContainerLogicProgrammer container = new ContainerLogicProgrammer(0, player.getInventory());
            OperatorLPElement element = container.getOperatorElement(Operators.ARITHMETIC_MULTIPLICATION);
            container.setActiveElement(element, 0, 0);
            container.applyExpressionAction(ExpressionEditor.actionSet(values, ExpressionParser.parse("(1 + 2) * 3", context)));
            container.applyExpressionAction(ExpressionEditor.actionNavigate(new int[]{0}));
            helper.assertTrue(element.getEditor().getEditPath().length == 1, "The nested operator was not opened");

            // Typing a change inside the open operator keeps it open
            container.applyExpressionAction(ExpressionEditor.actionSet(values, ExpressionParser.parse("(1 + 5) * 3", context)));
            helper.assertTrue(container.getActiveElement() == element && element.getEditor().getEditPath().length == 1
                    && element.getEditor().getEditPath()[0] == 0, "The open operator was closed");

            // When it is gone, the root is opened
            container.applyExpressionAction(ExpressionEditor.actionSet(values, ExpressionParser.parse("4 * 3", context)));
            helper.assertTrue(element.getEditor().getEditPath().length == 0, "The root was not opened");
        } finally {
            player.getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

}
