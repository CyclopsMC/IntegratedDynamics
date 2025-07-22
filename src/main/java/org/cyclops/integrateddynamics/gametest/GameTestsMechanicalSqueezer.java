package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.cyclops.integrateddynamics.blockentity.BlockEntityMechanicalSqueezer;

public class GameTestsMechanicalSqueezer {

    public static final String TEMPLATE_EMPTY = "integrateddynamics:empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMechanicalSqueezer(GameTestHelper helper) {
        // Place machine
        helper.setBlock(POS, RegistryEntries.BLOCK_MECHANICAL_SQUEEZER.value());
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_ENERGY_BATTERY.value());
        BlockEntityMechanicalSqueezer machine = helper.getBlockEntity(POS, BlockEntityMechanicalSqueezer.class);
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.north(), BlockEntityEnergyBattery.class);
        battery.setEnergyStored(100_000);

        // Set input
        machine.getInventory().setItem(0, new ItemStack(RegistryEntries.BLOCK_MENRIL_LOG.get()));

        helper.succeedWhen(() -> {
            helper.assertTrue(machine.getInventory().getItem(0).isEmpty(), Component.literal("Machine did not consume input"));
            helper.assertValueEqual(machine.getInventory().getItem(1).getItem(), RegistryEntries.ITEM_CRYSTALIZED_MENRIL_CHUNK.get(), Component.literal("Machine did not produce item output"));
            helper.assertValueEqual(machine.getTank().getFluid().getFluid(), RegistryEntries.FLUID_MENRIL_RESIN.get(), Component.literal("Machine did not produce fluid output"));
            helper.assertTrue(battery.getEnergyStored() < 100_000, Component.literal("Energy in battery did not decrease"));
        });
    }

}
