package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Chorus Brick Slab.
 * @author rubensworks
 *
 */
public class BlockCrystalizedChorusBrickSlabConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockCrystalizedChorusBrickSlabConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_chorus_brick_slab",
                (eConfig, properties) -> new SlabBlock(properties
                        .mapColor(MapColor.COLOR_PINK)
                        .sound(SoundType.SNOW)
                        .strength(1.5F)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
