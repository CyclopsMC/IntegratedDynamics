package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Menril Wood Planks.
 * @author rubensworks
 *
 */
public class BlockMenrilPlanksConfig extends BlockConfig {

    public BlockMenrilPlanksConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_planks",
                eConfig -> new Block(Block.Properties.create(Material.WOOD, MaterialColor.CYAN)
                        .hardnessAndResistance(2.0F, 3.0F)
                        .sound(SoundType.WOOD)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
    
    @Override
    public void onRegistered() {
        ((FireBlock) Blocks.FIRE).setFireInfo(RegistryEntries.BLOCK_MENRIL_PLANKS, 5, 20);
    }
    
}
