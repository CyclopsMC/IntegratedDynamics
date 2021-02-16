package org.cyclops.integrateddynamics.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.common.ToolType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Chorus Block Slab.
 * @author rubensworks
 *
 */
public class BlockCrystalizedChorusBlockSlabConfig extends BlockConfig {

    public BlockCrystalizedChorusBlockSlabConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_chorus_block_slab",
                eConfig -> new SlabBlock(AbstractBlock.Properties.create(Material.CLAY, MaterialColor.PINK)
                        .sound(SoundType.SNOW)
                        .hardnessAndResistance(1.5F)
                        .harvestLevel(0)
                        .harvestTool(ToolType.PICKAXE)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
