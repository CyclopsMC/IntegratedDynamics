package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.base.Optional;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.ToString;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.advancement.criterion.ValuePredicate;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;

import javax.annotation.Nullable;

/**
 * Value type with values that are itemstacks.
 * @author rubensworks
 */
public class ValueObjectTypeEntity extends ValueObjectTypeBase<ValueObjectTypeEntity.ValueEntity> implements
        IValueTypeNamed<ValueObjectTypeEntity.ValueEntity>, IValueTypeNullable<ValueObjectTypeEntity.ValueEntity> {

    private static final String DELIMITER = ";";

    public ValueObjectTypeEntity() {
        super("entity");
    }

    @Override
    public ValueEntity getDefault() {
        return ValueEntity.of(null);
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
        Optional<Entity> entity = value.getRawValue();
        if(entity.isPresent()) {
            int world = entity.get().world.provider.getDimension();
            int id = entity.get().getEntityId();
            return world + DELIMITER + id;
        }
        return "";
    }

    @Override
    public ValueEntity deserialize(String value) {
        String[] split = value.split(DELIMITER);
        Entity entity = null;
        if(split.length == 2) {
            try {
                int world = Integer.parseInt(split[0]);
                int id = Integer.parseInt(split[1]);
                if(MinecraftHelpers.isClientSide()) {
                    entity = Minecraft.getMinecraft().world.getEntityByID(id);
                } else {
                    WorldServer[] servers = FMLCommonHandler.instance().getMinecraftServerInstance().worlds;
                    if (servers.length > world) {
                        entity = servers[world].getEntityByID(id);
                    }
                }
            } catch (NumberFormatException e) {}
        }
        return ValueEntity.of(entity);
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
    public static class ValueEntity extends ValueOptionalBase<Entity> {

        private ValueEntity(Entity entity) {
            super(ValueTypes.OBJECT_ENTITY, entity);
        }

        public static ValueEntity of(Entity entity) {
            return new ValueEntity(entity);
        }

        @Override
        protected boolean isEqual(Entity a, Entity b) {
            return a.getEntityId() == b.getEntityId();
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
