package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Menril Wood Stairs.
 * @author rubensworks
 *
 */
public class BlockMenrilPlanksStairsConfig extends BlockConfig {

    public BlockMenrilPlanksStairsConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_planks_stairs",
                eConfig -> new StairsBlock(() -> RegistryEntries.BLOCK_MENRIL_PLANKS.getDefaultState(), Block.Properties.create(Material.WOOD, MaterialColor.CYAN)
                        .hardnessAndResistance(2.0F)
                        .sound(SoundType.WOOD)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
    
    @Override
    public void onRegistered() {
        BlockHelpers.setFireInfo(RegistryEntries.BLOCK_MENRIL_PLANKS_STAIRS, 5, 20);
    }

}
