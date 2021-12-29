package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Crystallized Menril Block Stairs.
 * @author rubensworks
 *
 */
public class BlockCrystalizedMenrilBlockStairsConfig extends BlockConfig {

    public BlockCrystalizedMenrilBlockStairsConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_menril_block_stairs",
                nulleConfig -> new StairBlock(() -> RegistryEntries.BLOCK_CRYSTALIZED_MENRIL_BLOCK.defaultBlockState(), Block.Properties.of(Material.CLAY)
                        .sound(SoundType.SNOW)
                        .strength(1.5F)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
