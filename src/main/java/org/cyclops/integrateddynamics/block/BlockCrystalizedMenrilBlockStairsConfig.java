package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Crystallized Menril Block Stairs.
 * @author rubensworks
 *
 */
public class BlockCrystalizedMenrilBlockStairsConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockCrystalizedMenrilBlockStairsConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_menril_block_stairs",
                (eConfig, properties) -> new StairBlock(RegistryEntries.BLOCK_CRYSTALIZED_MENRIL_BLOCK.get().defaultBlockState(), properties
                        .sound(SoundType.SNOW)
                        .strength(1.5F)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
