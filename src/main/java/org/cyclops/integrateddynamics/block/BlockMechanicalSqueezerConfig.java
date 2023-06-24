package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockMechanicalSqueezer}.
 * @author rubensworks
 */
public class BlockMechanicalSqueezerConfig extends BlockConfig {

    @ConfigurableProperty(category = "machine", comment = "The energy capacity of a mechanical squeezer.", minimalValue = 0)
    public static int capacity = 100000;

    @ConfigurableProperty(category = "machine", comment = "The energy consumption rate.", minimalValue = 0)
    public static int consumptionRate = 80;

    @ConfigurableProperty(category = "machine", comment = "How many mB per tick can be auto-ejected.", minimalValue = 0)
    public static int autoEjectFluidRate = 500;

    public BlockMechanicalSqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_squeezer",
                eConfig -> new BlockMechanicalSqueezer(Block.Properties.of()
                        .strength(2.0F, 5.0F)
                        .sound(SoundType.METAL)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
}
