package org.cyclops.integrateddynamics.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Menril Fence.
 * @author rubensworks
 *
 */
public class BlockMenrilFenceConfig extends BlockConfig {

    public BlockMenrilFenceConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_fence",
                eConfig -> new FenceBlock(AbstractBlock.Properties.create(Material.WOOD, MaterialColor.CYAN)
                        .hardnessAndResistance(2.0F, 3.0F)
                        .sound(SoundType.WOOD)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        BlockHelpers.setFireInfo(getInstance(), 5, 20);
    }
}
