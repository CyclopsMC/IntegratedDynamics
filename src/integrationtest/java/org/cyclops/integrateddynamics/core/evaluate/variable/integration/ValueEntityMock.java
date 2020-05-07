package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import net.minecraft.entity.Entity;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeEntity;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author rubensworks
 */
public class ValueEntityMock extends ValueObjectTypeEntity.ValueEntity {

    private final Entity entity;

    public ValueEntityMock(@Nullable Entity entity) {
        super(entity);
        this.entity = entity;
    }

    @Override
    public Optional<Entity> getRawValue() {
        return Optional.of(entity);
    }
}
