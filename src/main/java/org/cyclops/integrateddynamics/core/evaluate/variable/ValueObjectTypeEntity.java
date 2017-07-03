package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.base.Optional;
import lombok.ToString;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;

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

}
