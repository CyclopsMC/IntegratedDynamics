package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * A block for the Menril Resin fluid.
 * @author rubensworks
 */
public class BlockFluidMenrilResin extends LiquidBlock {

    public BlockFluidMenrilResin(Block.Properties builder) {
        super(() -> RegistryEntries.FLUID_MENRIL_RESIN, builder);
    }

}
