package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Chorus block.
 * @author rubensworks
 *
 */
public class BlockCrystalizedChorusBrickConfig extends BlockConfig {

    public BlockCrystalizedChorusBrickConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_chorus_brick",
                eConfig -> new Block(Block.Properties.of(Material.CLAY)
                        .sound(SoundType.SNOW)
                        .strength(1.5F)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
    
}
