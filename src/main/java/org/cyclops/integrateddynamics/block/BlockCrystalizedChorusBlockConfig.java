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
public class BlockCrystalizedChorusBlockConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockCrystalizedChorusBlockConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_chorus_block",
                (eConfig, properties) -> new Block(properties
                        .sound(SoundType.SNOW)
                        .strength(1.5F)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
