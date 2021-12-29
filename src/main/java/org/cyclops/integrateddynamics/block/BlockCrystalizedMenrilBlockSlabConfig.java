package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Menril Block Slab.
 * @author rubensworks
 *
 */
public class BlockCrystalizedMenrilBlockSlabConfig extends BlockConfig {

    public BlockCrystalizedMenrilBlockSlabConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_menril_block_slab",
                eConfig -> new SlabBlock(BlockBehaviour.Properties.of(Material.CLAY, MaterialColor.COLOR_CYAN)
                        .sound(SoundType.SNOW)
                        .strength(1.5F)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
