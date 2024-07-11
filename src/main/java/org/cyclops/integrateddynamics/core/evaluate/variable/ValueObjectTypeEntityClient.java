package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.UUID;

/**
 * @author rubensworks
 */
public class ValueObjectTypeEntityClient {

    public static Optional<Entity> getEntity(UUID uuid) {
        for (Entity entity : Minecraft.getInstance().level.entitiesForRendering()) {
            if (entity.getUUID().equals(uuid)) {
                return Optional.of(entity);
            }
        }
        return Optional.empty();
    }

}
