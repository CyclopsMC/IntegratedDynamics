package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.testReadAspect;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsAspectsReadInventory {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadInventoryFullTrue(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(POS.west());
        for (int i = 0; i < 27; i++) {
            chest.setItem(i, new ItemStack(Items.COAL));
        }
        testReadAspect(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.BOOLEAN_FULL, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadInventoryFullFalse(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(POS.west());
        for (int i = 0; i < 26; i++) {
            chest.setItem(i, new ItemStack(Items.COAL));
        }
        testReadAspect(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.BOOLEAN_FULL, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadInventoryEmptyTrue(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(POS.west());
        testReadAspect(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.BOOLEAN_EMPTY, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadInventoryEmptyFalse(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(POS.west());
        chest.setItem(0, new ItemStack(Items.COAL));
        testReadAspect(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.BOOLEAN_EMPTY, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadInventoryNonEmptyTrue(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(POS.west());
        chest.setItem(0, new ItemStack(Items.COAL));
        testReadAspect(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.BOOLEAN_NONEMPTY, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadInventoryNonEmptyFalse(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(POS.west());
        testReadAspect(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.BOOLEAN_NONEMPTY, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadInventoryApplicableTrue(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.CHEST);
        testReadAspect(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.BOOLEAN_APPLICABLE, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadInventoryApplicableFalse(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.BOOLEAN_APPLICABLE, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadInventoryCount(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(POS.west());
        chest.setItem(0, new ItemStack(Items.COAL, 10));
        chest.setItem(1, new ItemStack(Items.COAL, 5));
        testReadAspect(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.INTEGER_COUNT, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadInventorySlotsValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.CHEST);
        testReadAspect(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.INTEGER_SLOTS, ValueTypeInteger.ValueInteger.of(27));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadInventorySlotsInvalid(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.INTEGER_SLOTS, ValueTypeInteger.ValueInteger.of(0));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadInventorySlotsFilled(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(POS.west());
        chest.setItem(0, new ItemStack(Items.COAL, 10));
        chest.setItem(1, new ItemStack(Items.COAL, 5));
        chest.setItem(10, new ItemStack(Items.COAL, 5));
        testReadAspect(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.INTEGER_SLOTSFILLED, ValueTypeInteger.ValueInteger.of(3));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadInventoryFillRatio(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(POS.west());
        chest.setItem(0, new ItemStack(Items.COAL, 10));
        chest.setItem(1, new ItemStack(Items.COAL, 5));
        chest.setItem(10, new ItemStack(Items.COAL, 5));
        testReadAspect(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.DOUBLE_FILLRATIO, ValueTypeDouble.ValueDouble.of(3d / 27d));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadInventoryItemStacks(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(POS.west());
        chest.setItem(0, new ItemStack(Items.COAL, 10));
        chest.setItem(1, new ItemStack(Items.COAL, 5));
        chest.setItem(10, new ItemStack(Items.COAL, 5));
        testReadAspect(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.LIST_ITEMSTACKS, ValueTypeList.ValueList.ofAll(
                ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.COAL, 10)),
                ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.COAL, 5)),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.COAL, 5)),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY),
                ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY)
        ));
    }

}
