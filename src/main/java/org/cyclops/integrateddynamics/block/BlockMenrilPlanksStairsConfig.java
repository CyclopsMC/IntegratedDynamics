package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Menril Wood Stairs.
 * @author rubensworks
 *
 */
public class BlockMenrilPlanksStairsConfig extends BlockConfig {

    public BlockMenrilPlanksStairsConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_planks_stairs",
                eConfig -> new StairBlock(() -> RegistryEntries.BLOCK_MENRIL_PLANKS.defaultBlockState(), Block.Properties.of(Material.WOOD, MaterialColor.COLOR_CYAN)
                        .strength(2.0F)
                        .sound(SoundType.WOOD)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

    @Override
    public void onRegistered() {
        BlockHelpers.setFireInfo(RegistryEntries.BLOCK_MENRIL_PLANKS_STAIRS, 5, 20);
    }

}
