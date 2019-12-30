package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockLogicProgrammer}.
 * @author rubensworks
 */
public class BlockLogicProgrammerConfig extends BlockConfig {

    public BlockLogicProgrammerConfig() {
        super(
                IntegratedDynamics._instance,
                "logic_programmer",
                eConfig -> new BlockLogicProgrammer(Block.Properties.create(Material.GLASS)
                .hardnessAndResistance(3.0F)
                .sound(SoundType.METAL)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
