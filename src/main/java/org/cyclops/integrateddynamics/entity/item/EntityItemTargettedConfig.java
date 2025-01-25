package org.cyclops.integrateddynamics.entity.item;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.cyclops.cyclopscore.config.extendedconfig.EntityClientConfig;
import org.cyclops.cyclopscore.config.extendedconfig.EntityConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the {@link EntityItemTargetted}.
 * @author rubensworks
 *
 */
public class EntityItemTargettedConfig extends EntityConfigCommon<IntegratedDynamics, EntityItemTargetted> {

    public EntityItemTargettedConfig() {
        super(
                IntegratedDynamics._instance,
                "item_targetted",
                eConfig -> EntityType.Builder.<EntityItemTargetted>of(EntityItemTargetted::new, MobCategory.MISC)
                        .fireImmune()
                        .setShouldReceiveVelocityUpdates(true)
                        .sized(0.25F, 0.25F)
        );
    }

    @Override
    public EntityClientConfig<IntegratedDynamics, EntityItemTargetted> constructEntityClientConfig() {
        return new EntityItemTargettedConfigClient(this);
    }
}
