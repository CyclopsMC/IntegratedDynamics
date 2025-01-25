package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Chorus block.
 * @author rubensworks
 *
 */
public class BlockCrystalizedChorusBrickConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockCrystalizedChorusBrickConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_chorus_brick",
                (eConfig, properties) -> new Block(properties
                        .sound(SoundType.SNOW)
                        .strength(1.5F)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
