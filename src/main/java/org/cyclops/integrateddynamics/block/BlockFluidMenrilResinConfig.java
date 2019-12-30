package org.cyclops.integrateddynamics.block;


import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockFluidMenrilResin}.
 * @author rubensworks
 *
 */
public class BlockFluidMenrilResinConfig extends BlockConfig {

    public BlockFluidMenrilResinConfig() {
        super(
                IntegratedDynamics._instance,
                "block_menril_resin",
                eConfig -> new BlockFluidMenrilResin(Block.Properties.create(Material.WATER)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
    
}
