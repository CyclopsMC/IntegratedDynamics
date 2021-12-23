package org.cyclops.integrateddynamics.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.common.ToolType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Menril Block Slab.
 * @author rubensworks
 *
 */
public class BlockCrystalizedMenrilBlockSlabConfig extends BlockConfig {

    public BlockCrystalizedMenrilBlockSlabConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_menril_block_slab",
                eConfig -> new SlabBlock(AbstractBlock.Properties.of(Material.CLAY, MaterialColor.COLOR_CYAN)
                        .sound(SoundType.SNOW)
                        .strength(1.5F)
                        .harvestLevel(0)
                        .harvestTool(ToolType.PICKAXE)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
