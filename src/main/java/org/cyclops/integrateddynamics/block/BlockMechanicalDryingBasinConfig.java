package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SoundType;
import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockMechanicalDryingBasin}.
 * @author rubensworks
 */
public class BlockMechanicalDryingBasinConfig extends BlockConfigCommon<IntegratedDynamics> {

    @ConfigurablePropertyCommon(category = "machine", comment = "The energy capacity of a mechanical drying basin.", minimalValue = 0)
    public static int capacity = 100000;

    @ConfigurablePropertyCommon(category = "machine", comment = "The energy consumption rate.", minimalValue = 0)
    public static int consumptionRate = 80;

    public BlockMechanicalDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_drying_basin",
                (eConfig, properties) -> new BlockMechanicalDryingBasin(properties
                        .strength(2.0F, 5.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()
                        .isRedstoneConductor((blockState, world, pos) -> false)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
