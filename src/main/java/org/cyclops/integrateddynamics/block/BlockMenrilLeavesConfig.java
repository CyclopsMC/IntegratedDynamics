package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Menril Leaves.
 * @author rubensworks
 *
 */
public class BlockMenrilLeavesConfig extends BlockConfig {

    public BlockMenrilLeavesConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_leaves",
                eConfig -> new LeavesBlock(Block.Properties.create(Material.LEAVES)
                        .hardnessAndResistance(0.2F)
                        .tickRandomly()
                        .sound(SoundType.PLANT)
                        .notSolid()),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
    
    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        BlockHelpers.setFireInfo(getInstance(), 5, 20);
        ComposterBlock.CHANCES.put(getItemInstance(), 0.3F);
    }
    
}
