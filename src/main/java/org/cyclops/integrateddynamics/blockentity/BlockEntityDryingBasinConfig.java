package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.client.render.blockentity.RenderBlockEntityDryingBasin;

/**
 * Config for the {@link BlockEntityDryingBasin}.
 * @author rubensworks
 *
 */
public class BlockEntityDryingBasinConfig extends BlockEntityConfig<BlockEntityDryingBasin> {

    public BlockEntityDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "drying_basin",
                (eConfig) -> new BlockEntityType<>(BlockEntityDryingBasin::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_DRYING_BASIN), null)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        getMod().getProxy().registerRenderer(getInstance(), RenderBlockEntityDryingBasin::new);
    }

}
