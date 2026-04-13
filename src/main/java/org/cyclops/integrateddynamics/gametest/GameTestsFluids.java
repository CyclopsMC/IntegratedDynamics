package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Game tests for fluid block interaction (bucket pickup, block replacement).
 * @author rubensworks
 */
public class GameTestsFluids {

    public static final String TEMPLATE_EMPTY = "integrateddynamics:empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMenrilResinFluidReplaceable(GameTestHelper helper) {
        // Place menril resin fluid block
        helper.setBlock(POS, RegistryEntries.BLOCK_FLUID_MENRIL_RESIN.value().defaultBlockState());
        helper.assertTrue(
                helper.getBlockState(POS).getBlock() == RegistryEntries.BLOCK_FLUID_MENRIL_RESIN.value(),
                Component.literal("Menril resin fluid block was not placed"));

        // Place a solid block where the fluid is using a player with an ItemStack,
        // which triggers the replaceable check
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack dirtStack = new ItemStack(Items.DIRT);
        player.setItemInHand(InteractionHand.MAIN_HAND, dirtStack);
        helper.placeAt(player, dirtStack, POS.south(), Direction.NORTH);

        // Verify the fluid was replaced by the placed block
        helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.DIRT, POS));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMenrilResinFluidBucketPickup(GameTestHelper helper) {
        // Place menril resin fluid block (source level = 0)
        BlockState fluidState = RegistryEntries.BLOCK_FLUID_MENRIL_RESIN.value().defaultBlockState();
        helper.setBlock(POS, fluidState);
        helper.assertTrue(
                helper.getBlockState(POS).getBlock() == RegistryEntries.BLOCK_FLUID_MENRIL_RESIN.value(),
                Component.literal("Menril resin fluid block was not placed"));

        // Simulate bucket pickup using the BucketPickup interface
        Level level = helper.getLevel();
        BlockPos absPos = helper.absolutePos(POS);
        BlockState state = level.getBlockState(absPos);
        helper.assertTrue(
                state.getBlock() instanceof BucketPickup,
                Component.literal("Block does not implement BucketPickup"));

        ItemStack result = ((BucketPickup) state.getBlock()).pickupBlock(null, level, absPos, state);

        helper.succeedWhen(() -> {
            helper.assertFalse(result.isEmpty(), Component.literal("Bucket pickup returned an empty ItemStack"));
            helper.assertTrue(
                    result.getItem() == RegistryEntries.ITEM_BUCKET_MENRIL_RESIN.get(),
                    Component.literal("Bucket pickup returned wrong item: " + result));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testLiquidChorusFluidReplaceable(GameTestHelper helper) {
        // Place liquid chorus fluid block
        helper.setBlock(POS, RegistryEntries.BLOCK_FLUID_LIQUID_CHORUS.value().defaultBlockState());
        helper.assertTrue(
                helper.getBlockState(POS).getBlock() == RegistryEntries.BLOCK_FLUID_LIQUID_CHORUS.value(),
                Component.literal("Liquid chorus fluid block was not placed"));

        // Place a solid block where the fluid is using a player with an ItemStack,
        // which triggers the replaceable check
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack dirtStack = new ItemStack(Items.DIRT);
        player.setItemInHand(InteractionHand.MAIN_HAND, dirtStack);
        helper.placeAt(player, dirtStack, POS.south(), Direction.NORTH);

        // Verify the fluid was replaced by the placed block
        helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.DIRT, POS));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testLiquidChorusFluidBucketPickup(GameTestHelper helper) {
        // Place liquid chorus fluid block (source level = 0)
        BlockState fluidState = RegistryEntries.BLOCK_FLUID_LIQUID_CHORUS.value().defaultBlockState();
        helper.setBlock(POS, fluidState);
        helper.assertTrue(
                helper.getBlockState(POS).getBlock() == RegistryEntries.BLOCK_FLUID_LIQUID_CHORUS.value(),
                Component.literal("Liquid chorus fluid block was not placed"));

        // Simulate bucket pickup using the BucketPickup interface
        Level level = helper.getLevel();
        BlockPos absPos = helper.absolutePos(POS);
        BlockState state = level.getBlockState(absPos);
        helper.assertTrue(
                state.getBlock() instanceof BucketPickup,
                Component.literal("Block does not implement BucketPickup"));

        ItemStack result = ((BucketPickup) state.getBlock()).pickupBlock(null, level, absPos, state);

        helper.succeedWhen(() -> {
            helper.assertFalse(result.isEmpty(), Component.literal("Bucket pickup returned an empty ItemStack"));
            helper.assertTrue(
                    result.getItem() == RegistryEntries.ITEM_BUCKET_LIQUID_CHORUS.get(),
                    Component.literal("Bucket pickup returned wrong item: " + result));
        });
    }

}
