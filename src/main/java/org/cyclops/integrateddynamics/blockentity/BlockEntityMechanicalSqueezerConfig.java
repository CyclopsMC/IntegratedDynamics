package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
        IntegratedDynamics._instance.getModEventBus().addListener(new BlockEntityMechanicalSqueezer.CapabilityRegistrar(this::getInstance)::register);
    }

}
