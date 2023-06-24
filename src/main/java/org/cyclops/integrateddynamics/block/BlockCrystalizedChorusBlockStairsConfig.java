package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Crystalized Chorus Stairs.
 * @author rubensworks
 *
 */
public class BlockCrystalizedChorusBlockStairsConfig extends BlockConfig {

    public BlockCrystalizedChorusBlockStairsConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_chorus_block_stairs",
                eConfig -> new StairBlock(() -> RegistryEntries.BLOCK_CRYSTALIZED_CHORUS_BLOCK.defaultBlockState(), Block.Properties.of()
                        .sound(SoundType.SNOW)
                        .strength(1.5F)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
