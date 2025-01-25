package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Menril Block Slab.
 * @author rubensworks
 *
 */
public class BlockCrystalizedMenrilBlockSlabConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockCrystalizedMenrilBlockSlabConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_menril_block_slab",
                (eConfig, properties) -> new SlabBlock(properties
                        .mapColor(MapColor.COLOR_CYAN)
                        .sound(SoundType.SNOW)
                        .strength(1.5F)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
