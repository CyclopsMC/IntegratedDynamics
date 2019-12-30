package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * A block for the Menril Resin fluid.
 * @author rubensworks
 *
 */
public class BlockFluidMenrilResin extends FlowingFluidBlock {

    public BlockFluidMenrilResin(Block.Properties builder) {
        super(() -> RegistryEntries.FLUID_MENRIL_RESIN, builder);
    }

}
