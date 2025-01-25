package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Chorus Block Slab.
 * @author rubensworks
 *
 */
public class BlockCrystalizedChorusBlockSlabConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockCrystalizedChorusBlockSlabConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_chorus_block_slab",
                (eConfig, properties) -> new SlabBlock(properties
                        .mapColor(MapColor.COLOR_PINK)
                        .sound(SoundType.SNOW)
                        .strength(1.5F)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
