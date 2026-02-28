package org.cyclops.integrateddynamics.gametest;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.ILazyExpressionValueCache;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariableInvalidateListener;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.core.evaluate.expression.LazyExpression;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.logicprogrammer.event.LogicProgrammerVariableFacadeCreatedEvent;
import org.cyclops.integrateddynamics.core.network.event.NetworkInitializedEvent;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.core.part.event.PartReaderAspectEvent;
import org.cyclops.integrateddynamics.core.part.event.PartVariableDrivenVariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammer;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.*;

/**
 * Game tests for all advancements in IntegratedDynamics.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsAdvancements {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    // ---- Helpers ----

    private static void assertAdvancement(GameTestHelper helper, ServerPlayer player, String id) {
        AdvancementHolder advancement = helper.getLevel().getServer().getAdvancements().get(ResourceLocation.parse(id));
        if (advancement == null) {
            throw new GameTestAssertException("Advancement not found: " + id);
        }
        if (!player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            throw new GameTestAssertException("Advancement not obtained: " + id);
        }
    }

    private static void assertAdvancementNotDone(GameTestHelper helper, ServerPlayer player, String id) {
        AdvancementHolder advancement = helper.getLevel().getServer().getAdvancements().get(ResourceLocation.parse(id));
        if (advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            throw new GameTestAssertException("Advancement should NOT have been obtained: " + id);
        }
    }

    private static ILazyExpressionValueCache simpleCache() {
        return new ILazyExpressionValueCache() {
            private final Map<Integer, IValue> values = new HashMap<>();
            @Override public void setValue(int id, IValue value) { values.put(id, value); }
            @Override public boolean hasValue(int id) { return values.containsKey(id); }
            @Override public IValue getValue(int id) { return values.get(id); }
            @Override public void removeValue(int id) { values.remove(id); }
        };
    }

    @SuppressWarnings("unchecked")
    private static <V extends IValue> LazyExpression<V> makeOpVar(IOperator operator, IValueType<V> type, IVariable<?>... inputs) {
        ILazyExpressionValueCache cache = simpleCache();
        return new LazyExpression<V>(0, operator, inputs, cache) {
            @Override public IValueType<V> getType() { return type; }
            @Override public V getValue() throws EvaluationException { return type.getDefault(); }
        };
    }

    private static <V extends IValue> IAspectVariable<V> makeAspectVar(IAspectRead<V, ?> aspect) {
        return new IAspectVariable<V>() {
            @Override public PartTarget getTarget() { return null; }
            @Override public IAspectRead<V, ?> getAspect() { return aspect; }
            @Override public IValueType<V> getType() { return aspect.getValueType(); }
            @Override public V getValue() throws EvaluationException { return aspect.getValueType().getDefault(); }
            @Override public void invalidate() {}
            @Override public void addInvalidationListener(IVariableInvalidateListener l) {}
            @Override public void removeInvalidationListener(IVariableInvalidateListener l) {}
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void fireVariableDrivenEvent(ServerPlayer player, IVariable<?> variable) {
        NeoForge.EVENT_BUS.post(new PartVariableDrivenVariableContentsUpdatedEvent(
                null, null, null, PartTypes.DISPLAY_PANEL, null, player, variable, null));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void fireReaderAspectEvent(GameTestHelper helper, ServerPlayer player, BlockPos pos,
            org.cyclops.integrateddynamics.api.part.read.IPartTypeReader<?, ?> partType, IAspectRead<?, ?> aspect) {
        helper.setBlock(pos, RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(pos), Direction.WEST,
                partType, new ItemStack(partType.getItem()));
        PartPos partPos = PartPos.of(helper.getLevel(), helper.absolutePos(pos), Direction.WEST);
        PartHelpers.PartStateHolder<?, ?> holder = PartHelpers.getPart(partPos);
        INetwork network = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(pos), null);
        org.cyclops.integrateddynamics.api.network.IPartNetwork partNetwork = NetworkHelpers.getPartNetworkChecked(network);
        PartTarget target = PartTarget.fromCenter(partPos);
        NeoForge.EVENT_BUS.post(new PartReaderAspectEvent(
                network, partNetwork, target, partType,
                (org.cyclops.integrateddynamics.api.part.read.IPartStateReader) holder.getState(),
                player, aspect, ItemStack.EMPTY));
    }

    private static void placeVariableInWriterWithPlayer(Level level, PartPos partPos,
            IAspectWrite<?, ?> aspect, ItemStack variableCard, ServerPlayer player) {
        PartHelpers.PartStateHolder<?, ?> holder = PartHelpers.getPart(partPos);
        IPartTypeWriter<?, ?> part = (IPartTypeWriter<?, ?>) holder.getPart();
        IPartStateWriter<?> state = (IPartStateWriter<?>) holder.getState();

        int aspectIndex = -1;
        for (int i = 0; i < part.getWriteAspects().size(); i++) {
            if (part.getWriteAspects().get(i) == aspect) {
                aspectIndex = i;
                break;
            }
        }
        if (aspectIndex < 0) {
            throw new GameTestAssertException("Aspect " + aspect + " not found in writer");
        }

        state.getInventory().setItem(aspectIndex, variableCard);
        ((IPartTypeWriter) part).updateActivation(PartTarget.fromCenter(partPos), state, player);
    }

    // ---- Tests ----

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRoot(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack anyIDItem = new ItemStack(RegistryEntries.ITEM_VARIABLE.get());
        NeoForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(player, anyIDItem, null));
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:root"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementMeneglinDiscovery(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack menrilLog = new ItemStack(RegistryEntries.BLOCK_MENRIL_LOG.get());
        player.addItem(menrilLog);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:meneglin_basics/meneglin_discovery"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementMenrilProduction(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack crystalBlock = new ItemStack(RegistryEntries.BLOCK_CRYSTALIZED_MENRIL_BLOCK.get());
        player.addItem(crystalBlock);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:meneglin_basics/menril_production"));
    }

    private static void fireItemCraftedTrigger(ServerPlayer player, String itemId) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId)));
        NeoForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(player, stack, null));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSqueezing(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        fireItemCraftedTrigger(player, "integrateddynamics:squeezer");
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:meneglin_basics/squeezing"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementDrying(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        fireItemCraftedTrigger(player, "integrateddynamics:drying_basin");
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:meneglin_basics/drying"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementCablesLogic(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        fireItemCraftedTrigger(player, "integrateddynamics:cable");
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:network_foundations/cables_logic"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementMenrilWrenching(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        fireItemCraftedTrigger(player, "integrateddynamics:wrench");
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:network_foundations/menril_wrenching"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementVariables(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        fireItemCraftedTrigger(player, "integrateddynamics:variable");
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:network_foundations/variables"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementVariableInput(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        fireItemCraftedTrigger(player, "integrateddynamics:variable_transformer_input");
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:network_foundations/variable_input"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementVariableOutput(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        fireItemCraftedTrigger(player, "integrateddynamics:variable_transformer_output");
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:network_foundations/variable_output"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBlockReading(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        fireItemCraftedTrigger(player, "integrateddynamics:part_block_reader");
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:basic_network_components/block_reading"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementInventoryReading(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        fireItemCraftedTrigger(player, "integrateddynamics:part_inventory_reader");
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:basic_network_components/inventory_reading"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementMachineReading(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        fireItemCraftedTrigger(player, "integrateddynamics:part_machine_reader");
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:basic_network_components/machine_reading"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRedstoneReading(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        fireItemCraftedTrigger(player, "integrateddynamics:part_redstone_reader");
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:basic_network_components/redstone_reading"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementValueDisplaying(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        fireItemCraftedTrigger(player, "integrateddynamics:part_display_panel");
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:basic_network_components/value_displaying"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementLogicProgramming(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ContainerLogicProgrammer container = new ContainerLogicProgrammer(0, player.getInventory());
        NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, container));
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:logic_operations/logic_programming"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementNetworksLogic(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Place 10 connected cables
        for (int i = 0; i < 10; i++) {
            helper.setBlock(POS.east(i), RegistryEntries.BLOCK_CABLE.value());
        }
        INetwork network = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
        NeoForge.EVENT_BUS.post(new NetworkInitializedEvent(
                network, helper.getLevel(), helper.absolutePos(POS), player));
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:network_foundations/networks_logic"));
    }

    private static void fireVariableCreatedTrigger(ServerPlayer player, IVariableFacade facade,
            net.minecraft.world.level.block.Block block) {
        NeoForge.EVENT_BUS.post(new LogicProgrammerVariableFacadeCreatedEvent(
                player, facade, block.defaultBlockState()));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementConstantDefinition(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack card = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(0));
        IVariableFacade facade = getVariableFacade(helper.getLevel(), card);
        fireVariableCreatedTrigger(player, facade, RegistryEntries.BLOCK_LOGIC_PROGRAMMER.get());
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:logic_operations/constant_definition"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementVariableMaterialization(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack card = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(0));
        IVariableFacade facade = getVariableFacade(helper.getLevel(), card);
        fireVariableCreatedTrigger(player, facade, RegistryEntries.BLOCK_MATERIALIZER.get());
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:advanced_network_components/variable_materialization"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementVariableProxying(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack card = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(0));
        IVariableFacade facade = getVariableFacade(helper.getLevel(), card);
        fireVariableCreatedTrigger(player, facade, RegistryEntries.BLOCK_PROXY.get());
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:advanced_network_components/variable_proxying"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRecipeCreation(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack card = createVariableForValue(helper.getLevel(), ValueTypes.OBJECT_RECIPE,
                ValueObjectTypeRecipe.ValueRecipe.of(null));
        IVariableFacade facade = getVariableFacade(helper.getLevel(), card);
        fireVariableCreatedTrigger(player, facade, RegistryEntries.BLOCK_LOGIC_PROGRAMMER.get());
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:recipe_handling/recipe_creation"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRedstoneCapturing(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        fireReaderAspectEvent(helper, player, POS, PartTypes.REDSTONE_READER, Aspects.Read.Redstone.INTEGER_VALUE);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:aspects/redstone_capturing"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRecipeReading(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        fireReaderAspectEvent(helper, player, POS, PartTypes.MACHINE_READER, Aspects.Read.Machine.LIST_GETRECIPES);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:recipe_handling/recipe_reading"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRedstoneObservment(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        IAspectVariable<ValueTypeInteger.ValueInteger> aspectVar =
                makeAspectVar(Aspects.Read.Redstone.INTEGER_VALUE);
        fireVariableDrivenEvent(player, aspectVar);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:aspects/redstone_observement"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementArithmeticAddition(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        LazyExpression<ValueTypeInteger.ValueInteger> opVar =
                makeOpVar(Operators.ARITHMETIC_ADDITION, ValueTypes.INTEGER);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:logic_operations/arithmetic_addition"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementItemOriginIdentification(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        LazyExpression<ValueTypeString.ValueString> opVar =
                makeOpVar(Operators.OBJECT_ITEMSTACK_MODNAME, ValueTypes.STRING);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:advanced_operations/item_origin_identification"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementLogicalListBuilding(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ValueTypeList.ValueList listValue = ValueTypeList.ValueList.ofList(ValueTypes.INTEGER,
                Arrays.asList(
                        ValueTypeInteger.ValueInteger.of(1),
                        ValueTypeInteger.ValueInteger.of(10),
                        ValueTypeInteger.ValueInteger.of(100)));
        Variable<ValueTypeList.ValueList> listVar = new Variable<>(ValueTypes.LIST, listValue);
        fireVariableDrivenEvent(player, listVar);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:advanced_operations/logical_list_building"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementWhatWouldIBeLookingAt(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        LazyExpression<ValueObjectTypeBlock.ValueBlock> opVar =
                makeOpVar(Operators.OBJECT_PLAYER_TARGETBLOCK, ValueTypes.OBJECT_BLOCK);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:advanced_operations/what_would_i_be_looking_at"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRecipeLookup(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // input[0]: aspect variable for recipe-by-output aspect
        IAspectVariable<ValueTypeOperator.ValueOperator> input0 =
                makeAspectVar(Aspects.Read.Machine.OPERATOR_GETRECIPEBYOUTPUT);
        // input[1]: ingredients value (chest x1)
        ValueObjectTypeIngredients.ValueIngredients ingredientsValue =
                ValueObjectTypeIngredients.ValueIngredients.of(
                        MixedIngredients.ofInstance(IngredientComponents.ITEMSTACK,
                                new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:chest")), 1)));
        Variable<ValueObjectTypeIngredients.ValueIngredients> input1 =
                new Variable<>(ValueTypes.OBJECT_INGREDIENTS, ingredientsValue);
        LazyExpression<ValueTypeOperator.ValueOperator> opVar =
                makeOpVar(Operators.OPERATOR_APPLY, ValueTypes.OPERATOR, input0, input1);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:recipe_handling/recipe_lookup"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementLecternData(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        IAspectVariable<ValueTypeNbt.ValueNbt> aspectVar = makeAspectVar(Aspects.Read.Block.NBT);
        fireVariableDrivenEvent(player, aspectVar);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:nbt/lectern_data"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementLecternBook(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // input[0]: aspect variable for read_nbt_block_tile
        IAspectVariable<ValueTypeNbt.ValueNbt> input0 = makeAspectVar(Aspects.Read.Block.NBT);
        // input[1]: string value "Book"
        Variable<ValueTypeString.ValueString> input1 =
                new Variable<>(ValueTypes.STRING, ValueTypeString.ValueString.of("Book"));
        LazyExpression<ValueTypeNbt.ValueNbt> opVar =
                makeOpVar(Operators.NBT_COMPOUND_VALUE_COMPOUND, ValueTypes.NBT, input0, input1);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:nbt/lectern_book"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementLecternBookName(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // input[0]: string value ".Book.id"
        Variable<ValueTypeString.ValueString> input0 =
                new Variable<>(ValueTypes.STRING, ValueTypeString.ValueString.of(".Book.id"));
        // input[1]: aspect variable for read_nbt_block_tile
        IAspectVariable<ValueTypeNbt.ValueNbt> input1 = makeAspectVar(Aspects.Read.Block.NBT);
        LazyExpression<ValueTypeNbt.ValueNbt> opVar =
                makeOpVar(Operators.NBT_PATH_MATCH_FIRST, ValueTypes.NBT, input0, input1);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:nbt/lectern_book_name"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementDynamicAdditions(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // input[0] of outer OPERATOR_APPLY: inner LazyExpression(OPERATOR_APPLY) with type OPERATOR
        LazyExpression<ValueTypeOperator.ValueOperator> inner =
                makeOpVar(Operators.OPERATOR_APPLY, ValueTypes.OPERATOR);
        // input[1] of outer: any integer variable
        Variable<ValueTypeInteger.ValueInteger> intVar =
                new Variable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(0));
        LazyExpression<ValueTypeInteger.ValueInteger> opVar =
                makeOpVar(Operators.OPERATOR_APPLY, ValueTypes.INTEGER, inner, intVar);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:the_value_of_operators/dynamic_additions"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementDynamicListFiltering(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // The value predicate checks that the result is a list [10]
        ValueTypeList.ValueList resultList = ValueTypeList.ValueList.ofList(ValueTypes.INTEGER,
                Arrays.asList(ValueTypeInteger.ValueInteger.of(10)));
        ILazyExpressionValueCache cache = simpleCache();
        LazyExpression<ValueTypeList.ValueList> opVar =
                new LazyExpression<ValueTypeList.ValueList>(0, Operators.OPERATOR_FILTER, new IVariable[0], cache) {
                    @Override public IValueType<ValueTypeList.ValueList> getType() { return ValueTypes.LIST; }
                    @Override public ValueTypeList.ValueList getValue() { return resultList; }
                };
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:the_value_of_operators/dynamic_list_filtering"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementCreeperTaming(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Spawn a creeper as the input entity
        net.minecraft.world.entity.Entity creeper = helper.spawn(EntityType.CREEPER, POS.above());
        Variable<ValueObjectTypeEntity.ValueEntity> creeperVar = new Variable<>(
                ValueTypes.OBJECT_ENTITY, ValueObjectTypeEntity.ValueEntity.of(creeper));
        LazyExpression<ValueTypeDouble.ValueDouble> opVar =
                makeOpVar(Operators.OBJECT_ENTITY_HEALTH, ValueTypes.DOUBLE, creeperVar);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:challenges/creeper_taming"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRecursiveRecursion(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // An infinite list satisfies the "infinite_list: true" predicate
        ValueTypeList.ValueList infiniteList = ValueTypeList.ValueList.ofFactory(
                new ValueTypeListProxyLazyBuilt<>(ValueTypeInteger.ValueInteger.of(0), Operators.ARITHMETIC_ADDITION));
        Variable<ValueTypeList.ValueList> listVar = new Variable<>(ValueTypes.LIST, infiniteList);
        fireVariableDrivenEvent(player, listVar);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:challenges/recursive_recursion"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementToolForObsidian(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // input[0]: stone_pickaxe itemstack value
        Variable<ValueObjectTypeItemStack.ValueItemStack> input0 = new Variable<>(
                ValueTypes.OBJECT_ITEMSTACK,
                ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(
                        BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:stone_pickaxe")))));
        // input[1]: obsidian block value
        Variable<ValueObjectTypeBlock.ValueBlock> input1 = new Variable<>(
                ValueTypes.OBJECT_BLOCK,
                ValueObjectTypeBlock.ValueBlock.of(Blocks.OBSIDIAN.defaultBlockState()));
        LazyExpression<ValueTypeBoolean.ValueBoolean> opVar =
                makeOpVar(Operators.OBJECT_ITEMSTACK_CAN_HARVEST_BLOCK, ValueTypes.BOOLEAN, input0, input1);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:challenges/tool_for_obsidian"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementToolForObsidianNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // input[0]: bone_meal itemstack value (NOT stone_pickaxe - should NOT trigger the advancement)
        Variable<ValueObjectTypeItemStack.ValueItemStack> input0 = new Variable<>(
                ValueTypes.OBJECT_ITEMSTACK,
                ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(
                        BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:bone_meal")))));
        // input[1]: obsidian block value
        Variable<ValueObjectTypeBlock.ValueBlock> input1 = new Variable<>(
                ValueTypes.OBJECT_BLOCK,
                ValueObjectTypeBlock.ValueBlock.of(Blocks.OBSIDIAN.defaultBlockState()));
        LazyExpression<ValueTypeBoolean.ValueBoolean> opVar =
                makeOpVar(Operators.OBJECT_ITEMSTACK_CAN_HARVEST_BLOCK, ValueTypes.BOOLEAN, input0, input1);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:challenges/tool_for_obsidian"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRedstoneTransmission(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Level level = helper.getLevel();

        // Redstone reader at POS facing west, reading from a redstone block (value 15)
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.west(), Blocks.REDSTONE_BLOCK);
        PartHelpers.addPart(level, helper.absolutePos(POS), Direction.WEST,
                PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        PartPos readerPos = PartPos.of(level, helper.absolutePos(POS), Direction.WEST);

        // Redstone writer at POS.east() facing east
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(level, helper.absolutePos(POS.east()), Direction.EAST,
                PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));
        PartPos writerPos = PartPos.of(level, helper.absolutePos(POS.east()), Direction.EAST);

        // Create variable card from the redstone reader for the INTEGER_VALUE aspect
        ItemStack readerCard = createVariableFromReader(level, readerPos, Aspects.Read.Redstone.INTEGER_VALUE);

        // Place variable in writer slot and activate with player to fire the event
        placeVariableInWriterWithPlayer(level, writerPos, Aspects.Write.Redstone.INTEGER, readerCard, player);

        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:aspects/redstone_transmission"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSmartPressurePlate(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Level level = helper.getLevel();

        // Entity reader at POS facing west
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(level, helper.absolutePos(POS), Direction.WEST,
                PartTypes.ENTITY_READER, new ItemStack(PartTypes.ENTITY_READER.getItem()));
        PartPos readerPos = PartPos.of(level, helper.absolutePos(POS), Direction.WEST);

        // Redstone writer at POS.east() facing east
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(level, helper.absolutePos(POS.east()), Direction.EAST,
                PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));
        PartPos writerPos = PartPos.of(level, helper.absolutePos(POS.east()), Direction.EAST);

        // Variable store adjacent to cable
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_VARIABLE_STORE.get());
        BlockEntityVariablestore varStore = helper.getBlockEntity(POS.north());

        // Create entity variable card from reader
        ItemStack entityCard = createVariableFromReader(level, readerPos, Aspects.Read.Entity.ENTITY);
        int entityId = getVariableFacade(level, entityCard).getId();
        varStore.getInventory().setItem(0, entityCard);

        // Create NAMED_NAME operator variable referencing entity variable
        ItemStack nameCard = createVariableForOperator(level, Operators.NAMED_NAME, new int[]{entityId});
        int nameId = getVariableFacade(level, nameCard).getId();
        varStore.getInventory().setItem(1, nameCard);

        // Create a dummy string variable for the second input of RELATIONAL_EQUALS
        ItemStack dummyStringCard = createVariableForValue(level, ValueTypes.STRING,
                ValueTypeString.ValueString.of(""));
        int dummyStringId = getVariableFacade(level, dummyStringCard).getId();
        varStore.getInventory().setItem(2, dummyStringCard);

        // Create RELATIONAL_EQUALS operator with [nameId, dummyStringId] inputs
        ItemStack relEqualsCard = createVariableForOperator(level, Operators.RELATIONAL_EQUALS,
                new int[]{nameId, dummyStringId});

        // Place in writer and activate with player
        placeVariableInWriterWithPlayer(level, writerPos, Aspects.Write.Redstone.BOOLEAN, relEqualsCard, player);

        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:challenges/smart_pressure_plate"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSpongeStepSound(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Level level = helper.getLevel();

        // Cable at POS with audio writer facing west
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(level, helper.absolutePos(POS), Direction.WEST,
                PartTypes.AUDIO_WRITER, new ItemStack(PartTypes.AUDIO_WRITER.getItem()));
        PartPos writerPos = PartPos.of(level, helper.absolutePos(POS), Direction.WEST);

        // Variable store adjacent to cable
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_VARIABLE_STORE.get());
        BlockEntityVariablestore varStore = helper.getBlockEntity(POS.north());

        // Create wet sponge block value variable
        ItemStack spongeCard = createVariableForValue(level, ValueTypes.OBJECT_BLOCK,
                ValueObjectTypeBlock.ValueBlock.of(Blocks.WET_SPONGE.defaultBlockState()));
        int spongeId = getVariableFacade(level, spongeCard).getId();
        varStore.getInventory().setItem(0, spongeCard);

        // Create BLOCK_STEPSOUND operator variable referencing the sponge variable
        ItemStack stepSoundCard = createVariableForOperator(level, Operators.OBJECT_BLOCK_STEPSOUND,
                new int[]{spongeId});

        // Place in audio writer for STRING_SOUND aspect and activate with player
        placeVariableInWriterWithPlayer(level, writerPos, Aspects.Write.Audio.STRING_SOUND, stepSoundCard, player);

        helper.succeedWhen(() -> assertAdvancement(helper, player, "integrateddynamics:challenges/sponge_step_sound"));
    }

    // ---- Negative Tests ----

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRootNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Fire ItemCraftedEvent with a vanilla item (not an integrateddynamics item) - should NOT trigger root
        ItemStack stick = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:stick")));
        NeoForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(player, stick, null));
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:root"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementMeneglinDiscoveryNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Add a vanilla log instead of menril_log - should NOT trigger meneglin_discovery
        ItemStack oakLog = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:oak_log")));
        player.addItem(oakLog);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:meneglin_basics/meneglin_discovery"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementMenrilProductionNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Add a vanilla block instead of crystalized_menril_block - should NOT trigger menril_production
        ItemStack oakPlanks = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:oak_planks")));
        player.addItem(oakPlanks);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:meneglin_basics/menril_production"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSqueezingNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Craft drying_basin instead of squeezer - should NOT trigger squeezing
        fireItemCraftedTrigger(player, "integrateddynamics:drying_basin");
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:meneglin_basics/squeezing"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementDryingNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Craft squeezer instead of drying_basin - should NOT trigger drying
        fireItemCraftedTrigger(player, "integrateddynamics:squeezer");
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:meneglin_basics/drying"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementCablesLogicNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Craft wrench instead of cable - should NOT trigger cables_logic
        fireItemCraftedTrigger(player, "integrateddynamics:wrench");
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:network_foundations/cables_logic"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementMenrilWrenchingNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Craft cable instead of wrench - should NOT trigger menril_wrenching
        fireItemCraftedTrigger(player, "integrateddynamics:cable");
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:network_foundations/menril_wrenching"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementVariablesNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Craft variable_transformer_input instead of variable - should NOT trigger variables
        fireItemCraftedTrigger(player, "integrateddynamics:variable_transformer_input");
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:network_foundations/variables"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementVariableInputNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Craft variable instead of variable_transformer_input - should NOT trigger variable_input
        fireItemCraftedTrigger(player, "integrateddynamics:variable");
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:network_foundations/variable_input"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementVariableOutputNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Craft variable instead of variable_transformer_output - should NOT trigger variable_output
        fireItemCraftedTrigger(player, "integrateddynamics:variable");
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:network_foundations/variable_output"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementNetworksLogicNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Place only 9 connected cables (not the required 10) - should NOT trigger networks_logic
        for (int i = 0; i < 9; i++) {
            helper.setBlock(POS.east(i), RegistryEntries.BLOCK_CABLE.value());
        }
        INetwork network = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
        NeoForge.EVENT_BUS.post(new NetworkInitializedEvent(
                network, helper.getLevel(), helper.absolutePos(POS), player));
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:network_foundations/networks_logic"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBlockReadingNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Craft inventory_reader instead of block_reader - should NOT trigger block_reading
        fireItemCraftedTrigger(player, "integrateddynamics:part_inventory_reader");
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:basic_network_components/block_reading"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementInventoryReadingNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Craft block_reader instead of inventory_reader - should NOT trigger inventory_reading
        fireItemCraftedTrigger(player, "integrateddynamics:part_block_reader");
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:basic_network_components/inventory_reading"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementMachineReadingNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Craft block_reader instead of machine_reader - should NOT trigger machine_reading
        fireItemCraftedTrigger(player, "integrateddynamics:part_block_reader");
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:basic_network_components/machine_reading"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRedstoneReadingNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Craft block_reader instead of redstone_reader - should NOT trigger redstone_reading
        fireItemCraftedTrigger(player, "integrateddynamics:part_block_reader");
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:basic_network_components/redstone_reading"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementValueDisplayingNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Craft block_reader instead of display_panel - should NOT trigger value_displaying
        fireItemCraftedTrigger(player, "integrateddynamics:part_block_reader");
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:basic_network_components/value_displaying"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementLogicProgrammingNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Open a different container (player inventory) - should NOT trigger logic_programming
        NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.inventoryMenu));
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:logic_operations/logic_programming"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementConstantDefinitionNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Create a BOOLEAN variable at logic_programmer (not integer type) - should NOT trigger constant_definition
        ItemStack card = createVariableForValue(helper.getLevel(), ValueTypes.BOOLEAN, ValueTypeBoolean.ValueBoolean.of(false));
        IVariableFacade facade = getVariableFacade(helper.getLevel(), card);
        fireVariableCreatedTrigger(player, facade, RegistryEntries.BLOCK_LOGIC_PROGRAMMER.get());
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:logic_operations/constant_definition"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementVariableMaterializationNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Create integer variable at PROXY (not materializer) - should NOT trigger variable_materialization
        ItemStack card = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(0));
        IVariableFacade facade = getVariableFacade(helper.getLevel(), card);
        fireVariableCreatedTrigger(player, facade, RegistryEntries.BLOCK_PROXY.get());
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:advanced_network_components/variable_materialization"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementVariableProxyingNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Create integer variable at MATERIALIZER (not proxy) - should NOT trigger variable_proxying
        ItemStack card = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(0));
        IVariableFacade facade = getVariableFacade(helper.getLevel(), card);
        fireVariableCreatedTrigger(player, facade, RegistryEntries.BLOCK_MATERIALIZER.get());
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:advanced_network_components/variable_proxying"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRecipeCreationNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Create INTEGER variable at logic_programmer (not recipe type) - should NOT trigger recipe_creation
        ItemStack card = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(0));
        IVariableFacade facade = getVariableFacade(helper.getLevel(), card);
        fireVariableCreatedTrigger(player, facade, RegistryEntries.BLOCK_LOGIC_PROGRAMMER.get());
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:recipe_handling/recipe_creation"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRedstoneCapturingNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Fire reader aspect event with machine_reader (not redstone_reader) - should NOT trigger redstone_capturing
        fireReaderAspectEvent(helper, player, POS, PartTypes.MACHINE_READER, Aspects.Read.Machine.LIST_GETRECIPES);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:aspects/redstone_capturing"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRecipeReadingNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Fire reader aspect event with redstone_reader (not machine_reader) - should NOT trigger recipe_reading
        fireReaderAspectEvent(helper, player, POS, PartTypes.REDSTONE_READER, Aspects.Read.Redstone.INTEGER_VALUE);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:recipe_handling/recipe_reading"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRedstoneObservementNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Fire variable driven event with block NBT aspect (not redstone value aspect) - should NOT trigger redstone_observement
        IAspectVariable<ValueTypeNbt.ValueNbt> aspectVar = makeAspectVar(Aspects.Read.Block.NBT);
        fireVariableDrivenEvent(player, aspectVar);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:aspects/redstone_observement"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementArithmeticAdditionNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Fire variable driven event with OPERATOR_APPLY (not arithmetic_addition) - should NOT trigger arithmetic_addition
        LazyExpression<ValueTypeOperator.ValueOperator> opVar =
                makeOpVar(Operators.OPERATOR_APPLY, ValueTypes.OPERATOR);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:logic_operations/arithmetic_addition"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementItemOriginIdentificationNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Fire variable driven event with ARITHMETIC_ADDITION (not itemstack_mod) - should NOT trigger item_origin_identification
        LazyExpression<ValueTypeInteger.ValueInteger> opVar =
                makeOpVar(Operators.ARITHMETIC_ADDITION, ValueTypes.INTEGER);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:advanced_operations/item_origin_identification"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementLogicalListBuildingNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Fire variable driven event with a list [1, 10, 99] (not [1, 10, 100]) - should NOT trigger logical_list_building
        ValueTypeList.ValueList listValue = ValueTypeList.ValueList.ofList(ValueTypes.INTEGER,
                Arrays.asList(
                        ValueTypeInteger.ValueInteger.of(1),
                        ValueTypeInteger.ValueInteger.of(10),
                        ValueTypeInteger.ValueInteger.of(99)));
        Variable<ValueTypeList.ValueList> listVar = new Variable<>(ValueTypes.LIST, listValue);
        fireVariableDrivenEvent(player, listVar);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:advanced_operations/logical_list_building"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementWhatWouldIBeLookingAtNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Fire variable driven event with ARITHMETIC_ADDITION (not entity_targetblock) - should NOT trigger what_would_i_be_looking_at
        LazyExpression<ValueTypeInteger.ValueInteger> opVar =
                makeOpVar(Operators.ARITHMETIC_ADDITION, ValueTypes.INTEGER);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:advanced_operations/what_would_i_be_looking_at"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRecipeLookupNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Same operator_apply with recipe aspect, but iron_ingot instead of chest - should NOT trigger recipe_lookup
        IAspectVariable<ValueTypeOperator.ValueOperator> input0 =
                makeAspectVar(Aspects.Read.Machine.OPERATOR_GETRECIPEBYOUTPUT);
        ValueObjectTypeIngredients.ValueIngredients ingredientsValue =
                ValueObjectTypeIngredients.ValueIngredients.of(
                        MixedIngredients.ofInstance(IngredientComponents.ITEMSTACK,
                                new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:iron_ingot")), 1)));
        Variable<ValueObjectTypeIngredients.ValueIngredients> input1 =
                new Variable<>(ValueTypes.OBJECT_INGREDIENTS, ingredientsValue);
        LazyExpression<ValueTypeOperator.ValueOperator> opVar =
                makeOpVar(Operators.OPERATOR_APPLY, ValueTypes.OPERATOR, input0, input1);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:recipe_handling/recipe_lookup"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementLecternDataNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Fire variable driven event with redstone aspect (not block NBT aspect) - should NOT trigger lectern_data
        IAspectVariable<ValueTypeInteger.ValueInteger> aspectVar =
                makeAspectVar(Aspects.Read.Redstone.INTEGER_VALUE);
        fireVariableDrivenEvent(player, aspectVar);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:nbt/lectern_data"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementLecternBookNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Same operator with NBT aspect but wrong key "Page" (not "Book") - should NOT trigger lectern_book
        IAspectVariable<ValueTypeNbt.ValueNbt> input0 = makeAspectVar(Aspects.Read.Block.NBT);
        Variable<ValueTypeString.ValueString> input1 =
                new Variable<>(ValueTypes.STRING, ValueTypeString.ValueString.of("Page"));
        LazyExpression<ValueTypeNbt.ValueNbt> opVar =
                makeOpVar(Operators.NBT_COMPOUND_VALUE_COMPOUND, ValueTypes.NBT, input0, input1);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:nbt/lectern_book"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementLecternBookNameNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Same operator with wrong path ".Book.title" (not ".Book.id") - should NOT trigger lectern_book_name
        Variable<ValueTypeString.ValueString> input0 =
                new Variable<>(ValueTypes.STRING, ValueTypeString.ValueString.of(".Book.title"));
        IAspectVariable<ValueTypeNbt.ValueNbt> input1 = makeAspectVar(Aspects.Read.Block.NBT);
        LazyExpression<ValueTypeNbt.ValueNbt> opVar =
                makeOpVar(Operators.NBT_PATH_MATCH_FIRST, ValueTypes.NBT, input0, input1);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:nbt/lectern_book_name"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementDynamicAdditionsNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // OPERATOR_APPLY with inner OPERATOR_APPLY but STRING input (not INTEGER) - should NOT trigger dynamic_additions
        LazyExpression<ValueTypeOperator.ValueOperator> inner =
                makeOpVar(Operators.OPERATOR_APPLY, ValueTypes.OPERATOR);
        Variable<ValueTypeString.ValueString> stringVar =
                new Variable<>(ValueTypes.STRING, ValueTypeString.ValueString.of(""));
        LazyExpression<ValueTypeString.ValueString> opVar =
                makeOpVar(Operators.OPERATOR_APPLY, ValueTypes.STRING, inner, stringVar);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:the_value_of_operators/dynamic_additions"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementDynamicListFilteringNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // operator_filter with result list [20] (not [10]) - should NOT trigger dynamic_list_filtering
        ValueTypeList.ValueList resultList = ValueTypeList.ValueList.ofList(ValueTypes.INTEGER,
                Arrays.asList(ValueTypeInteger.ValueInteger.of(20)));
        ILazyExpressionValueCache cache = simpleCache();
        LazyExpression<ValueTypeList.ValueList> opVar =
                new LazyExpression<ValueTypeList.ValueList>(0, Operators.OPERATOR_FILTER, new IVariable[0], cache) {
                    @Override public IValueType<ValueTypeList.ValueList> getType() { return ValueTypes.LIST; }
                    @Override public ValueTypeList.ValueList getValue() { return resultList; }
                };
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:the_value_of_operators/dynamic_list_filtering"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementCreeperTamingNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Use SHEEP instead of CREEPER - should NOT trigger creeper_taming
        net.minecraft.world.entity.Entity sheep = helper.spawn(EntityType.SHEEP, POS.above());
        Variable<ValueObjectTypeEntity.ValueEntity> sheepVar = new Variable<>(
                ValueTypes.OBJECT_ENTITY, ValueObjectTypeEntity.ValueEntity.of(sheep));
        LazyExpression<ValueTypeDouble.ValueDouble> opVar =
                makeOpVar(Operators.OBJECT_ENTITY_HEALTH, ValueTypes.DOUBLE, sheepVar);
        fireVariableDrivenEvent(player, opVar);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:challenges/creeper_taming"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRecursiveRecursionNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Use a finite list (not infinite) - should NOT trigger recursive_recursion
        ValueTypeList.ValueList finiteList = ValueTypeList.ValueList.ofList(ValueTypes.INTEGER,
                Arrays.asList(ValueTypeInteger.ValueInteger.of(1), ValueTypeInteger.ValueInteger.of(2)));
        Variable<ValueTypeList.ValueList> listVar = new Variable<>(ValueTypes.LIST, finiteList);
        fireVariableDrivenEvent(player, listVar);
        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:challenges/recursive_recursion"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRedstoneTransmissionNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Level level = helper.getLevel();

        // Same setup but STONE instead of REDSTONE_BLOCK (gives value 0, not 15)
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.west(), Blocks.STONE);
        PartHelpers.addPart(level, helper.absolutePos(POS), Direction.WEST,
                PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        PartPos readerPos = PartPos.of(level, helper.absolutePos(POS), Direction.WEST);

        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(level, helper.absolutePos(POS.east()), Direction.EAST,
                PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));
        PartPos writerPos = PartPos.of(level, helper.absolutePos(POS.east()), Direction.EAST);

        ItemStack readerCard = createVariableFromReader(level, readerPos, Aspects.Read.Redstone.INTEGER_VALUE);
        placeVariableInWriterWithPlayer(level, writerPos, Aspects.Write.Redstone.INTEGER, readerCard, player);

        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:aspects/redstone_transmission"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSmartPressurePlateNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Level level = helper.getLevel();

        // Same redstone writer setup but use a plain boolean variable (not the required operator chain)
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(level, helper.absolutePos(POS.east()), Direction.EAST,
                PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));
        PartPos writerPos = PartPos.of(level, helper.absolutePos(POS.east()), Direction.EAST);

        ItemStack boolCard = createVariableForValue(level, ValueTypes.BOOLEAN,
                ValueTypeBoolean.ValueBoolean.of(true));
        placeVariableInWriterWithPlayer(level, writerPos, Aspects.Write.Redstone.BOOLEAN, boolCard, player);

        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:challenges/smart_pressure_plate"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSpongeStepSoundNegative(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Level level = helper.getLevel();

        // Same setup but with dry SPONGE (not WET_SPONGE) - should NOT trigger sponge_step_sound
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(level, helper.absolutePos(POS), Direction.WEST,
                PartTypes.AUDIO_WRITER, new ItemStack(PartTypes.AUDIO_WRITER.getItem()));
        PartPos writerPos = PartPos.of(level, helper.absolutePos(POS), Direction.WEST);

        helper.setBlock(POS.north(), RegistryEntries.BLOCK_VARIABLE_STORE.get());
        BlockEntityVariablestore varStore = helper.getBlockEntity(POS.north());

        ItemStack spongeCard = createVariableForValue(level, ValueTypes.OBJECT_BLOCK,
                ValueObjectTypeBlock.ValueBlock.of(Blocks.SPONGE.defaultBlockState()));
        int spongeId = getVariableFacade(level, spongeCard).getId();
        varStore.getInventory().setItem(0, spongeCard);

        ItemStack stepSoundCard = createVariableForOperator(level, Operators.OBJECT_BLOCK_STEPSOUND,
                new int[]{spongeId});
        placeVariableInWriterWithPlayer(level, writerPos, Aspects.Write.Audio.STRING_SOUND, stepSoundCard, player);

        helper.succeedWhen(() -> assertAdvancementNotDone(helper, player, "integrateddynamics:challenges/sponge_step_sound"));
    }

}
