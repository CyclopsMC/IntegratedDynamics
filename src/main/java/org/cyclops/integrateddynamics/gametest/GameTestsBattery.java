package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.cyclops.integrateddynamics.capability.energystorage.SimpleEnergyHandlerCapacity;

public class GameTestsBattery {

    public static final String TEMPLATE_EMPTY = "integrateddynamics:empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testBatteryTransfer(GameTestHelper helper) {
        // Place batteries
        helper.setBlock(POS, RegistryEntries.BLOCK_ENERGY_BATTERY.value());
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_ENERGY_BATTERY.value());
        BlockEntityEnergyBattery battery1 = helper.getBlockEntity(POS, BlockEntityEnergyBattery.class);
        BlockEntityEnergyBattery battery2 = helper.getBlockEntity(POS.north(), BlockEntityEnergyBattery.class);

        // Fill battery 1
        battery1.setEnergyStored(100_000);

        // Enable transfer with redstone signal
        helper.setBlock(POS.south(), Blocks.REDSTONE_TORCH);

        helper.succeedWhen(() -> {
            helper.assertValueEqual(battery1.getEnergyStored(), 0, Component.literal("Energy in battery 1 is not zero"));
            helper.assertValueEqual(battery2.getEnergyStored(), 100_000, Component.literal("Energy in battery 2 is not 5000"));

        });
    }

    protected static ItemStack getBatteryItemEntity(GameTestHelper helper) {
        return helper.findEntities(EntityTypes.ITEM, POS.getX(), POS.getY(), POS.getZ(), 10)
                .stream()
                .filter(i -> i.getItem().getItem() == RegistryEntries.ITEM_ENERGY_BATTERY.get())
                .findFirst()
                .map(ItemEntity::getItem)
                .orElse(ItemStack.EMPTY);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testBatteryCombineEmpty(GameTestHelper helper) {
        // Set crafter
        helper.setBlock(POS, Blocks.CRAFTER);
        CrafterBlockEntity crafter = helper.getBlockEntity(POS, CrafterBlockEntity.class);
        helper.setBlock(POS.north(), Blocks.REDSTONE_TORCH);

        // Set recipe
        crafter.setItem(0, new ItemStack(RegistryEntries.ITEM_ENERGY_BATTERY));
        crafter.setItem(1, new ItemStack(RegistryEntries.ITEM_ENERGY_BATTERY));

        helper.succeedWhen(() -> {
            ItemStack result = getBatteryItemEntity(helper);
            helper.assertValueEqual(result.getItem(), RegistryEntries.ITEM_ENERGY_BATTERY.get(), Component.literal("Result item is incorrect"));
            helper.assertValueEqual(result.get(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_CAPACITY), BlockEnergyBatteryConfig.capacity * 2, Component.literal("Result item capacity is incorrect"));
            helper.assertValueEqual(result.get(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_ENERGY_STORAGE), 0, Component.literal("Result item energy content is incorrect"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testBatteryCombineFilled(GameTestHelper helper) {
        // Set crafter
        helper.setBlock(POS, Blocks.CRAFTER);
        CrafterBlockEntity crafter = helper.getBlockEntity(POS, CrafterBlockEntity.class);
        helper.setBlock(POS.north(), Blocks.REDSTONE_TORCH);

        // Set recipe
        crafter.setItem(0, new ItemStack(RegistryEntries.ITEM_ENERGY_BATTERY));
        crafter.setItem(1, new ItemStack(RegistryEntries.ITEM_ENERGY_BATTERY));

        // Add some energy
        crafter.getItem(0).set(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_ENERGY_STORAGE, 10_000);
        crafter.getItem(1).set(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_ENERGY_STORAGE, 10_000);

        helper.succeedWhen(() -> {
            ItemStack result = getBatteryItemEntity(helper);
            helper.assertValueEqual(result.getItem(), RegistryEntries.ITEM_ENERGY_BATTERY.get(), Component.literal("Result item is incorrect"));
            helper.assertValueEqual(result.get(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_CAPACITY), BlockEnergyBatteryConfig.capacity * 2, Component.literal("Result item capacity is incorrect"));
            helper.assertValueEqual(result.get(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_ENERGY_STORAGE), 20_000, Component.literal("Result item energy content is incorrect"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testBatteryCombineFilledHigherCapacity(GameTestHelper helper) {
        // Set crafter
        helper.setBlock(POS, Blocks.CRAFTER);
        CrafterBlockEntity crafter = helper.getBlockEntity(POS, CrafterBlockEntity.class);
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
            ItemStack result = getBatteryItemEntity(helper);
            helper.assertValueEqual(result.getItem(), RegistryEntries.ITEM_ENERGY_BATTERY.get(), Component.literal("Result item is incorrect"));
            helper.assertValueEqual(result.get(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_CAPACITY), BlockEnergyBatteryConfig.capacity * 4, Component.literal("Result item capacity is incorrect"));
            helper.assertValueEqual(result.get(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_ENERGY_STORAGE), 20_000, Component.literal("Result item energy content is incorrect"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testBatteryCapacityPersistence(GameTestHelper helper) {
        // Place battery
        helper.setBlock(POS, RegistryEntries.BLOCK_ENERGY_BATTERY.value());
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS, BlockEntityEnergyBattery.class);

        // Set a custom capacity (simulating a crafted higher-capacity battery) and energy above the default capacity
        int customCapacity = BlockEnergyBatteryConfig.capacity * 2;
        int customEnergy = BlockEnergyBatteryConfig.capacity + 500_000;
        battery.getEnergyHandler().setCapacity(customCapacity);
        battery.setEnergyStored(customEnergy);

        // Simulate save/load by serializing and deserializing the energy handler
        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        battery.getEnergyHandler().serialize(output);
        CompoundTag tag = output.buildResult();

        SimpleEnergyHandlerCapacity newHandler = new SimpleEnergyHandlerCapacity(BlockEnergyBatteryConfig.capacity);
        ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), tag);
        newHandler.deserialize(input);

        helper.assertValueEqual(newHandler.getCapacityAsInt(), customCapacity, Component.literal("Capacity was not persisted after save/load"));
        helper.assertValueEqual(newHandler.getAmountAsInt(), customEnergy, Component.literal("Energy was not persisted after save/load"));
        helper.succeed();
    }

}
