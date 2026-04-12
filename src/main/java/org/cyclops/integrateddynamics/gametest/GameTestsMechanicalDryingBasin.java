package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.cyclops.integrateddynamics.blockentity.BlockEntityMechanicalDryingBasin;

public class GameTestsMechanicalDryingBasin {

    public static final String TEMPLATE_EMPTY = "integrateddynamics:empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMechanicalDryingBasin(GameTestHelper helper) {
        // Place machine
        helper.setBlock(POS, RegistryEntries.BLOCK_MECHANICAL_DRYING_BASIN.value());
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_ENERGY_BATTERY.value());
        BlockEntityMechanicalDryingBasin machine = helper.getBlockEntity(POS, BlockEntityMechanicalDryingBasin.class);
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.north(), BlockEntityEnergyBattery.class);
        battery.setEnergyStored(100_000);

        // Set input
        machine.getTankInput().setFluid(new FluidStack(RegistryEntries.FLUID_MENRIL_RESIN.get(), 1_000));

        helper.succeedWhen(() -> {
            helper.assertValueEqual(machine.getInventory().getItem(1).getItem(), RegistryEntries.BLOCK_CRYSTALIZED_MENRIL_BLOCK.get().asItem(), Component.literal("Machine did not produce item output"));
            helper.assertTrue(machine.getTankInput().getFluid().isEmpty(), Component.literal("Machine did not consume fluid input"));
            helper.assertTrue(battery.getEnergyStored() < 100_000, Component.literal("Energy in battery did not decrease"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 200)
    public void testMechanicalDryingBasinItemAndFluidInputs(GameTestHelper helper) {
        // Place machine
        helper.setBlock(POS, RegistryEntries.BLOCK_MECHANICAL_DRYING_BASIN.value());
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_ENERGY_BATTERY.value());
        BlockEntityMechanicalDryingBasin machine = helper.getBlockEntity(POS, BlockEntityMechanicalDryingBasin.class);
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.north(), BlockEntityEnergyBattery.class);
        battery.setEnergyStored(100_000);

        // Set both item and fluid inputs simultaneously
        machine.getInventory().setItem(0, new ItemStack(Items.ROTTEN_FLESH));
        machine.getTankInput().setFluid(new FluidStack(RegistryEntries.FLUID_MENRIL_RESIN.get(), 1_000));

        helper.succeedWhen(() -> {
            // Both item-only (rotten_flesh -> leather) and fluid-only (menril_resin -> crystalized_menril_block)
            // recipes should have been processed sequentially
            boolean hasLeather = false;
            boolean hasMenrilBlock = false;
            for (int i = 1; i <= 4; i++) {
                if (machine.getInventory().getItem(i).is(Items.LEATHER)) {
                    hasLeather = true;
                }
                if (machine.getInventory().getItem(i).is(RegistryEntries.BLOCK_CRYSTALIZED_MENRIL_BLOCK.get().asItem())) {
                    hasMenrilBlock = true;
                }
            }
            helper.assertTrue(hasLeather, Component.literal("Machine did not process item-only recipe (rotten flesh -> leather)"));
            helper.assertTrue(hasMenrilBlock, Component.literal("Machine did not process fluid-only recipe (menril resin -> crystalized menril block)"));
            helper.assertTrue(machine.getInventory().getItem(0).isEmpty(), Component.literal("Machine did not consume item input"));
            helper.assertTrue(machine.getTankInput().getFluid().isEmpty(), Component.literal("Machine did not consume fluid input"));
        });
    }

}
