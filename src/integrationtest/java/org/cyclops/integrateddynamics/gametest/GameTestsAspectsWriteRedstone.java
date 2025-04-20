package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.*;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsAspectsWriteRedstone {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsWriteRedstoneBooleanTrue(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.REDSTONE_WIRE);
        testWriteAspectSetup(POS, helper, PartTypes.REDSTONE_WRITER, Aspects.Write.Redstone.BOOLEAN, ValueTypeBoolean.ValueBoolean.of(true));
        helper.succeedWhen(() -> helper.assertBlockProperty(POS.west(), RedStoneWireBlock.POWER, 15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsWriteRedstoneBooleanFalse(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.REDSTONE_WIRE);
        testWriteAspectSetup(POS, helper, PartTypes.REDSTONE_WRITER, Aspects.Write.Redstone.BOOLEAN, ValueTypeBoolean.ValueBoolean.of(false));
        helper.succeedWhen(() -> helper.assertBlockProperty(POS.west(), RedStoneWireBlock.POWER, 0));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsWriteRedstoneInteger(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.REDSTONE_WIRE);
        testWriteAspectSetup(POS, helper, PartTypes.REDSTONE_WRITER, Aspects.Write.Redstone.INTEGER, ValueTypeInteger.ValueInteger.of(10));
        helper.succeedWhen(() -> helper.assertBlockProperty(POS.west(), RedStoneWireBlock.POWER, 10));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsWriteRedstoneBooleanPulse(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.EAST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        testWriteAspectSetup(POS, helper, PartTypes.REDSTONE_WRITER, Aspects.Write.Redstone.BOOLEAN_PULSE, createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.EAST), Aspects.Read.Redstone.BOOLEAN_CLOCK));
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertBlockProperty(POS.west(), RedStoneWireBlock.POWER, 0))
                .thenWaitUntil(() -> helper.assertBlockProperty(POS.west(), RedStoneWireBlock.POWER, 15))
                .thenWaitUntil(() -> helper.assertBlockProperty(POS.west(), RedStoneWireBlock.POWER, 0))
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsWriteRedstoneIntegerPulse(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.EAST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Place variable store
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_VARIABLE_STORE.get());
        BlockEntityVariablestore variableStore = helper.getBlockEntity(POS.north());

        // Writer redstone signal from redstone reader to variable card
        ItemStack variableClock = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.EAST), Aspects.Read.Redstone.BOOLEAN_CLOCK);
        variableStore.getInventory().setItem(0, variableClock);

        // Create 1 and 0 constants
        ItemStack variable1 = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(0));
        ItemStack variable2 = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(3));
        variableStore.getInventory().setItem(1, variable1);
        variableStore.getInventory().setItem(2, variable2);

        // Create variable card for choice operator that switches between 0 and 1
        ItemStack variableChoice = createVariableForOperator(helper.getLevel(), Operators.GENERAL_CHOICE, new int[]{
                getVariableFacade(helper.getLevel(), variableClock).getId(),
                getVariableFacade(helper.getLevel(), variable1).getId(),
                getVariableFacade(helper.getLevel(), variable2).getId()
        });

        testWriteAspectSetup(POS, helper, PartTypes.REDSTONE_WRITER, Aspects.Write.Redstone.INTEGER_PULSE, variableChoice);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertBlockProperty(POS.west(), RedStoneWireBlock.POWER, 0))
                .thenWaitUntil(() -> helper.assertBlockProperty(POS.west(), RedStoneWireBlock.POWER, 15))
                .thenWaitUntil(() -> helper.assertBlockProperty(POS.west(), RedStoneWireBlock.POWER, 0))
                .thenSucceed();
    }

}
