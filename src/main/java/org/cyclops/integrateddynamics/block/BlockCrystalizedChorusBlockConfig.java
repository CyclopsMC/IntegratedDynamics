package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Chorus block.
 * @author rubensworks
 *
 */
public class BlockCrystalizedChorusBlockConfig extends BlockConfig {

    public BlockCrystalizedChorusBlockConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_chorus_block",
                eConfig -> new Block(Block.Properties.create(Material.CLAY)
                        .sound(SoundType.SNOW)
                        .hardnessAndResistance(1.5F)
                        .harvestLevel(0)
                        .harvestTool(ToolType.PICKAXE)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
    
}
