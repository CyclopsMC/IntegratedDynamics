package org.cyclops.integrateddynamics.entity.item;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
                eConfig -> EntityType.Builder.<EntityItemTargetted>create(EntityItemTargetted::new, EntityClassification.MISC)
                        .immuneToFire()
                        .setShouldReceiveVelocityUpdates(true)
                        .size(0.25F, 0.25F)
        );
    }

    @OnlyIn(Dist.CLIENT)
	@Override
	public EntityRenderer<EntityItemTargetted> getRender(EntityRendererManager renderManager, ItemRenderer renderItem) {
        return (EntityRenderer) new net.minecraft.client.renderer.entity.ItemRenderer(renderManager, renderItem);
	}
}
