package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Crystalized Chorus Stairs.
 * @author rubensworks
 *
 */
public class BlockCrystalizedChorusBlockStairsConfig extends BlockConfig {

    public BlockCrystalizedChorusBlockStairsConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_chorus_block_stairs",
                eConfig -> new StairsBlock(() -> RegistryEntries.BLOCK_CRYSTALIZED_CHORUS_BLOCK.getDefaultState(), Block.Properties.create(Material.CLAY)
                        .sound(SoundType.SNOW)
                        .hardnessAndResistance(1.5F)
                        .harvestLevel(0)
                        .harvestTool(ToolType.PICKAXE)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
