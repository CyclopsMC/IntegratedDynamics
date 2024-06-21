package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.client.render.blockentity.RenderBlockEntitySqueezer;

/**
 * Config for the {@link BlockEntitySqueezer}.
 * @author rubensworks
 *
 */
public class BlockEntitySqueezerConfig extends BlockEntityConfig<BlockEntitySqueezer> {

    public BlockEntitySqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "squeezer",
                (eConfig) -> new BlockEntityType<>(BlockEntitySqueezer::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_SQUEEZER.get()), null)
        );
        IntegratedDynamics._instance.getModEventBus().addListener(new BlockEntitySqueezer.CapabilityRegistrar(this::getInstance)::register);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        getMod().getProxy().registerRenderer(getInstance(), RenderBlockEntitySqueezer::new);
    }

}
