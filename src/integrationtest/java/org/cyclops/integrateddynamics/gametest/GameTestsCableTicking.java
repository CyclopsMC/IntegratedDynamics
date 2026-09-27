package org.cyclops.integrateddynamics.gametest;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsCableTicking {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(1, 1, 1);

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

}
