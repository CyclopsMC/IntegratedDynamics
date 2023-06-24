package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Menril Brick Slab.
 * @author rubensworks
 *
 */
public class BlockCrystalizedMenrilBrickSlabConfig extends BlockConfig {

    public BlockCrystalizedMenrilBrickSlabConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_menril_brick_slab",
                eConfig -> new SlabBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_CYAN)
                        .sound(SoundType.SNOW)
                        .strength(1.5F)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
