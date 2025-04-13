package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.cyclops.integrateddynamics.blockentity.BlockEntityMechanicalDryingBasin;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsMechanicalDryingBasin {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMechanicalDryingBasin(GameTestHelper helper) {
        // Place machine
        helper.setBlock(POS, RegistryEntries.BLOCK_MECHANICAL_DRYING_BASIN.value());
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_ENERGY_BATTERY.value());
        BlockEntityMechanicalDryingBasin machine = helper.getBlockEntity(POS);
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.north());
        battery.setEnergyStored(100_000);

        // Set input
        machine.getTankInput().setFluid(new FluidStack(RegistryEntries.FLUID_MENRIL_RESIN.get(), 1_000));

        helper.succeedWhen(() -> {
            helper.assertValueEqual(machine.getInventory().getItem(1).getItem(), RegistryEntries.BLOCK_CRYSTALIZED_MENRIL_BLOCK.get().asItem(), "Machine did not produce item output");
            helper.assertTrue(machine.getTankInput().getFluid().isEmpty(), "Machine did not consume fluid input");
            helper.assertTrue(battery.getEnergyStored() < 100_000, "Energy in battery did not decrease");
        });
    }

}
