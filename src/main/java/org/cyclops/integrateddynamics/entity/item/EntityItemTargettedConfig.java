package org.cyclops.integrateddynamics.entity.item;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.config.extendedconfig.EntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the {@link EntityItemTargetted}.
 * @author rubensworks
 *
 */
public class EntityItemTargettedConfig extends EntityConfig<EntityItemTargetted> {

    public EntityItemTargettedConfig() {
        super(
                IntegratedDynamics._instance,
                "entityItemTargetted",
                eConfig -> EntityType.Builder.<EntityItemTargetted>of(EntityItemTargetted::new, MobCategory.MISC)
                        .fireImmune()
                        .setShouldReceiveVelocityUpdates(true)
                        .sized(0.25F, 0.25F)
        );
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public EntityRenderer<EntityItemTargetted> getRender(EntityRendererProvider.Context renderContext, ItemRenderer renderItem) {
        return (EntityRenderer) new net.minecraft.client.renderer.entity.ItemEntityRenderer(renderContext);
    }
}
