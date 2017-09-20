package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.base.Optional;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.ToString;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.advancement.criterion.ValuePredicate;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Value type with values that are itemstacks.
 * @author rubensworks
 */
public class ValueObjectTypeEntity extends ValueObjectTypeBase<ValueObjectTypeEntity.ValueEntity> implements
        IValueTypeNamed<ValueObjectTypeEntity.ValueEntity>, IValueTypeNullable<ValueObjectTypeEntity.ValueEntity> {

    public ValueObjectTypeEntity() {
        super("entity");
    }

    @Override
    public ValueEntity getDefault() {
        return ValueEntity.of((UUID) null);
    }

    @Override
    public String toCompactString(ValueEntity value) {
        Optional<Entity> entity = value.getRawValue();
        if(entity.isPresent()) {
            Entity e = entity.get();
            if(e instanceof EntityItem) {
                return ((EntityItem) e).getItem().getDisplayName();
            } else {
                return e.getName();
            }
        }
        return "";
    }

    @Override
    public String serialize(ValueEntity value) {
        Optional<UUID> uuid = value.getUuid();
        if(uuid.isPresent()) {
            return uuid.get().toString();
        }
        return "";
    }

    @Override
    public ValueEntity deserialize(String value) {
        try {
            return ValueEntity.of(UUID.fromString(value));
        } catch (IllegalArgumentException e) {}
        return ValueEntity.of((UUID) null);
    }

    @Override
    public String getName(ValueEntity a) {
        return toCompactString(a);
    }

    @Override
    public boolean isNull(ValueEntity a) {
        return !a.getRawValue().isPresent();
    }

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return null;
    }

    @Override
    public ValuePredicate<ValueEntity> deserializeValuePredicate(JsonObject element, @Nullable IValue value) {
        JsonElement jsonElement = element.get("entity_class");
        String className = jsonElement != null && !jsonElement.isJsonNull() ? jsonElement.getAsString() : null;
        Class<?> clazz = null;
        if (className != null) {
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new JsonSyntaxException("Could not find the container class with name '" + className + "'");
            }
            if (clazz.isAssignableFrom(Entity.class)) {
                throw new JsonSyntaxException("The class '" + className + "' is not an entity class");
            }
        }
        return new ValueEntityPredicate(this, value, (Class<? extends Entity>) clazz);
    }

    @ToString
    public static class ValueEntity extends ValueBase {

        private final Optional<UUID> value;

        protected ValueEntity(@Nullable Entity value) {
            super(ValueTypes.OBJECT_ENTITY);
            this.value = value == null ? Optional.<UUID>absent() : Optional.of(value.getUniqueID());
        }

        private ValueEntity(@Nullable UUID entityUuid) {
            super(ValueTypes.OBJECT_ENTITY);
            this.value = Optional.fromNullable(entityUuid);
        }

        /**
         * @return The raw value in an optional holder.
         */
        public Optional<Entity> getRawValue() {
            Optional<UUID> uuid = getUuid();
            if (uuid.isPresent()) {
                if (MinecraftHelpers.isClientSide()) {
                    for (Entity entity : FMLClientHandler.instance().getWorldClient().getLoadedEntityList()) {
                        if (entity.getUniqueID().equals(uuid.get())) {
                            return Optional.of(entity);
                        }
                    }
                    return Optional.absent();
                }
                return Optional.fromNullable(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityFromUuid(uuid.get()));
            }
            return Optional.absent();
        }

        public Optional<UUID> getUuid() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof ValueEntity) {
                if (((ValueEntity) o).value.isPresent() && value.isPresent()) {
                    return ((ValueEntity) o).value.get().equals(value.get());
                } else if (!((ValueEntity) o).value.isPresent() && !value.isPresent()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getType().hashCode() + (getRawValue().isPresent() ? getRawValue().get().hashCode() : 0);
        }

        public static ValueEntity of(@Nullable Entity entity) {
            return new ValueEntity(entity);
        }

        public static ValueEntity of(@Nullable UUID entityUuid) {
            return new ValueEntity(entityUuid);
        }

    }

    public static class ValueEntityPredicate extends ValuePredicate<ValueEntity> {

        private final Class<? extends Entity> clazz;

        public ValueEntityPredicate(@Nullable IValueType valueType, @Nullable IValue value, @Nullable Class<? extends Entity> clazz) {
            super(valueType, value);
            this.clazz = clazz;
        }

        @Override
        protected boolean testTyped(ValueEntity value) {
            return super.testTyped(value)
                    && (clazz == null
                        || (value.getRawValue().isPresent() && clazz.isInstance(value.getRawValue().get())));
        }
    }

}
