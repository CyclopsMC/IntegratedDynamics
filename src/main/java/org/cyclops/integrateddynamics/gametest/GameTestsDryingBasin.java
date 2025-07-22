package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityDryingBasin;

public class GameTestsDryingBasin {

    public static final String TEMPLATE_EMPTY = "integrateddynamics:empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 1, 2);

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 200)
    public void testDryingBasin(GameTestHelper helper) {
        // Place machine
        helper.setBlock(POS, RegistryEntries.BLOCK_DRYING_BASIN.value());
        BlockEntityDryingBasin machine = helper.getBlockEntity(POS, BlockEntityDryingBasin.class);

        // Set input
        machine.getTank().setFluid(new FluidStack(RegistryEntries.FLUID_MENRIL_RESIN.get(), 1_000));

        helper.succeedWhen(() -> {
            helper.assertValueEqual(machine.getInventory().getItem(0).getItem(), RegistryEntries.BLOCK_CRYSTALIZED_MENRIL_BLOCK.get().asItem(), Component.literal("Machine did not produce item output"));
            helper.assertTrue(machine.getTank().getFluid().isEmpty(), Component.literal("Machine did not consume fluid input"));
        });
    }

}
