package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntitySqueezer;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsSqueezer {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testSqueezer(GameTestHelper helper) {
        // Place machine
        helper.setBlock(POS, RegistryEntries.BLOCK_SQUEEZER.value());
        BlockEntitySqueezer machine = helper.getBlockEntity(POS);

        // Set input
        machine.getInventory().setItem(0, new ItemStack(RegistryEntries.BLOCK_MENRIL_LOG.get()));

        // Let mob fall on the squeezer
        Wrapper<Goat> entity = new Wrapper<>(helper.spawnWithNoFreeWill(EntityType.GOAT, POS.offset(0, 10, 0)));
        helper.runAfterDelay(20, () -> {
            entity.get().die(helper.getLevel().damageSources().generic());
            entity.set(helper.spawnWithNoFreeWill(EntityType.GOAT, POS.offset(0, 10, 0)));
        });

        helper.succeedWhen(() -> {
            helper.assertTrue(machine.getInventory().getItem(0).isEmpty(), "Machine did not consume input");
            helper.assertValueEqual(machine.getTank().getFluid().getFluid(), RegistryEntries.FLUID_MENRIL_RESIN.get(), "Machine did not produce fluid output");
        });
    }

}
