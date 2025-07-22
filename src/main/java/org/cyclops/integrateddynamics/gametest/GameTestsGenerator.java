package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityCoalGenerator;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;

public class GameTestsGenerator {

    public static final String TEMPLATE_EMPTY = "integrateddynamics:empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testGenerator(GameTestHelper helper) {
        // Place generator
        helper.setBlock(POS, RegistryEntries.BLOCK_COAL_GENERATOR.value());
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_ENERGY_BATTERY.value());
        BlockEntityCoalGenerator generator = helper.getBlockEntity(POS, BlockEntityCoalGenerator.class);
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.north(), BlockEntityEnergyBattery.class);

        // Fill generator
        generator.getInventory().setItem(0, new ItemStack(Items.COAL));

        helper.succeedWhen(() -> {
            helper.assertTrue(generator.getInventory().getItem(0).isEmpty(), Component.literal("Generator did not consume input"));
            helper.assertTrue(generator.isBurning(), Component.literal("Generator is not burning"));
            helper.assertTrue(battery.getEnergyStored() > 1000, Component.literal("Energy in battery did not increase"));

        });
    }

}
