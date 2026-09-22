package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.blockentity.BlockEntityDelay;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.PartTypePanelDisplay;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.*;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsDelayer {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    // Layout of the "Saving State" tutorial network
    public static final BlockPos STATE_CABLE = POS;
    public static final BlockPos STATE_CABLE_WRITER = POS.east();
    public static final BlockPos STATE_DELAY = POS.below();
    public static final BlockPos STATE_DELAY_PRESSES = POS.east().below();
    public static final BlockPos STATE_STORE = POS.north();
    public static final BlockPos STATE_BUTTON_ON = POS.west();
    public static final BlockPos STATE_BUTTON_OFF = POS.south();
    public static final BlockPos STATE_LAMP = POS.east().east();

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDelayerFromAddition(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone readers
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Place proxy
        helper.setBlock(POS.below(), RegistryEntries.BLOCK_DELAY.value());
        BlockEntityDelay delay = helper.getBlockEntity(POS.below());

        // Place display panel
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST, PartTypes.DISPLAY_PANEL, new ItemStack(PartTypes.DISPLAY_PANEL.getItem()));

        // Place variable store
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_VARIABLE_STORE.get());
        BlockEntityVariablestore variableStore = helper.getBlockEntity(POS.north());

        // Produce a redstone signals
        helper.setBlock(POS.west().below(), Blocks.STONE);
        helper.setBlock(POS.west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west().below(), Blocks.STONE);
        helper.setBlock(POS.west().west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west().west().below(), Blocks.STONE);
        helper.setBlock(POS.west().west().west(), Blocks.REDSTONE_TORCH);
        helper.setBlock(POS.south(), Blocks.REDSTONE_BLOCK);

        // Writer redstone signal from redstone readers to variable card
        ItemStack variableAspect1 = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Redstone.INTEGER_VALUE);
        ItemStack variableAspect2 = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH), Aspects.Read.Redstone.INTEGER_VALUE);

        // Insert redstone signal variable in variable store
        variableStore.getInventory().setItem(0, variableAspect1);
        variableStore.getInventory().setItem(1, variableAspect2);

        // Create variable card for + operator on variable aspect
        ItemStack variableAdded = createVariableForOperator(helper.getLevel(), Operators.ARITHMETIC_ADDITION, new int[]{
                getVariableFacade(helper.getLevel(), variableAspect1).getId(),
                getVariableFacade(helper.getLevel(), variableAspect2).getId()
        });

        // Place variable in delay
        delay.getInventory().setItem(0, variableAdded);
        delay.getInventory().setItem(1, new ItemStack(RegistryEntries.ITEM_VARIABLE.get()));

        // Wait a tick for the delay to write
        Wrapper<Pair<PartTypePanelDisplay, PartTypePanelDisplay.State>> partAndState = new Wrapper<>(null);
        helper.runAfterDelay(1, () -> {
            ItemStack variableDelayed = delay.getInventory().getItem(2);

            // Place delayed variable in writer
            partAndState.set(placeVariableInDisplayPanel(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST), variableDelayed));
        });

        // Remove signal of the full redstone block after 3 ticks
        helper.runAfterDelay(3, () -> helper.destroyBlock(POS.south()));

        helper.succeedWhen(() -> {
            helper.assertTrue(partAndState.get() != null, "No variable was delayed");
            assertValueEqual(partAndState.get().getRight().getDisplayValue(), ValueTypeList.ValueList.ofAll(
                    ValueTypeInteger.ValueInteger.of(29),
                    ValueTypeInteger.ValueInteger.of(29),
                    ValueTypeInteger.ValueInteger.of(29),
                    ValueTypeInteger.ValueInteger.of(14)
            ));
        });
    }


    // ---- "Saving State" tutorial helpers ----

    private static int[] ids(Level level, ItemStack... cards) {
        int[] ids = new int[cards.length];
        for (int i = 0; i < cards.length; i++) {
            ids[i] = getVariableFacade(level, cards[i]).getId();
        }
        return ids;
    }

    static PartPos stateWriterPos(GameTestHelper helper) {
        return PartPos.of(helper.getLevel(), helper.absolutePos(STATE_CABLE_WRITER), Direction.EAST);
    }

    /**
     * Places the blocks and parts of the tutorial network: two cables, two delayers, a variable store,
     * an "on" and "off" redstone reader, and a redstone writer targeting a lamp.
     * Cards can only be built two ticks later, once the delayers have generated their proxy ids.
     */
    static void placeStateNetwork(GameTestHelper helper) {
        Level level = helper.getLevel();
        helper.setBlock(STATE_CABLE, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(STATE_CABLE_WRITER, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(STATE_DELAY, RegistryEntries.BLOCK_DELAY.get());
        helper.setBlock(STATE_DELAY_PRESSES, RegistryEntries.BLOCK_DELAY.get());
        helper.setBlock(STATE_STORE, RegistryEntries.BLOCK_VARIABLE_STORE.get());
        helper.setBlock(STATE_LAMP, Blocks.REDSTONE_LAMP);
        PartHelpers.addPart(level, helper.absolutePos(STATE_CABLE), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        PartHelpers.addPart(level, helper.absolutePos(STATE_CABLE), Direction.SOUTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        PartHelpers.addPart(level, helper.absolutePos(STATE_CABLE_WRITER), Direction.EAST, PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));
    }

    /**
     * Builds the latch of the tutorial: state = on || (previous && !off), with previous read from the delayer history.
     * The state card is placed in the delayer, and a second state card is returned for the redstone writer.
     */
    static ItemStack buildStateLatch(GameTestHelper helper) {
        Level level = helper.getLevel();
        BlockEntityVariablestore store = helper.getBlockEntity(STATE_STORE);
        BlockEntityDelay delay = helper.getBlockEntity(STATE_DELAY);
        delay.setCapacity(1);

        ItemStack on = createVariableFromReader(level, PartPos.of(level, helper.absolutePos(STATE_CABLE), Direction.WEST), Aspects.Read.Redstone.BOOLEAN_NONLOW);
        ItemStack off = createVariableFromReader(level, PartPos.of(level, helper.absolutePos(STATE_CABLE), Direction.SOUTH), Aspects.Read.Redstone.BOOLEAN_NONLOW);
        ItemStack falseCard = createVariableForValue(level, ValueTypes.BOOLEAN, ValueTypeBoolean.ValueBoolean.of(false));
        ItemStack zero = createVariableForValue(level, ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(0));
        ItemStack history = delay.writeProxyInfo(true, new ItemStack(RegistryEntries.ITEM_VARIABLE.get()), delay.getProxyId());
        ItemStack previous = createVariableForOperator(level, Operators.LIST_ELEMENT_DEFAULT, ids(level, history, zero, falseCard));
        ItemStack notOff = createVariableForOperator(level, Operators.LOGICAL_NOT, ids(level, off));
        ItemStack keep = createVariableForOperator(level, Operators.LOGICAL_AND, ids(level, previous, notOff));

        ItemStack[] stored = {on, off, falseCard, zero, history, previous, notOff, keep};
        for (int i = 0; i < stored.length; i++) {
            store.getInventory().setItem(i, stored[i]);
        }
        delay.getInventory().setItem(0, createVariableForOperator(level, Operators.LOGICAL_OR, ids(level, on, keep)));
        return createVariableForOperator(level, Operators.LOGICAL_OR, ids(level, on, keep));
    }

    /**
     * Builds the toggle of the tutorial on top of {@link #buildStateLatch(GameTestHelper)}:
     * state = edge ? !previous : previous, with edge detected as the press history equalling [false, true].
     * The state card replaces the latch card in the delayer, and a second state card is returned for the redstone writer.
     */
    static ItemStack buildStateToggle(GameTestHelper helper) {
        Level level = helper.getLevel();
        BlockEntityVariablestore store = helper.getBlockEntity(STATE_STORE);
        BlockEntityDelay delay = helper.getBlockEntity(STATE_DELAY);
        BlockEntityDelay delayPresses = helper.getBlockEntity(STATE_DELAY_PRESSES);
        delayPresses.setCapacity(2);
        ItemStack previous = store.getInventory().getItem(5);

        delayPresses.getInventory().setItem(0, createVariableFromReader(level, PartPos.of(level, helper.absolutePos(STATE_CABLE), Direction.WEST), Aspects.Read.Redstone.BOOLEAN_NONLOW));
        ItemStack presses = delayPresses.writeProxyInfo(true, new ItemStack(RegistryEntries.ITEM_VARIABLE.get()), delayPresses.getProxyId());
        ItemStack pattern = createVariableForValue(level, ValueTypes.LIST, ValueTypeList.ValueList.ofAll(
                ValueTypeBoolean.ValueBoolean.of(false),
                ValueTypeBoolean.ValueBoolean.of(true)
        ));
        ItemStack edge = createVariableForOperator(level, Operators.RELATIONAL_EQUALS, ids(level, presses, pattern));
        ItemStack notPrevious = createVariableForOperator(level, Operators.LOGICAL_NOT, ids(level, previous));

        ItemStack[] stored = {presses, pattern, edge, notPrevious};
        for (int i = 0; i < stored.length; i++) {
            store.getInventory().setItem(8 + i, stored[i]);
        }
        delay.getInventory().setItem(0, createVariableForOperator(level, Operators.GENERAL_CHOICE, ids(level, edge, notPrevious, previous)));
        return createVariableForOperator(level, Operators.GENERAL_CHOICE, ids(level, edge, notPrevious, previous));
    }

    private static void pressButton(GameTestHelper helper, BlockPos pos, int tick) {
        helper.runAfterDelay(tick, () -> helper.setBlock(pos, Blocks.REDSTONE_BLOCK));
        helper.runAfterDelay(tick + 5, () -> helper.setBlock(pos, Blocks.AIR));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDelayerStateLatch(GameTestHelper helper) {
        placeStateNetwork(helper);
        helper.runAfterDelay(2, () -> placeVariableInWriter(helper, stateWriterPos(helper), Aspects.Write.Redstone.BOOLEAN, buildStateLatch(helper)));

        helper.runAfterDelay(8, () -> helper.assertBlockProperty(STATE_LAMP, RedstoneLampBlock.LIT, false));
        pressButton(helper, STATE_BUTTON_ON, 10);
        helper.runAfterDelay(25, () -> helper.assertBlockProperty(STATE_LAMP, RedstoneLampBlock.LIT, true));
        pressButton(helper, STATE_BUTTON_OFF, 30);
        helper.runAfterDelay(45, () -> helper.assertBlockProperty(STATE_LAMP, RedstoneLampBlock.LIT, false));
        pressButton(helper, STATE_BUTTON_ON, 50);
        helper.runAfterDelay(65, () -> {
            helper.assertBlockProperty(STATE_LAMP, RedstoneLampBlock.LIT, true);
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDelayerStateToggle(GameTestHelper helper) {
        placeStateNetwork(helper);
        helper.runAfterDelay(2, () -> {
            buildStateLatch(helper);
            placeVariableInWriter(helper, stateWriterPos(helper), Aspects.Write.Redstone.BOOLEAN, buildStateToggle(helper));
        });

        helper.runAfterDelay(8, () -> helper.assertBlockProperty(STATE_LAMP, RedstoneLampBlock.LIT, false));
        pressButton(helper, STATE_BUTTON_ON, 10);
        helper.runAfterDelay(25, () -> helper.assertBlockProperty(STATE_LAMP, RedstoneLampBlock.LIT, true));
        pressButton(helper, STATE_BUTTON_ON, 30);
        helper.runAfterDelay(45, () -> helper.assertBlockProperty(STATE_LAMP, RedstoneLampBlock.LIT, false));
        pressButton(helper, STATE_BUTTON_ON, 50);
        helper.runAfterDelay(65, () -> {
            helper.assertBlockProperty(STATE_LAMP, RedstoneLampBlock.LIT, true);
            helper.succeed();
        });
    }

}
