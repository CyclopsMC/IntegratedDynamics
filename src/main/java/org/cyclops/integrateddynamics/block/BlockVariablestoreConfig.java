package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockVariablestore}.
 * @author rubensworks
 */
public class BlockVariablestoreConfig extends BlockConfig {

    public BlockVariablestoreConfig() {
        super(
                IntegratedDynamics._instance,
                "variablestore",
                eConfig -> new BlockVariablestore(Block.Properties.create(Material.ANVIL)
                        .hardnessAndResistance(5.0F)
                        .sound(SoundType.METAL)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
