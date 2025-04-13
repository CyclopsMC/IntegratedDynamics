package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
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
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
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

}
