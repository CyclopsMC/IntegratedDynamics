package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.path.SidedPathElementParams;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.network.NetworkParams;
import org.cyclops.integrateddynamics.core.persist.world.NetworkWorldStorage;

import java.util.List;
import java.util.Optional;

public class GameTestsNetworkSnapshot {
    @GameTest(template = GameTestsNetwork.TEMPLATE_EMPTY)
    public void testRepeatedSaveReplacesSnapshot(GameTestHelper helper) {
        BlockPos pos = BlockPos.ZERO.offset(1, 1, 1);
        helper.setBlock(pos, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(pos.east(), RegistryEntries.BLOCK_CABLE.value());
        helper.succeedWhen(() -> {
            Network network = (Network) NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(pos), null);
            NetworkParams expected = network.toParams();
            NetworkWorldStorage storage = new NetworkWorldStorage(List.of(new NetworkParams(List.of(), true)));
            storage.addNewNetwork(network);
            for (int i = 0; i < 10; i++) {
                storage.beforeSave();
                helper.assertTrue(storage.getNetworkParams().equals(List.of(expected)), Component.literal("Save retained stale or duplicate networks"));
            }
            storage.removeInvalidatedNetwork(network);
            storage.beforeSave();
            helper.assertTrue(storage.getNetworkParams().isEmpty(), Component.literal("Removed network was resaved"));
        });
    }

    @GameTest(template = GameTestsNetwork.TEMPLATE_EMPTY)
    public void testSaveBeforeDeferredLoad(GameTestHelper helper) {
        SidedPathElementParams path = new SidedPathElementParams(helper.getLevel().dimension().identifier().toString(), helper.absolutePos(BlockPos.ZERO), Optional.of(Direction.EAST));
        NetworkParams pending = new NetworkParams(List.of(path), true);
        Network network = new Network();
        network.fromParams(pending);
        NetworkWorldStorage storage = new NetworkWorldStorage(List.of(pending));
        storage.addNewNetwork(network);
        storage.beforeSave();
        storage.beforeSave();
        helper.assertTrue(storage.getNetworkParams().equals(List.of(pending)), Component.literal("Deferred network data changed before its first tick"));
        helper.succeed();
    }

    @GameTest(template = GameTestsNetwork.TEMPLATE_EMPTY)
    public void testFailedSnapshotPreservesPrevious(GameTestHelper helper) {
        NetworkParams previous = new NetworkParams(List.of(), true);
        NetworkWorldStorage storage = new NetworkWorldStorage(List.of(previous));
        storage.addNewNetwork(new Network() {
            @Override
            public void beforeServerStop() {}
            @Override
            public NetworkParams toParams() { throw new IllegalStateException("Synthetic snapshot failure"); }
        });
        boolean failed = false;
        try {
            storage.beforeSave();
        } catch (IllegalStateException expected) {
            failed = true;
        }
        helper.assertTrue(failed && storage.getNetworkParams().equals(List.of(previous)), Component.literal("Failed snapshot changed previous data"));
        helper.succeed();
    }
}
