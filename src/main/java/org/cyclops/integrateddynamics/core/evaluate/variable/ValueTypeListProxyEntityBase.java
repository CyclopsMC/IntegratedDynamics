package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

/**
 * A list proxy for the something of an entity.
 */
public abstract class ValueTypeListProxyEntityBase<T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyBase<T, V> implements INBTProvider {

    private String world;
    private int entity;

    public ValueTypeListProxyEntityBase(ResourceLocation name, T valueType, Level world, Entity entity) {
        super(name, valueType);
        this.world = (world == null ? Level.OVERWORLD : world.dimension()).location().toString();
        this.entity = entity == null ? -1 : entity.getId();
    }

    protected Entity getEntity() {
        ServerLevel worldServer = ServerLifecycleHooks.getCurrentServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(this.world)));
        if(worldServer != null) {
            return worldServer.getEntity(entity);
        }
        return null;
    }

    @Override
    public void writeGeneratedFieldsToNBT(CompoundTag tag) {
        tag.putString("world", world);
        tag.putInt("entity", entity);
    }

    @Override
    public void readGeneratedFieldsFromNBT(CompoundTag tag) {
        this.world = tag.getString("world");
        this.entity = tag.getInt("entity");
    }
}
