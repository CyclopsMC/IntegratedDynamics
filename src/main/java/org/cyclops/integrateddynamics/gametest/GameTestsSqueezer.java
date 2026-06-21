package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntitySqueezer;

public class GameTestsSqueezer {

    public static final String TEMPLATE_EMPTY = "integrateddynamics:empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testSqueezer(GameTestHelper helper) {
        // Place machine
        helper.setBlock(POS, RegistryEntries.BLOCK_SQUEEZER.value());
        BlockEntitySqueezer machine = helper.getBlockEntity(POS, BlockEntitySqueezer.class);

        // Set input
        machine.getInventory().setItem(0, new ItemStack(RegistryEntries.BLOCK_MENRIL_LOG.get()));

        // Let mob fall on the squeezer
        Wrapper<Goat> entity = new Wrapper<>(helper.spawnWithNoFreeWill(EntityTypes.GOAT, POS.offset(0, 8, 0)));
        helper.runAfterDelay(20, () -> {
            entity.get().die(helper.getLevel().damageSources().generic());
            entity.set(helper.spawnWithNoFreeWill(EntityTypes.GOAT, POS.offset(0, 8, 0)));
        });

        helper.succeedWhen(() -> {
            helper.assertTrue(machine.getInventory().getItem(0).isEmpty(), Component.literal("Machine did not consume input"));
            helper.assertValueEqual(machine.getTank().getFluid().getFluid(), RegistryEntries.FLUID_MENRIL_RESIN.get(), Component.literal("Machine did not produce fluid output"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testSqueezerPlaceSingleItem(GameTestHelper helper) {
        // Place machine
        helper.setBlock(POS, RegistryEntries.BLOCK_SQUEEZER.value());
        BlockEntitySqueezer machine = helper.getBlockEntity(POS, BlockEntitySqueezer.class);

        // Player right-clicks empty squeezer with a single item
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().setItem(player.getInventory().getSelectedSlot(), new ItemStack(Items.DIRT, 1));

        BlockState blockState = helper.getLevel().getBlockState(helper.absolutePos(POS));
        blockState.useWithoutItem(helper.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(POS)), Direction.UP, helper.absolutePos(POS), false));

        helper.succeedWhen(() -> {
            helper.assertValueEqual(machine.getInventory().getItem(0).getItem(), Items.DIRT, Component.literal("Squeezer did not receive item"));
            helper.assertTrue(player.getInventory().getSelectedItem().isEmpty(), Component.literal("Player hand should be empty after placing single item"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testSqueezerPlaceStackItem(GameTestHelper helper) {
        // Place machine
        helper.setBlock(POS, RegistryEntries.BLOCK_SQUEEZER.value());
        BlockEntitySqueezer machine = helper.getBlockEntity(POS, BlockEntitySqueezer.class);

        // Player right-clicks empty squeezer with a stack of 5 items
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().setItem(player.getInventory().getSelectedSlot(), new ItemStack(Items.DIRT, 5));

        BlockState blockState = helper.getLevel().getBlockState(helper.absolutePos(POS));
        blockState.useWithoutItem(helper.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(POS)), Direction.UP, helper.absolutePos(POS), false));

        helper.succeedWhen(() -> {
            helper.assertValueEqual(machine.getInventory().getItem(0).getItem(), Items.DIRT, Component.literal("Squeezer did not receive item from stack"));
            helper.assertValueEqual(player.getInventory().getSelectedItem().getCount(), 4, Component.literal("Player stack should be decremented by 1 after placing item"));
        });
    }

}
