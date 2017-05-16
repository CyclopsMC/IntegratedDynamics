package org.cyclops.integrateddynamics.entity.item;

import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.extendedconfig.EntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the {@link EntityItemTargetted}.
 * @author rubensworks
 *
 */
public class EntityItemTargettedConfig extends EntityConfig<Entity> {
    
    /**
     * The unique instance.
     */
    public static EntityItemTargettedConfig _instance;

    /**
     * Make a new instance.
     */
    public EntityItemTargettedConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "entityItemTargetted",
                null,
                EntityItemTargetted.class
        );
    }
    
    @Override
    public boolean sendVelocityUpdates() {
        return true;
    }

    @SideOnly(Side.CLIENT)
	@Override
	public Render getRender(RenderManager renderManager, RenderItem renderItem) {
        return new RenderEntityItem(renderManager, renderItem);
	}
}
