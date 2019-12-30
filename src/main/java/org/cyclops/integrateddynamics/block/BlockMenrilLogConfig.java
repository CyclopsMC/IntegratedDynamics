package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.LogBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
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
                eConfig -> new LogBlock(MaterialColor.CYAN, Block.Properties.create(Material.WOOD, MaterialColor.CYAN)
                        .hardnessAndResistance(2.0F)
                        .sound(SoundType.WOOD)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
    
    @Override
    public void onRegistered() {
        ((FireBlock) Blocks.FIRE).setFireInfo(RegistryEntries.BLOCK_MENRIL_LOG, 5, 20);
    }

}
