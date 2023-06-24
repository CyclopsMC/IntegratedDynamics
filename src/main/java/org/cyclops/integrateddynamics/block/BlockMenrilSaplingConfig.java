package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.world.gen.TreeMenril;

/**
 * Config for the Menril Sapling.
 * @author rubensworks
 *
 */
public class BlockMenrilSaplingConfig extends BlockConfig {

    public BlockMenrilSaplingConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_sapling",
                eConfig -> new SaplingBlock(new TreeMenril(), Block.Properties.of()
                        .noCollission()
                        .randomTicks()
                        .strength(0)
                        .sound(SoundType.GRASS)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        ComposterBlock.COMPOSTABLES.put(getItemInstance(), 0.3F);
    }
}
