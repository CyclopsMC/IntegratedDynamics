package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;

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
                eConfig -> new LeavesBlock(Block.Properties.of(Material.LEAVES)
                        .strength(0.2F)
                        .randomTicks()
                        .sound(SoundType.GRASS)
                        .noOcclusion()),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
    
    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        BlockHelpers.setFireInfo(getInstance(), 5, 20);
        ComposterBlock.COMPOSTABLES.put(getItemInstance(), 0.3F);
    }
    
}
