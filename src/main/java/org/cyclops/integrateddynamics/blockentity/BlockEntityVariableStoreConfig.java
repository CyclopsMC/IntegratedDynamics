package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link BlockEntityVariablestore}.
 * @author rubensworks
 *
 */
public class BlockEntityVariableStoreConfig extends BlockEntityConfig<BlockEntityVariablestore> {

    public BlockEntityVariableStoreConfig() {
        super(
                IntegratedDynamics._instance,
                "variable_store",
                (eConfig) -> new BlockEntityType<>(BlockEntityVariablestore::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_VARIABLE_STORE), null)
        );
    }

}
