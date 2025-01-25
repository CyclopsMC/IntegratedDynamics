package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Crystalized Chorus Stairs.
 * @author rubensworks
 *
 */
public class BlockCrystalizedChorusBlockStairsConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockCrystalizedChorusBlockStairsConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_chorus_block_stairs",
                (eConfig, properties) -> new StairBlock(RegistryEntries.BLOCK_CRYSTALIZED_CHORUS_BLOCK.get().defaultBlockState(), properties
                        .sound(SoundType.SNOW)
                        .strength(1.5F)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
