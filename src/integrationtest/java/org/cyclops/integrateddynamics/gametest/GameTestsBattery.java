package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsBattery {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testBatteryTransfer(GameTestHelper helper) {
        // Place batteries
        helper.setBlock(POS, RegistryEntries.BLOCK_ENERGY_BATTERY.value());
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_ENERGY_BATTERY.value());
        BlockEntityEnergyBattery battery1 = helper.getBlockEntity(POS);
        BlockEntityEnergyBattery battery2 = helper.getBlockEntity(POS.north());

        // Fill battery 1
        battery1.setEnergyStored(100_000);

        // Enable transfer with redstone signal
        helper.setBlock(POS.south(), Blocks.REDSTONE_TORCH);

        helper.succeedWhen(() -> {
            helper.assertValueEqual(battery1.getEnergyStored(), 0, "Energy in battery 1 is not zero");
            helper.assertValueEqual(battery2.getEnergyStored(), 100_000, "Energy in battery 2 is not 5000");

        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testBatteryCombineEmpty(GameTestHelper helper) {
        // Set crafter
        helper.setBlock(POS, Blocks.CRAFTER);
        CrafterBlockEntity crafter = helper.getBlockEntity(POS);
        helper.setBlock(POS.north(), Blocks.REDSTONE_TORCH);

        // Set recipe
        crafter.setItem(0, new ItemStack(RegistryEntries.ITEM_ENERGY_BATTERY));
        crafter.setItem(1, new ItemStack(RegistryEntries.ITEM_ENERGY_BATTERY));

        helper.succeedWhen(() -> {
            ItemStack result = helper.findOneEntity(EntityType.ITEM).getItem();
            helper.assertValueEqual(result.getItem(), RegistryEntries.ITEM_ENERGY_BATTERY.get(), "Result item is incorrect");
            helper.assertValueEqual(result.get(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_CAPACITY), BlockEnergyBatteryConfig.capacity * 2, "Result item capacity is incorrect");
            helper.assertValueEqual(result.get(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_ENERGY_STORAGE), 0, "Result item energy content is incorrect");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testBatteryCombineFilled(GameTestHelper helper) {
        // Set crafter
        helper.setBlock(POS, Blocks.CRAFTER);
        CrafterBlockEntity crafter = helper.getBlockEntity(POS);
        helper.setBlock(POS.north(), Blocks.REDSTONE_TORCH);

        // Set recipe
        crafter.setItem(0, new ItemStack(RegistryEntries.ITEM_ENERGY_BATTERY));
        crafter.setItem(1, new ItemStack(RegistryEntries.ITEM_ENERGY_BATTERY));

        // Add some energy
        crafter.getItem(0).set(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_ENERGY_STORAGE, 10_000);
        crafter.getItem(1).set(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_ENERGY_STORAGE, 10_000);

        helper.succeedWhen(() -> {
            ItemStack result = helper.findOneEntity(EntityType.ITEM).getItem();
            helper.assertValueEqual(result.getItem(), RegistryEntries.ITEM_ENERGY_BATTERY.get(), "Result item is incorrect");
            helper.assertValueEqual(result.get(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_CAPACITY), BlockEnergyBatteryConfig.capacity * 2, "Result item capacity is incorrect");
            helper.assertValueEqual(result.get(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_ENERGY_STORAGE), 20_000, "Result item energy content is incorrect");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testBatteryCombineFilledHigherCapacity(GameTestHelper helper) {
        // Set crafter
        helper.setBlock(POS, Blocks.CRAFTER);
        CrafterBlockEntity crafter = helper.getBlockEntity(POS);
        helper.setBlock(POS.north(), Blocks.REDSTONE_TORCH);

        // Set recipe
        crafter.setItem(0, new ItemStack(RegistryEntries.ITEM_ENERGY_BATTERY));
        crafter.setItem(1, new ItemStack(RegistryEntries.ITEM_ENERGY_BATTERY));

        // Set capacity
        crafter.getItem(0).set(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_CAPACITY, BlockEnergyBatteryConfig.capacity * 2);
        crafter.getItem(1).set(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_CAPACITY, BlockEnergyBatteryConfig.capacity * 2);

        // Add some energy
        crafter.getItem(0).set(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_ENERGY_STORAGE, 10_000);
        crafter.getItem(1).set(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_ENERGY_STORAGE, 10_000);

        helper.succeedWhen(() -> {
            ItemStack result = helper.findOneEntity(EntityType.ITEM).getItem();
            helper.assertValueEqual(result.getItem(), RegistryEntries.ITEM_ENERGY_BATTERY.get(), "Result item is incorrect");
            helper.assertValueEqual(result.get(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_CAPACITY), BlockEnergyBatteryConfig.capacity * 4, "Result item capacity is incorrect");
            helper.assertValueEqual(result.get(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_ENERGY_STORAGE), 20_000, "Result item energy content is incorrect");
        });
    }

}
