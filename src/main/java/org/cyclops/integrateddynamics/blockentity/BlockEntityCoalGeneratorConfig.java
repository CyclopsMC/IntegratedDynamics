package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link BlockEntityCoalGenerator}.
 * @author rubensworks
 *
 */
public class BlockEntityCoalGeneratorConfig extends BlockEntityConfig<BlockEntityCoalGenerator> {

    public BlockEntityCoalGeneratorConfig() {
        super(
                IntegratedDynamics._instance,
                "coal_generator",
                (eConfig) -> new BlockEntityType<>(BlockEntityCoalGenerator::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_COAL_GENERATOR.get()), null)
        );
        IntegratedDynamics._instance.getModEventBus().addListener(new BlockEntityCoalGenerator.CapabilityRegistrar(this::getInstance)::register);
    }

}
