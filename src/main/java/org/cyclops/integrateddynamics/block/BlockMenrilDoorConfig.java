package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Menril Door.
 * @author josephcsible
 *
 */
public class BlockMenrilDoorConfig extends BlockConfig {

    public BlockMenrilDoorConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_door",
                eConfig -> new DoorBlock(Block.Properties.create(Material.WOOD, MaterialColor.CYAN)
                        .hardnessAndResistance(3.0F)
                        .sound(SoundType.WOOD)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
