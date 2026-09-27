package org.cyclops.integrateddynamics.gametest;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.PartTypePanelDisplay;
import org.cyclops.integrateddynamics.part.aspect.Aspects;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteBuilders;

import java.util.Objects;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.*;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsCableTicking {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(1, 1, 1);
    // Same layout as the redstone writer tests
    public static final BlockPos PULSE_POS = BlockPos.ZERO.offset(2, 0, 2);

    private static boolean isTicking(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockState(pos).getValue(BlockCable.TICKING);
    }

    private static BlockEntityMultipartTicking getBlockEntity(GameTestHelper helper, BlockPos pos) {
        return BlockEntityHelpers.get(helper.getLevel(), helper.absolutePos(pos), BlockEntityMultipartTicking.class)
                .orElseThrow();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCableTickingIdleStops(GameTestHelper helper) {
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        helper.assertTrue(isTicking(helper, POS), "Newly placed cable is not ticking");

        helper.succeedWhen(() -> {
            helper.assertFalse(isTicking(helper, POS), "Idle cable is still ticking");
            helper.assertFalse(isTicking(helper, POS.east()), "Idle cable is still ticking");
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCableTickingWithPart(GameTestHelper helper) {
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        helper.runAfterDelay(20, () -> {
            helper.assertTrue(isTicking(helper, POS), "Cable with part is not ticking");

            // Removing the last part must make it idle again
            PartHelpers.getPartContainerChecked(helper.getLevel(), helper.absolutePos(POS), Direction.WEST)
                    .removePart(Direction.WEST, null, false, false);
            helper.succeedWhen(() -> helper.assertFalse(isTicking(helper, POS), "Cable without parts is still ticking"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCableTickingIdleWakesOnPartAdded(GameTestHelper helper) {
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        helper.runAfterDelay(20, () -> {
            helper.assertFalse(isTicking(helper, POS), "Idle cable is still ticking");

            PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
            helper.assertTrue(isTicking(helper, POS), "Cable did not start ticking when a part was added");

            helper.runAfterDelay(20, () -> {
                helper.assertTrue(isTicking(helper, POS), "Cable with part stopped ticking");
                helper.succeed();
            });
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCableTickingIdleConnectionUpdate(GameTestHelper helper) {
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        helper.runAfterDelay(20, () -> {
            helper.assertFalse(isTicking(helper, POS), "Idle cable is still ticking");

            // Connection changes of an idle cable must be handled without ticking
            helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());
            helper.assertFalse(isTicking(helper, POS), "Idle cable started ticking on a connection change");
            helper.assertFalse(getBlockEntity(helper, POS).shouldSendUpdate(), "Idle cable has a pending update");
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST),
                    "Connected cables are invalid"
            );

            helper.succeedWhen(() -> {
                INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
                INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
                helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
                helper.assertFalse(isTicking(helper, POS.east()), "Idle cable is still ticking");
            });
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCableTickingIdleLoadRevalidatesNetwork(GameTestHelper helper) {
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        helper.runAfterDelay(20, () -> {
            helper.assertFalse(isTicking(helper, POS), "Idle cable is still ticking");
            INetwork network = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);

            // Simulate loading an idle cable from disk, where the network is not yet attached
            BlockEntityMultipartTicking blockEntity = getBlockEntity(helper, POS);
            blockEntity.getNetworkCarrier().setNetwork(null);
            blockEntity.onLoad();
            helper.assertTrue(blockEntity.getNetwork() == network, "Network was not revalidated");
            helper.assertFalse(isTicking(helper, POS), "Cable started ticking while it could be revalidated directly");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCableTickingIdleLoadFallbackTicking(GameTestHelper helper) {
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        helper.runAfterDelay(20, () -> {
            helper.assertFalse(isTicking(helper, POS), "Idle cable is still ticking");

            // Simulate loading an idle cable that still requires work in its ticker
            BlockEntityMultipartTicking blockEntity = getBlockEntity(helper, POS);
            blockEntity.getConnected().clear();
            blockEntity.onLoad();
            helper.assertTrue(isTicking(helper, POS), "Loaded idle cable with pending work did not start ticking");

            helper.succeedWhen(() -> {
                helper.assertFalse(blockEntity.getConnected().isEmpty(), "Connections were not updated");
                helper.assertFalse(isTicking(helper, POS), "Cable is still ticking after its work was done");
            });
        });
    }

    @GameTest(template = TEMPLATE_EMPTY, batch = "cableticking_pulse")
    public void testCableTickingIdleWakesForRedstonePulse(GameTestHelper helper) {
        helper.setBlock(PULSE_POS.west().below(), Blocks.STONE);
        helper.setBlock(PULSE_POS.west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(PULSE_POS, RegistryEntries.BLOCK_CABLE.value());

        helper.runAfterDelay(20, () -> {
            helper.assertFalse(isTicking(helper, PULSE_POS), "Idle cable is still ticking");

            // Pulse resets are handled by the ticker of the cable, so this only works if the cable woke up
            PartHelpers.addPart(helper.getLevel(), helper.absolutePos(PULSE_POS), Direction.EAST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
            PartHelpers.addPart(helper.getLevel(), helper.absolutePos(PULSE_POS), Direction.WEST, PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));
            PartPos partPos = PartPos.of(helper.getLevel(), helper.absolutePos(PULSE_POS), Direction.WEST);
            ItemStack variableClock = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(PULSE_POS), Direction.EAST), Aspects.Read.Redstone.BOOLEAN_CLOCK);
            placeVariableInWriter(helper, partPos, Aspects.Write.Redstone.BOOLEAN_PULSE, variableClock);
            setAspectProperty(partPos, Aspects.Write.Redstone.BOOLEAN_PULSE,
                    AspectWriteBuilders.Redstone.PROP_PULSE_LENGTH, ValueTypeInteger.ValueInteger.of(10));
            helper.assertTrue(isTicking(helper, PULSE_POS), "Cable did not start ticking when parts were added");

            helper.startSequence()
                    .thenWaitUntil(() -> helper.assertBlockProperty(PULSE_POS.west(), RedStoneWireBlock.POWER, 0))
                    .thenWaitUntil(() -> helper.assertBlockProperty(PULSE_POS.west(), RedStoneWireBlock.POWER, 15))
                    .thenWaitUntil(() -> helper.assertBlockProperty(PULSE_POS.west(), RedStoneWireBlock.POWER, 0))
                    .thenExecute(() -> helper.assertTrue(isTicking(helper, PULSE_POS), "Cable with parts stopped ticking"))
                    .thenSucceed();
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCableTickingIdleWakesForDisplayPanel(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.REDSTONE_BLOCK);
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        helper.runAfterDelay(20, () -> {
            helper.assertFalse(isTicking(helper, POS), "Idle cable is still ticking");
            helper.assertFalse(isTicking(helper, POS.east()), "Idle cable is still ticking");

            // Read the redstone signal, and show it in a display panel on the other idle cable
            PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
            PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST, PartTypes.DISPLAY_PANEL, new ItemStack(PartTypes.DISPLAY_PANEL.getItem()));
            ItemStack variable = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Redstone.INTEGER_VALUE);
            PartTypePanelDisplay.State state = placeVariableInDisplayPanel(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST), variable).getRight();

            helper.succeedWhen(() -> {
                helper.assertTrue(isTicking(helper, POS), "Cable with reader is not ticking");
                helper.assertTrue(isTicking(helper, POS.east()), "Cable with display panel is not ticking");
                helper.assertTrue(Objects.equals(state.getDisplayValue(), ValueTypeInteger.ValueInteger.of(15)), "Display panel value is incorrect");
            });
        });
    }

}
