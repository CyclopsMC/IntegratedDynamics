package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsMenrilLogs {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    protected void testSmeltsToCharcoal(GameTestHelper helper, Block log) {
        helper.setBlock(POS, Blocks.FURNACE);
        FurnaceBlockEntity furnace = helper.getBlockEntity(POS);
        furnace.setItem(0, new ItemStack(log));
        furnace.setItem(1, new ItemStack(Items.COAL));

        helper.succeedWhen(() -> helper.assertValueEqual(furnace.getItem(2).getItem(), Items.CHARCOAL, "Furnace did not produce charcoal"));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 300)
    public void testMenrilLogSmeltsToCharcoal(GameTestHelper helper) {
        testSmeltsToCharcoal(helper, RegistryEntries.BLOCK_MENRIL_LOG.get());
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 300)
    public void testMenrilLogFilledSmeltsToCharcoal(GameTestHelper helper) {
        testSmeltsToCharcoal(helper, RegistryEntries.BLOCK_MENRIL_LOG_FILLED.get());
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 300)
    public void testMenrilLogStrippedSmeltsToCharcoal(GameTestHelper helper) {
        testSmeltsToCharcoal(helper, RegistryEntries.BLOCK_MENRIL_LOG_STRIPPED.get());
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 300)
    public void testMenrilWoodSmeltsToCharcoal(GameTestHelper helper) {
        testSmeltsToCharcoal(helper, RegistryEntries.BLOCK_MENRIL_WOOD.get());
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 300)
    public void testMenrilWoodStrippedSmeltsToCharcoal(GameTestHelper helper) {
        testSmeltsToCharcoal(helper, RegistryEntries.BLOCK_MENRIL_WOOD_STRIPPED.get());
    }

}
