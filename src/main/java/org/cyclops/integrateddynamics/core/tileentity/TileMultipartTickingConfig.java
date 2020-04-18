package org.cyclops.integrateddynamics.core.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.client.render.tileentity.RenderCable;

/**
 * Config for the {@link TileMultipartTicking}.
 * @author rubensworks
 *
 */
public class TileMultipartTickingConfig extends TileEntityConfig<TileMultipartTicking> {

    public TileMultipartTickingConfig() {
        super(
                IntegratedDynamics._instance,
                "multipart_ticking",
                (eConfig) -> new TileEntityType<>(TileMultipartTicking::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_CABLE), null)
        );
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        if(MinecraftHelpers.isClientSide()) {
            registerClientSide();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void registerClientSide() {
        IntegratedDynamics._instance.getProxy().registerRenderer(getInstance(), RenderCable::new);
    }

}
