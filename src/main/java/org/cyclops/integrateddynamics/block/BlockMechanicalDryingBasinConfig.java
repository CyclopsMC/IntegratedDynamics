package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockMechanicalDryingBasin}.
 * @author rubensworks
 */
public class BlockMechanicalDryingBasinConfig extends BlockConfig {

    @ConfigurableProperty(category = "machine", comment = "The energy capacity of a mechanical drying basin.", minimalValue = 0)
    public static int capacity = 100000;

    @ConfigurableProperty(category = "machine", comment = "The energy consumption rate.", minimalValue = 0)
    public static int consumptionRate = 80;

    public BlockMechanicalDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_drying_basin",
                eConfig -> new BlockMechanicalDryingBasin(Block.Properties.of()
                        .strength(2.0F, 5.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()
                        .isRedstoneConductor((blockState, world, pos) -> false)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
