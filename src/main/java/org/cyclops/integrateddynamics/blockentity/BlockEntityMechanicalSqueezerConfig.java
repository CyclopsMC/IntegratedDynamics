package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link BlockEntityMechanicalSqueezer}.
 * @author rubensworks
 *
 */
public class BlockEntityMechanicalSqueezerConfig extends BlockEntityConfig<BlockEntityMechanicalSqueezer> {

    public BlockEntityMechanicalSqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_squeezer",
                (eConfig) -> new BlockEntityType<>(BlockEntityMechanicalSqueezer::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_MECHANICAL_SQUEEZER.get()), null)
        );
        IntegratedDynamics._instance.getModEventBus().addListener(this::registerCapability);
    }

    protected void registerCapability(RegisterCapabilitiesEvent event) {
        BlockEntityMechanicalSqueezer.registerMechanicalSqueezerCapabilities(event, getInstance());
    }

}
