package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.ModConfigLocation;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link BlockEntityCoalGenerator}.
 * @author rubensworks
 *
 */
public class BlockEntityCoalGeneratorConfig extends BlockEntityConfigCommon<BlockEntityCoalGenerator, IntegratedDynamics> {

    @ConfigurablePropertyCommon(category = "machine", comment = "The energy production rate (in RF/t) of the coal generator.", minimalValue = 1, configLocation = ModConfigLocation.SERVER)
    public static int energyPerTick = 20;

    public BlockEntityCoalGeneratorConfig() {
        super(
                IntegratedDynamics._instance,
                "coal_generator",
                (eConfig) -> new BlockEntityType<>(BlockEntityCoalGenerator::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_COAL_GENERATOR.get()))
        );
        IntegratedDynamics._instance.getModEventBus().addListener(new BlockEntityCoalGenerator.CapabilityRegistrar(this::getInstance)::register);
    }

}
