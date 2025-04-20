package org.cyclops.integrateddynamics.gametest;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.cyclopscore.client.particle.ParticleBlurData;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;

import java.util.Optional;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsNetwork {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(1, 1, 1);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkSingle(GameTestHelper helper) {
        // Place cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.south().east(), RegistryEntries.BLOCK_CABLE.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            INetwork network4 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south().east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of connected cables are not equal");
            helper.assertTrue(network3 == network4, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST, Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.EAST, Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(Direction.WEST, Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south().east())),
                    Sets.newHashSet(Direction.WEST, Direction.NORTH),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkSingleByPlayer(GameTestHelper helper) {
        // Place cables as player
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.BLOCK_CABLE.value(), 4);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);

        helper.placeAt(player, itemStack, POS.south(), Direction.NORTH);
        helper.placeAt(player, itemStack, POS.south().south(), Direction.NORTH);
        helper.placeAt(player, itemStack, POS.south().east(), Direction.NORTH);
        helper.placeAt(player, itemStack, POS.south().south().east(), Direction.NORTH);

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            INetwork network4 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south().east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of connected cables are not equal");
            helper.assertTrue(network3 == network4, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST, Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.EAST, Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(Direction.WEST, Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south().east())),
                    Sets.newHashSet(Direction.WEST, Direction.NORTH),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkTwo(GameTestHelper helper) {
        // Player two networks with empty space inbetween
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(1, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(3, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(4, 0, 0), RegistryEntries.BLOCK_CABLE.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(1, 0, 0)), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(3, 0, 0)), null);
            INetwork network4 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(4, 0, 0)), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of disconnected cables are equal");
            helper.assertTrue(network3 == network4, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(1, 0, 0))),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(3, 0, 0))),
                    Sets.newHashSet(Direction.EAST),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(4, 0, 0))),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkTwoDisconnectedByWrench(GameTestHelper helper) {
        // Place two networks directly next to each other
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(1, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(2, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(3, 0, 0), RegistryEntries.BLOCK_CABLE.value());

        // And disconnect them using the wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_WRENCH.value());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.setPos(helper.absolutePos(POS.offset(1, 0, 0)).getCenter().add(0.25, -1.5, -0.5));
        helper.getBlockState(POS.offset(1, 0, 0)).useItemOn(itemStack, helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS.offset(1, 0, 0)).getCenter(),
                        Direction.NORTH,
                        helper.absolutePos(POS.offset(1, 0, 0)),
                        false)
        );

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(1, 0, 0)), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(2, 0, 0)), null);
            INetwork network4 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(3, 0, 0)), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of wrench-disconnected cables are equal");
            helper.assertTrue(network3 == network4, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(1, 0, 0))),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(2, 0, 0))),
                    Sets.newHashSet(Direction.EAST),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(3, 0, 0))),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkTwoDisconnectedByWrenchOffhand(GameTestHelper helper) {
        // Place two networks directly next to each other
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(1, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(2, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(3, 0, 0), RegistryEntries.BLOCK_CABLE.value());

        // And disconnect them using the wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_WRENCH.value());
        player.setItemInHand(InteractionHand.OFF_HAND, itemStack);
        player.setPos(helper.absolutePos(POS.offset(1, 0, 0)).getCenter().add(0.25, -1.5, -0.5));
        helper.getBlockState(POS.offset(1, 0, 0)).useItemOn(itemStack, helper.getLevel(), player,
                InteractionHand.OFF_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS.offset(1, 0, 0)).getCenter(),
                        Direction.NORTH,
                        helper.absolutePos(POS.offset(1, 0, 0)),
                        false)
        );

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(1, 0, 0)), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(2, 0, 0)), null);
            INetwork network4 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(3, 0, 0)), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of wrench-disconnected cables are equal");
            helper.assertTrue(network3 == network4, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(1, 0, 0))),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(2, 0, 0))),
                    Sets.newHashSet(Direction.EAST),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(3, 0, 0))),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkTwoDisconnectedAndReconnectedByWrench(GameTestHelper helper) {
        // Place two networks directly next to each other
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(1, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(2, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(3, 0, 0), RegistryEntries.BLOCK_CABLE.value());

        // And disconnect them using the wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_WRENCH.value());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.setPos(helper.absolutePos(POS.offset(1, 0, 0)).getCenter().add(0.25, -1.5, -0.5));
        helper.getBlockState(POS.offset(1, 0, 0)).useItemOn(itemStack, helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS.offset(1, 0, 0)).getCenter(),
                        Direction.NORTH,
                        helper.absolutePos(POS.offset(1, 0, 0)),
                        false)
        );

        // And reconnect them using the wrench
        player.setPos(helper.absolutePos(POS.offset(1, 0, 0)).getCenter().add(0.25, -1.5, 0));
        player.setYRot(90);
        helper.getLevel().sendParticles(new ParticleBlurData(1, 1, 1, 1, 100), player.position().x, player.position().y + player.getEyeHeight(), player.position().z, 10, 0, 0, 0, 0); // TODO: for debugging
        helper.getBlockState(POS.offset(1, 0, 0)).useItemOn(itemStack, helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS.offset(1, 0, 0)).getCenter(),
                        Direction.EAST,
                        helper.absolutePos(POS.offset(1, 0, 0)),
                        false)
        );

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(1, 0, 0)), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(2, 0, 0)), null);
            INetwork network4 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(3, 0, 0)), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of wrench-reconnected cables are equal");
            helper.assertTrue(network3 == network4, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(1, 0, 0))),
                    Sets.newHashSet(Direction.WEST, Direction.EAST),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(2, 0, 0))),
                    Sets.newHashSet(Direction.WEST, Direction.EAST),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(3, 0, 0))),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkTwoRemoved(GameTestHelper helper) {
        // Place two networks directly next to each other
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(1, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(2, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(3, 0, 0), RegistryEntries.BLOCK_CABLE.value());

        // And remove one cable
        helper.destroyBlock(POS.offset(1, 0, 0));

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            Optional<INetwork> network2 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS.offset(1, 0, 0)), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(2, 0, 0)), null);
            INetwork network4 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(3, 0, 0)), null);
            helper.assertTrue(network1 != network3, "Networks of connected cables are not equal");
            helper.assertTrue(network2.isEmpty(), "Network of removed cable is not empty");
            helper.assertTrue(network3 == network4, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(1, 0, 0))),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(2, 0, 0))),
                    Sets.newHashSet(Direction.EAST),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(3, 0, 0))),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );

            helper.assertItemEntityNotPresent(RegistryEntries.ITEM_CABLE.get());
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkTwoRemovedByWrench(GameTestHelper helper) {
        // Place two networks directly next to each other
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(1, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(2, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(3, 0, 0), RegistryEntries.BLOCK_CABLE.value());

        // And remove one cable using the wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true); // To remove cable!
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_WRENCH.value());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.setPos(helper.absolutePos(POS.offset(1, 0, 0)).getCenter().add(0.25, -1.5, -0.5));
        helper.getBlockState(POS.offset(1, 0, 0)).useItemOn(itemStack, helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS.offset(1, 0, 0)).getCenter(),
                        Direction.NORTH,
                        helper.absolutePos(POS.offset(1, 0, 0)),
                        false)
        );

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            Optional<INetwork> network2 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS.offset(1, 0, 0)), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(2, 0, 0)), null);
            INetwork network4 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(3, 0, 0)), null);
            helper.assertTrue(network1 != network3, "Networks of connected cables are not equal");
            helper.assertTrue(network2.isEmpty(), "Network of removed cable is not empty");
            helper.assertTrue(network3 == network4, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(1, 0, 0))),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(2, 0, 0))),
                    Sets.newHashSet(Direction.EAST),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(3, 0, 0))),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );

            helper.assertItemEntityPresent(RegistryEntries.ITEM_CABLE.get());
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkTwoRemovedByWrenchOffand(GameTestHelper helper) {
        // Place two networks directly next to each other
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(1, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(2, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(3, 0, 0), RegistryEntries.BLOCK_CABLE.value());

        // And remove one cable using the wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true); // To remove cable!
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_WRENCH.value());
        player.setItemInHand(InteractionHand.OFF_HAND, itemStack);
        player.setPos(helper.absolutePos(POS.offset(1, 0, 0)).getCenter().add(0.25, -1.5, -0.5));
        helper.getBlockState(POS.offset(1, 0, 0)).useItemOn(itemStack, helper.getLevel(), player,
                InteractionHand.OFF_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS.offset(1, 0, 0)).getCenter(),
                        Direction.NORTH,
                        helper.absolutePos(POS.offset(1, 0, 0)),
                        false)
        );

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            Optional<INetwork> network2 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS.offset(1, 0, 0)), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(2, 0, 0)), null);
            INetwork network4 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(3, 0, 0)), null);
            helper.assertTrue(network1 != network3, "Networks of connected cables are not equal");
            helper.assertTrue(network2.isEmpty(), "Network of removed cable is not empty");
            helper.assertTrue(network3 == network4, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(1, 0, 0))),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(2, 0, 0))),
                    Sets.newHashSet(Direction.EAST),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.offset(3, 0, 0))),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );

            helper.assertItemEntityPresent(RegistryEntries.ITEM_CABLE.get());
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkCableWithLever(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS.offset(1, 0, 1), RegistryEntries.BLOCK_CABLE.value());

        // Attempt to place lever as player
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(Items.LEVER);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        helper.placeAt(player, itemStack, POS.offset(1, 0, 1).south(), Direction.NORTH);

        helper.succeedWhen(() -> {
            helper.assertValueEqual(player.getItemInHand(InteractionHand.MAIN_HAND).getItem(), Items.LEVER, "Item hand is incorrect");
            helper.assertBlockNotPresent(Blocks.LEVER, POS.offset(1, 0, 1).north());
        });
    }

    /* ----- Variable store ----- */

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkVariableStoreSingle(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_VARIABLE_STORE.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST, Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkVariableStoreSingleAfterwards(GameTestHelper helper) {
        // Place cables directly
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place variable store afterwards
        helper.setBlock(POS, RegistryEntries.BLOCK_VARIABLE_STORE.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST, Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkVariableStoreDisconnectByWrench(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_VARIABLE_STORE.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // And disconnect them using the wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_WRENCH.value());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.setPos(helper.absolutePos(POS.east()).getCenter().add(-0.25, -1.5, -0.5));
        helper.getBlockState(POS.east()).useItemOn(itemStack, helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS.east()).getCenter(),
                        Direction.NORTH,
                        helper.absolutePos(POS.east()),
                        false)
        );

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkVariableStoreBreakByWrench(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_VARIABLE_STORE.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Break variable store with wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true); // To remove store!
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RegistryEntries.ITEM_WRENCH.value()));
        player.setPos(helper.absolutePos(POS).getCenter());
        helper.getBlockState(POS).useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND), helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS).getCenter(),
                        Direction.SOUTH,
                        helper.absolutePos(POS),
                        false)
        );

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_VARIABLE_STORE.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );

            helper.assertItemEntityPresent(RegistryEntries.BLOCK_VARIABLE_STORE.get().asItem());
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkVariableStoreBreakByPickaxe(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_VARIABLE_STORE.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Break variable store with wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        player.setPos(helper.absolutePos(POS).getCenter());
        helper.getBlockState(POS).onDestroyedByPlayer(helper.getLevel(), helper.absolutePos(POS), player, true, helper.getLevel().getFluidState(helper.absolutePos(POS)));

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_VARIABLE_STORE.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkVariableStoreBreakByTnt(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_VARIABLE_STORE.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Destroy with TNT
        helper.setBlock(POS.above(), Blocks.TNT);
        helper.setBlock(POS.above().above(), Blocks.REDSTONE_BLOCK);

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_VARIABLE_STORE.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );

            helper.assertItemEntityPresent(RegistryEntries.BLOCK_VARIABLE_STORE.get().asItem());
        });
    }

    /* ----- Coal Generator ----- */

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkCoalGeneratorSingle(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_COAL_GENERATOR.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST, Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkCoalGeneratorSingleAfterwards(GameTestHelper helper) {
        // Place cables directly
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place variable store afterwards
        helper.setBlock(POS, RegistryEntries.BLOCK_COAL_GENERATOR.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST, Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkCoalGeneratorDisconnectByWrench(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_COAL_GENERATOR.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // And disconnect them using the wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_WRENCH.value());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.setPos(helper.absolutePos(POS.east()).getCenter().add(-0.25, -1.5, -0.5));
        helper.getBlockState(POS.east()).useItemOn(itemStack, helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS.east()).getCenter(),
                        Direction.NORTH,
                        helper.absolutePos(POS.east()),
                        false)
        );

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkCoalGeneratorBreakByWrench(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_COAL_GENERATOR.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Break variable store with wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true); // To remove store!
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RegistryEntries.ITEM_WRENCH.value()));
        player.setPos(helper.absolutePos(POS).getCenter());
        helper.getBlockState(POS).useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND), helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS).getCenter(),
                        Direction.SOUTH,
                        helper.absolutePos(POS),
                        false)
        );

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_VARIABLE_STORE.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );

            helper.assertItemEntityPresent(RegistryEntries.BLOCK_COAL_GENERATOR.get().asItem());
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkCoalGeneratorBreakByPickaxe(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_COAL_GENERATOR.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Break variable store with wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        player.setPos(helper.absolutePos(POS).getCenter());
        helper.getBlockState(POS).onDestroyedByPlayer(helper.getLevel(), helper.absolutePos(POS), player, true, helper.getLevel().getFluidState(helper.absolutePos(POS)));

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_VARIABLE_STORE.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkCoalGeneratorBreakByTnt(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_COAL_GENERATOR.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Destroy with TNT
        helper.setBlock(POS.above(), Blocks.TNT);
        helper.setBlock(POS.above().above(), Blocks.REDSTONE_BLOCK);

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_COAL_GENERATOR.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );

            helper.assertItemEntityPresent(RegistryEntries.BLOCK_COAL_GENERATOR.get().asItem());
        });
    }

    /* ----- Energy Battery ----- */

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkEnergyBatterySingle(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_ENERGY_BATTERY.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST, Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkEnergyBatterySingleAfterwards(GameTestHelper helper) {
        // Place cables directly
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place variable store afterwards
        helper.setBlock(POS, RegistryEntries.BLOCK_ENERGY_BATTERY.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST, Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkEnergyBatteryDisconnectByWrench(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_ENERGY_BATTERY.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // And disconnect them using the wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_WRENCH.value());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.setPos(helper.absolutePos(POS.east()).getCenter().add(-0.25, -1.5, -0.5));
        helper.getBlockState(POS.east()).useItemOn(itemStack, helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS.east()).getCenter(),
                        Direction.NORTH,
                        helper.absolutePos(POS.east()),
                        false)
        );

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkEnergyBatteryBreakByWrench(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_ENERGY_BATTERY.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Break variable store with wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true); // To remove store!
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RegistryEntries.ITEM_WRENCH.value()));
        player.setPos(helper.absolutePos(POS).getCenter());
        helper.getBlockState(POS).useItemOn(new ItemStack(RegistryEntries.ITEM_WRENCH.value()), helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS).getCenter(),
                        Direction.SOUTH,
                        helper.absolutePos(POS),
                        false)
        );

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_ENERGY_BATTERY.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );

            helper.assertItemEntityPresent(RegistryEntries.BLOCK_ENERGY_BATTERY.get().asItem());
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkEnergyBatteryBreakByPickaxe(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_ENERGY_BATTERY.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Break variable store with wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        player.setPos(helper.absolutePos(POS).getCenter());
        helper.getBlockState(POS).onDestroyedByPlayer(helper.getLevel(), helper.absolutePos(POS), player, true, helper.getLevel().getFluidState(helper.absolutePos(POS)));

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_VARIABLE_STORE.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkEnergyBatteryBreakByTnt(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_ENERGY_BATTERY.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Destroy with TNT
        helper.setBlock(POS.above(), Blocks.TNT);
        helper.setBlock(POS.above().above(), Blocks.REDSTONE_BLOCK);

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_VARIABLE_STORE.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );

            helper.assertItemEntityPresent(RegistryEntries.BLOCK_ENERGY_BATTERY.get().asItem());
        });
    }

    /* ----- Delayer ----- */

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkDelayerSingle(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_DELAY.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST, Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkDelayerSingleAfterwards(GameTestHelper helper) {
        // Place cables directly
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place variable store afterwards
        helper.setBlock(POS, RegistryEntries.BLOCK_DELAY.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST, Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkDelayerDisconnectByWrench(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_DELAY.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // And disconnect them using the wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_WRENCH.value());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.setPos(helper.absolutePos(POS.east()).getCenter().add(-0.25, -1.5, -0.5));
        helper.getBlockState(POS.east()).useItemOn(itemStack, helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS.east()).getCenter(),
                        Direction.NORTH,
                        helper.absolutePos(POS.east()),
                        false)
        );

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkDelayerBreakByWrench(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_DELAY.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Break variable store with wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true); // To remove store!
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RegistryEntries.ITEM_WRENCH.value()));
        player.setPos(helper.absolutePos(POS).getCenter());
        helper.getBlockState(POS).useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND), helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS).getCenter(),
                        Direction.SOUTH,
                        helper.absolutePos(POS),
                        false)
        );

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_DELAY.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );

            helper.assertItemEntityPresent(RegistryEntries.BLOCK_DELAY.get().asItem());
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkDelayerBreakByPickaxe(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_DELAY.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Break variable store with wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        player.setPos(helper.absolutePos(POS).getCenter());
        helper.getBlockState(POS).onDestroyedByPlayer(helper.getLevel(), helper.absolutePos(POS), player, true, helper.getLevel().getFluidState(helper.absolutePos(POS)));

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_DELAY.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkDelayerBreakByTnt(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_DELAY.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Destroy with TNT
        helper.setBlock(POS.above(), Blocks.TNT);
        helper.setBlock(POS.above().above(), Blocks.REDSTONE_BLOCK);

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_VARIABLE_STORE.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );

            helper.assertItemEntityPresent(RegistryEntries.BLOCK_DELAY.get().asItem());
        });
    }

    /* ----- Materializer ----- */

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkMaterializerSingle(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_MATERIALIZER.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST, Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkMaterializerSingleAfterwards(GameTestHelper helper) {
        // Place cables directly
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place variable store afterwards
        helper.setBlock(POS, RegistryEntries.BLOCK_MATERIALIZER.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST, Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkMaterializerDisconnectByWrench(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_MATERIALIZER.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // And disconnect them using the wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_WRENCH.value());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.setPos(helper.absolutePos(POS.east()).getCenter().add(-0.25, -1.5, -0.5));
        helper.getBlockState(POS.east()).useItemOn(itemStack, helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS.east()).getCenter(),
                        Direction.NORTH,
                        helper.absolutePos(POS.east()),
                        false)
        );

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkMaterializerBreakByWrench(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_MATERIALIZER.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Break variable store with wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true); // To remove store!
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RegistryEntries.ITEM_WRENCH.value()));
        player.setPos(helper.absolutePos(POS).getCenter());
        helper.getBlockState(POS).useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND), helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS).getCenter(),
                        Direction.SOUTH,
                        helper.absolutePos(POS),
                        false)
        );

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_MATERIALIZER.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );

            helper.assertItemEntityPresent(RegistryEntries.BLOCK_MATERIALIZER.get().asItem());
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkMaterializerBreakByPickaxe(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_MATERIALIZER.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Break variable store with wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        player.setPos(helper.absolutePos(POS).getCenter());
        helper.getBlockState(POS).onDestroyedByPlayer(helper.getLevel(), helper.absolutePos(POS), player, true, helper.getLevel().getFluidState(helper.absolutePos(POS)));

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_MATERIALIZER.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkMaterializerBreakByTnt(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_MATERIALIZER.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Destroy with TNT
        helper.setBlock(POS.above(), Blocks.TNT);
        helper.setBlock(POS.above().above(), Blocks.REDSTONE_BLOCK);

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_MATERIALIZER.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );

            helper.assertItemEntityPresent(RegistryEntries.BLOCK_MATERIALIZER.get().asItem());
        });
    }

    /* ----- Proxy ----- */

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkProxySingle(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_PROXY.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST, Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkProxySingleAfterwards(GameTestHelper helper) {
        // Place cables directly
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place variable store afterwards
        helper.setBlock(POS, RegistryEntries.BLOCK_PROXY.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST, Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(Direction.WEST),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkProxyDisconnectByWrench(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_PROXY.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // And disconnect them using the wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_WRENCH.value());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.setPos(helper.absolutePos(POS.east()).getCenter().add(-0.25, -1.5, -0.5));
        helper.getBlockState(POS.east()).useItemOn(itemStack, helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS.east()).getCenter(),
                        Direction.NORTH,
                        helper.absolutePos(POS.east()),
                        false)
        );

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.SOUTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.NORTH),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkProxyBreakByWrench(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_PROXY.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Break variable store with wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true); // To remove store!
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RegistryEntries.ITEM_WRENCH.value()));
        player.setPos(helper.absolutePos(POS).getCenter());
        helper.getBlockState(POS).useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND), helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        helper.absolutePos(POS).getCenter(),
                        Direction.SOUTH,
                        helper.absolutePos(POS),
                        false)
        );

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_PROXY.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );

            helper.assertItemEntityPresent(RegistryEntries.BLOCK_PROXY.get().asItem());
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkProxyBreakByPickaxe(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_PROXY.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Break variable store with wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        player.setPos(helper.absolutePos(POS).getCenter());
        helper.getBlockState(POS).onDestroyedByPlayer(helper.getLevel(), helper.absolutePos(POS), player, true, helper.getLevel().getFluidState(helper.absolutePos(POS)));

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_PROXY.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkProxyBreakByTnt(GameTestHelper helper) {
        // Place variable store and cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_PROXY.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Destroy with TNT
        helper.setBlock(POS.above(), Blocks.TNT);
        helper.setBlock(POS.above().above(), Blocks.REDSTONE_BLOCK);

        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_PROXY.value(), POS);

            Optional<INetwork> network1 = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null);
            helper.assertTrue(network1.isEmpty(), "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );

            helper.assertItemEntityPresent(RegistryEntries.BLOCK_PROXY.get().asItem());
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkMonodirectional(GameTestHelper helper) {
        // Place cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.south().south().south(), RegistryEntries.BLOCK_CABLE.value());

        // Place monodirectional connectors
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH, PartTypes.CONNECTOR_MONO, new ItemStack(PartTypes.CONNECTOR_MONO.getItem()));
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.south().south().south()), Direction.NORTH, PartTypes.CONNECTOR_MONO, new ItemStack(PartTypes.CONNECTOR_MONO.getItem()));

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south().south().south()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south().south().south())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkMonodirectionalUnconnected(GameTestHelper helper) {
        // Place cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.south().south().south().east(), RegistryEntries.BLOCK_CABLE.value());

        // Place monodirectional connectors
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH, PartTypes.CONNECTOR_MONO, new ItemStack(PartTypes.CONNECTOR_MONO.getItem()));
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.south().south().south().east()), Direction.NORTH, PartTypes.CONNECTOR_MONO, new ItemStack(PartTypes.CONNECTOR_MONO.getItem()));

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south().south().south().east()), null);
            helper.assertFalse(network1 == network2, "Networks of connected cables are equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south().south().south())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkOmnidirectional(GameTestHelper helper) {
        // Place cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.south().south().south().east(), RegistryEntries.BLOCK_CABLE.value());

        // Place omnidirectional connectors
        ItemStack omniDirectionalConnector = new ItemStack(PartTypes.CONNECTOR_OMNI.getItem());
        omniDirectionalConnector.set(RegistryEntries.DATACOMPONENT_OMNIDIRECTIONAL_GROUP, 1);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH, PartTypes.CONNECTOR_OMNI, omniDirectionalConnector.copy());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.south().south().south().east()), Direction.NORTH, PartTypes.CONNECTOR_OMNI, omniDirectionalConnector.copy());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south().south().south().east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south().south().south())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkOmnidirectionalUnconnected(GameTestHelper helper) {
        // Place cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.south().south().south().east(), RegistryEntries.BLOCK_CABLE.value());

        // Place omnidirectional connectors
        ItemStack omniDirectionalConnector1 = new ItemStack(PartTypes.CONNECTOR_OMNI.getItem());
        omniDirectionalConnector1.set(RegistryEntries.DATACOMPONENT_OMNIDIRECTIONAL_GROUP, 1);
        ItemStack omniDirectionalConnector2 = new ItemStack(PartTypes.CONNECTOR_OMNI.getItem());
        omniDirectionalConnector2.set(RegistryEntries.DATACOMPONENT_OMNIDIRECTIONAL_GROUP, 2);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH, PartTypes.CONNECTOR_OMNI, omniDirectionalConnector1.copy());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.south().south().south().east()), Direction.NORTH, PartTypes.CONNECTOR_OMNI, omniDirectionalConnector2.copy());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south().south().south().east()), null);
            helper.assertFalse(network1 == network2, "Networks of connected cables are equal");

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south().south().south())),
                    Sets.newHashSet(),
                    "Connected cables are invalid"
            );
        });
    }

}
