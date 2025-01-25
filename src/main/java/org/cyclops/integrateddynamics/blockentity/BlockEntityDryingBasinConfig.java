package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.client.render.blockentity.RenderBlockEntityDryingBasin;

/**
 * Config for the {@link BlockEntityDryingBasin}.
 * @author rubensworks
 *
 */
public class BlockEntityDryingBasinConfig extends BlockEntityConfigCommon<BlockEntityDryingBasin, IntegratedDynamics> {

    public BlockEntityDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "drying_basin",
                (eConfig) -> new BlockEntityType<>(BlockEntityDryingBasin::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_DRYING_BASIN.get()))
        );
        IntegratedDynamics._instance.getModEventBus().addListener(new BlockEntityDryingBasin.CapabilityRegistrar(this::getInstance)::register);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        getMod().getProxy().registerRenderer(getInstance(), RenderBlockEntityDryingBasin::new);
    }

}
