package org.cyclops.integrateddynamics.block;


import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

import java.util.Collection;
import java.util.Collections;

/**
 * Config for {@link BlockFluidLiquidChorus}.
 * @author rubensworks
 *
 */
public class BlockFluidLiquidChorusConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockFluidLiquidChorusConfig() {
        super(
                IntegratedDynamics._instance,
                "block_liquid_chorus",
                (eConfig, properties) -> new BlockFluidLiquidChorus(properties
                        .noCollision()
                        .strength(100.0F)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

    @Override
    public Collection<java.util.function.Supplier<ItemStack>> getDefaultCreativeTabEntries() {
        return Collections.emptyList();
    }

}
