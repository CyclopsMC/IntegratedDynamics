package org.cyclops.integrateddynamics.block;


import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.PushReaction;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

import java.util.Collection;
import java.util.Collections;

/**
 * Config for {@link BlockFluidMenrilResin}.
 * @author rubensworks
 *
 */
public class BlockFluidMenrilResinConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockFluidMenrilResinConfig() {
        super(
                IntegratedDynamics._instance,
                "block_menril_resin",
                (eConfig, properties) -> new BlockFluidMenrilResin(properties
                        .replaceable()
                        .noCollision()
                        .strength(100.0F)
                        .pushReaction(PushReaction.DESTROY)
                        .noLootTable()
                        .liquid()),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

    @Override
    public Collection<java.util.function.Supplier<ItemStack>> getDefaultCreativeTabEntries() {
        return Collections.emptyList();
    }

}
