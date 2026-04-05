package org.cyclops.integrateddynamics.entity.item;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.cyclops.cyclopscore.config.extendedconfig.EntityClientConfig;
import org.cyclops.cyclopscore.config.extendedconfig.EntityConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class EntityItemTargettedConfigClient extends EntityClientConfig<IntegratedDynamics, EntityItemTargetted> {
    public EntityItemTargettedConfigClient(EntityConfigCommon<IntegratedDynamics, EntityItemTargetted> entityConfig) {
        super(entityConfig);
    }

    @Override
    public EntityRenderer<? super EntityItemTargetted, ?> getRender(EntityRendererProvider.Context renderContext) {
        return new net.minecraft.client.renderer.entity.ItemEntityRenderer(renderContext);
    }
}
