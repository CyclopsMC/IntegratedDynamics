package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Chorus Block Slab.
 * @author rubensworks
 *
 */
public class BlockCrystalizedChorusBlockSlabConfig extends BlockConfig {

    public BlockCrystalizedChorusBlockSlabConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_chorus_block_slab",
                eConfig -> new SlabBlock(BlockBehaviour.Properties.of(Material.CLAY, MaterialColor.COLOR_PINK)
                        .sound(SoundType.SNOW)
                        .strength(1.5F)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
