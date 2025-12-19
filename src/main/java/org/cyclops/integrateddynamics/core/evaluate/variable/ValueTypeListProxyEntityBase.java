package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

/**
 * A list proxy for the something of an entity.
 */
public abstract class ValueTypeListProxyEntityBase<T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyBase<T, V> implements INBTProvider {

    private String world;
    private int entity;

    public ValueTypeListProxyEntityBase(Identifier name, T valueType, Level world, Entity entity) {
        super(name, valueType);
        this.world = (world == null ? Level.OVERWORLD : world.dimension()).identifier().toString();
        this.entity = entity == null ? -1 : entity.getId();
    }

    protected Entity getEntity() {
        ServerLevel worldServer = ServerLifecycleHooks.getCurrentServer().getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(this.world)));
        if(worldServer != null) {
            return worldServer.getEntity(entity);
        }
        return null;
    }

    @Override
    public void writeGeneratedFieldsToNBT(ValueOutput output) {
        output.putString("world", world);
        output.putInt("entity", entity);
    }

    @Override
    public void readGeneratedFieldsFromNBT(ValueInput input) {
        this.world = input.getString("world").orElseThrow();
        this.entity = input.getInt("entity").orElseThrow();
    }
}
