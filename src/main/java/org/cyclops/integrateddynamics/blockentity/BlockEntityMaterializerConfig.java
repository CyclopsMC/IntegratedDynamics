package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link BlockEntityMaterializer}.
 * @author rubensworks
 *
 */
public class BlockEntityMaterializerConfig extends BlockEntityConfig<BlockEntityMaterializer> {

    public BlockEntityMaterializerConfig() {
        super(
                IntegratedDynamics._instance,
                "materializer",
                (eConfig) -> new BlockEntityType<>(BlockEntityMaterializer::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_MATERIALIZER.get()), null)
        );
        IntegratedDynamics._instance.getModEventBus().addListener(this::registerCapability);
    }

    protected void registerCapability(RegisterCapabilitiesEvent event) {
        BlockEntityMaterializer.registerMaterializerCapabilities(event, getInstance());
    }

}
