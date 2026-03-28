package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.config.ModConfig;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link BlockEntityCoalGenerator}.
 * @author rubensworks
 *
 */
public class BlockEntityCoalGeneratorConfig extends BlockEntityConfig<BlockEntityCoalGenerator> {

    @ConfigurableProperty(category = "machine", comment = "The energy production rate (in RF/t) of the coal generator.", minimalValue = 1, configLocation = ModConfig.Type.SERVER)
    public static int energyPerTick = 20;

    public BlockEntityCoalGeneratorConfig() {
        super(
                IntegratedDynamics._instance,
                "coal_generator",
                (eConfig) -> new BlockEntityType<>(BlockEntityCoalGenerator::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_COAL_GENERATOR), null)
        );
    }

}
