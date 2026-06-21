package org.cyclops.integrateddynamics.gametest;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;

public class GameTestsFacades {

    public static final String TEMPLATE_EMPTY = "integrateddynamics:empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(1, 1, 1);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFacadeCable(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place facade as player
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStackFacade = new ItemStack(RegistryEntries.ITEM_FACADE.value());
        RegistryEntries.ITEM_FACADE.value().writeFacadeBlock(itemStackFacade, Blocks.COPPER_BLOCK.weathering().exposed().defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStackFacade);
        helper.placeAt(player, itemStackFacade, POS.south(), Direction.NORTH);

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            helper.assertTrue(network1 != null, Component.literal("Network is null"));
            helper.assertTrue(CableHelpers.hasFacade(helper.getLevel(), helper.absolutePos(POS), helper.getBlockState(POS)), Component.literal("Facade is not present"));
            helper.assertValueEqual(CableHelpers.getFacade(helper.getLevel(), helper.absolutePos(POS), helper.getBlockState(POS)).get(), Blocks.COPPER_BLOCK.weathering().exposed().defaultBlockState(), Component.literal("Facade type is incorrect"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFacadeCablesConnect(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place facade as player
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStackFacade = new ItemStack(RegistryEntries.ITEM_FACADE.value());
        RegistryEntries.ITEM_FACADE.value().writeFacadeBlock(itemStackFacade, Blocks.COPPER_BLOCK.weathering().exposed().defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStackFacade);
        helper.placeAt(player, itemStackFacade, POS.south(), Direction.NORTH);

        // Place cables around
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.south().east(), RegistryEntries.BLOCK_CABLE.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            INetwork network4 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south().east()), null);
            helper.assertTrue(network1 == network2, Component.literal("Networks of connected cables are not equal"));
            helper.assertTrue(network2 == network3, Component.literal("Networks of connected cables are not equal"));
            helper.assertTrue(network3 == network4, Component.literal("Networks of connected cables are not equal"));

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(Direction.EAST, Direction.SOUTH),
                    Component.literal("Connected cables are invalid")
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south())),
                    Sets.newHashSet(Direction.EAST, Direction.NORTH),
                    Component.literal("Connected cables are invalid")
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.east())),
                    Sets.newHashSet(Direction.WEST, Direction.SOUTH),
                    Component.literal("Connected cables are invalid")
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.south().east())),
                    Sets.newHashSet(Direction.WEST, Direction.NORTH),
                    Component.literal("Connected cables are invalid")
            );

            helper.assertTrue(CableHelpers.hasFacade(helper.getLevel(), helper.absolutePos(POS), helper.getBlockState(POS)), Component.literal("Facade is not present"));
            helper.assertValueEqual(CableHelpers.getFacade(helper.getLevel(), helper.absolutePos(POS), helper.getBlockState(POS)).get(), Blocks.COPPER_BLOCK.weathering().exposed().defaultBlockState(), Component.literal("Facade type is incorrect"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFacadeCableWithPart(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Place facade as player
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStackFacade = new ItemStack(RegistryEntries.ITEM_FACADE.value());
        RegistryEntries.ITEM_FACADE.value().writeFacadeBlock(itemStackFacade, Blocks.COPPER_BLOCK.weathering().exposed().defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStackFacade);
        helper.placeAt(player, itemStackFacade, POS.south(), Direction.NORTH);

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            helper.assertTrue(network1 != null, Component.literal("Network is null"));
            helper.assertTrue(CableHelpers.hasFacade(helper.getLevel(), helper.absolutePos(POS), helper.getBlockState(POS)), Component.literal("Facade is not present"));
            helper.assertValueEqual(CableHelpers.getFacade(helper.getLevel(), helper.absolutePos(POS), helper.getBlockState(POS)).get(), Blocks.COPPER_BLOCK.weathering().exposed().defaultBlockState(), Component.literal("Facade type is incorrect"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFacadeCableWithLever(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place facade as player
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStackFacade = new ItemStack(RegistryEntries.ITEM_FACADE.value());
        RegistryEntries.ITEM_FACADE.value().writeFacadeBlock(itemStackFacade, Blocks.COPPER_BLOCK.weathering().exposed().defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStackFacade);
        helper.placeAt(player, itemStackFacade, POS.south(), Direction.NORTH);

        // Attempt to place lever as player
        ItemStack itemStack = new ItemStack(Items.LEVER);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        helper.placeAt(player, itemStack, POS.south(), Direction.NORTH);

        helper.succeedWhen(() -> {
            helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty(), Component.literal("Item hand is not empty"));
            helper.assertBlockPresent(Blocks.LEVER, POS.north());
        });
    }

}
