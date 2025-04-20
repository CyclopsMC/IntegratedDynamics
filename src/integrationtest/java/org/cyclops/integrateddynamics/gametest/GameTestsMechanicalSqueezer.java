package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.cyclops.integrateddynamics.blockentity.BlockEntityMechanicalSqueezer;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsMechanicalSqueezer {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMechanicalSqueezer(GameTestHelper helper) {
        // Place machine
        helper.setBlock(POS, RegistryEntries.BLOCK_MECHANICAL_SQUEEZER.value());
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_ENERGY_BATTERY.value());
        BlockEntityMechanicalSqueezer machine = helper.getBlockEntity(POS);
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.north());
        battery.setEnergyStored(100_000);

        // Set input
        machine.getInventory().setItem(0, new ItemStack(RegistryEntries.BLOCK_MENRIL_LOG.get()));

        helper.succeedWhen(() -> {
            helper.assertTrue(machine.getInventory().getItem(0).isEmpty(), "Machine did not consume input");
            helper.assertValueEqual(machine.getInventory().getItem(1).getItem(), RegistryEntries.ITEM_CRYSTALIZED_MENRIL_CHUNK.get(), "Machine did not produce item output");
            helper.assertValueEqual(machine.getTank().getFluid().getFluid(), RegistryEntries.FLUID_MENRIL_RESIN.get(), "Machine did not produce fluid output");
            helper.assertTrue(battery.getEnergyStored() < 100_000, "Energy in battery did not decrease");
        });
    }

}
