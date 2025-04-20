package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityCoalGenerator;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsGenerator {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testGenerator(GameTestHelper helper) {
        // Place generator
        helper.setBlock(POS, RegistryEntries.BLOCK_COAL_GENERATOR.value());
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_ENERGY_BATTERY.value());
        BlockEntityCoalGenerator generator = helper.getBlockEntity(POS);
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.north());

        // Fill generator
        generator.getInventory().setItem(0, new ItemStack(Items.COAL));

        helper.succeedWhen(() -> {
            helper.assertTrue(generator.getInventory().getItem(0).isEmpty(), "Generator did not consume input");
            helper.assertTrue(generator.isBurning(), "Generator is not burning");
            helper.assertTrue(battery.getEnergyStored() > 1000, "Energy in battery did not increase");

        });
    }

}
