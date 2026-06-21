package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityDryingBasin;

public class GameTestsDryingBasin {

    public static final String TEMPLATE_EMPTY = "integrateddynamics:empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 200)
    public void testDryingBasin(GameTestHelper helper) {
        // Place machine
        helper.setBlock(POS, RegistryEntries.BLOCK_DRYING_BASIN.value());
        BlockEntityDryingBasin machine = helper.getBlockEntity(POS, BlockEntityDryingBasin.class);

        // Set input
        machine.getTank().setFluid(new FluidStack(RegistryEntries.FLUID_MENRIL_RESIN.get(), 1_000));

        helper.succeedWhen(() -> {
            helper.assertValueEqual(machine.getInventory().getItem(0).getItem(), RegistryEntries.BLOCK_CRYSTALIZED_MENRIL_BLOCK.get().asItem(), Component.literal("Machine did not produce item output"));
            helper.assertTrue(machine.getTank().getFluid().isEmpty(), Component.literal("Machine did not consume fluid input"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 200)
    public void testDryingBasinNoFluidInput(GameTestHelper helper) {
        // Place machine
        helper.setBlock(POS, RegistryEntries.BLOCK_DRYING_BASIN.value());
        BlockEntityDryingBasin machine = helper.getBlockEntity(POS, BlockEntityDryingBasin.class);

        // Set input: rotten flesh dries into leather (no fluid input required)
        machine.getInventory().setItem(0, new ItemStack(Items.ROTTEN_FLESH));

        helper.succeedWhen(() -> {
            helper.assertValueEqual(machine.getInventory().getItem(0).getItem(), Items.LEATHER, Component.literal("Machine did not produce leather from rotten flesh"));
            helper.assertTrue(machine.getTank().getFluid().isEmpty(), Component.literal("Machine unexpectedly produced fluid output"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 200)
    public void testDryingBasinItemAndFluidInputs(GameTestHelper helper) {
        // Place machine
        helper.setBlock(POS, RegistryEntries.BLOCK_DRYING_BASIN.value());
        BlockEntityDryingBasin machine = helper.getBlockEntity(POS, BlockEntityDryingBasin.class);

        // Set both item and fluid inputs simultaneously
        machine.getInventory().setItem(0, new ItemStack(Items.ROTTEN_FLESH));
        machine.getTank().setFluid(new FluidStack(RegistryEntries.FLUID_MENRIL_RESIN.get(), 1_000));

        helper.succeedWhen(() -> {
            // The item-only recipe (rotten_flesh -> leather) should run despite fluid being present.
            // The fluid recipe cannot run afterwards because leather (the output) occupies slot 0.
            helper.assertValueEqual(machine.getInventory().getItem(0).getItem(), Items.LEATHER, Component.literal("Machine did not process item-only recipe (rotten flesh -> leather)"));
            helper.assertValueEqual(machine.getTank().getFluid().getAmount(), 1_000, Component.literal("Menril resin should still be in tank (fluid recipe cannot run while leather is in slot 0)"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDryingBasinPlaceSingleItem(GameTestHelper helper) {
        // Place machine
        helper.setBlock(POS, RegistryEntries.BLOCK_DRYING_BASIN.value());
        BlockEntityDryingBasin machine = helper.getBlockEntity(POS, BlockEntityDryingBasin.class);

        // Player right-clicks empty basin with a single item
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().setItem(player.getInventory().getSelectedSlot(), new ItemStack(Items.DIRT, 1));

        BlockState blockState = helper.getLevel().getBlockState(helper.absolutePos(POS));
        blockState.useWithoutItem(helper.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(POS)), Direction.UP, helper.absolutePos(POS), false));

        helper.succeedWhen(() -> {
            helper.assertValueEqual(machine.getInventory().getItem(0).getItem(), Items.DIRT, Component.literal("Basin did not receive item"));
            helper.assertTrue(player.getInventory().getSelectedItem().isEmpty(), Component.literal("Player hand should be empty after placing single item"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDryingBasinPlaceStackItem(GameTestHelper helper) {
        // Place machine
        helper.setBlock(POS, RegistryEntries.BLOCK_DRYING_BASIN.value());
        BlockEntityDryingBasin machine = helper.getBlockEntity(POS, BlockEntityDryingBasin.class);

        // Player right-clicks empty basin with a stack of 5 items
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().setItem(player.getInventory().getSelectedSlot(), new ItemStack(Items.DIRT, 5));

        BlockState blockState = helper.getLevel().getBlockState(helper.absolutePos(POS));
        blockState.useWithoutItem(helper.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(POS)), Direction.UP, helper.absolutePos(POS), false));

        helper.succeedWhen(() -> {
            helper.assertValueEqual(machine.getInventory().getItem(0).getItem(), Items.DIRT, Component.literal("Basin did not receive item from stack"));
            helper.assertValueEqual(player.getInventory().getSelectedItem().getCount(), 4, Component.literal("Player stack should be decremented by 1 after placing item"));
        });
    }

}
