package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Crystallized Menril Brick Stairs.
 * @author rubensworks
 *
 */
public class BlockCrystalizedMenrilBrickStairsConfig extends BlockConfig {

    public BlockCrystalizedMenrilBrickStairsConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_menril_brick_stairs",
                eConfig -> new StairBlock(() -> RegistryEntries.BLOCK_CRYSTALIZED_MENRIL_BRICK.get().defaultBlockState(), Block.Properties.of()
                        .sound(SoundType.SNOW)
                        .strength(1.5F)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
