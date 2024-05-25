package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link BlockEntityMechanicalDryingBasin}.
 * @author rubensworks
 *
 */
public class BlockEntityMechanicalDryingBasinConfig extends BlockEntityConfig<BlockEntityMechanicalDryingBasin> {

    public BlockEntityMechanicalDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_drying_basin",
                (eConfig) -> new BlockEntityType<>(BlockEntityMechanicalDryingBasin::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_MECHANICAL_DRYING_BASIN.get()), null)
        );
        IntegratedDynamics._instance.getModEventBus().addListener(this::registerCapability);
    }

    protected void registerCapability(RegisterCapabilitiesEvent event) {
        BlockEntityMechanicalDryingBasin.registerMechanicalDryingBasinCapabilities(event, getInstance());
    }

}
