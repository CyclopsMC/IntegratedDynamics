package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Chorus Brick Slab.
 * @author rubensworks
 *
 */
public class BlockCrystalizedChorusBrickSlabConfig extends BlockConfig {

    public BlockCrystalizedChorusBrickSlabConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_chorus_brick_slab",
                eConfig -> new SlabBlock(BlockBehaviour.Properties.of(Material.CLAY, MaterialColor.COLOR_PINK)
                        .sound(SoundType.SNOW)
                        .strength(1.5F)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
