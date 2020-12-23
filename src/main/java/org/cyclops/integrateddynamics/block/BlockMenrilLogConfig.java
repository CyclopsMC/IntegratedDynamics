package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Blocks;
import net.minecraft.block.material.MaterialColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Menril Wood.
 * @author rubensworks
 *
 */
public class BlockMenrilLogConfig extends BlockConfig {

    public BlockMenrilLogConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_log",
                eConfig -> Blocks.createLogBlock(MaterialColor.CYAN, MaterialColor.CYAN),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
    
    @Override
    public void onRegistered() {
        BlockHelpers.setFireInfo(RegistryEntries.BLOCK_MENRIL_LOG, 5, 20);
    }

}
