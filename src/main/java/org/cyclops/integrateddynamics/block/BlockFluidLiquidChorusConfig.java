package org.cyclops.integrateddynamics.block;


import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockFluidLiquidChorus}.
 * @author rubensworks
 *
 */
public class BlockFluidLiquidChorusConfig extends BlockConfig {

    public BlockFluidLiquidChorusConfig() {
        super(
                IntegratedDynamics._instance,
                "block_liquid_chorus",
                eConfig -> new BlockFluidLiquidChorus(Block.Properties.create(Material.WATER)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
    
}
