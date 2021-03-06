package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Blocks;
import net.minecraft.block.material.MaterialColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Stripped Menril Wood.
 * @author rubensworks
 *
 */
public class BlockMenrilWoodConfig extends BlockConfig {

    public BlockMenrilWoodConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_wood",
                eConfig -> Blocks.createLogBlock(MaterialColor.CYAN, MaterialColor.CYAN),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        BlockHelpers.setFireInfo(getInstance(), 5, 20);
    }
}
