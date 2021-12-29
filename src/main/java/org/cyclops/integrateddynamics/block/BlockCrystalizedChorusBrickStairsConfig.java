package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Crystalized Chorus Stairs.
 * @author rubensworks
 *
 */
public class BlockCrystalizedChorusBrickStairsConfig extends BlockConfig {

    public BlockCrystalizedChorusBrickStairsConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_chorus_brick_stairs",
                eConfig -> new StairBlock(() -> RegistryEntries.BLOCK_CRYSTALIZED_CHORUS_BRICK.defaultBlockState(), Block.Properties.of(Material.CLAY)
                        .sound(SoundType.SNOW)
                        .strength(1.5F)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
