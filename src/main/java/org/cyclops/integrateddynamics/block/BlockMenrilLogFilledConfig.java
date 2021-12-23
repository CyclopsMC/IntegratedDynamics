package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Menril Wood.
 * @author rubensworks
 *
 */
public class BlockMenrilLogFilledConfig extends BlockConfig {

    @ConfigurableProperty(category = "worldgeneration", comment = "The 1/x chance at which Menril Wood will be filled with Menril Resin when generated, the higher this value, the lower the chance.", isCommandable = true, minimalValue = 0)
    public static int filledMenrilLogChance = 10;

    public BlockMenrilLogFilledConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_log_filled",
                eConfig -> new BlockMenrilLogFilled(Block.Properties.of(Material.WOOD, MaterialColor.COLOR_CYAN)
                        .strength(2.0F)
                        .sound(SoundType.WOOD)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

    @Override
    public void onRegistered() {
        BlockHelpers.setFireInfo(RegistryEntries.BLOCK_MENRIL_LOG, 5, 20);
    }

}
