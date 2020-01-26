package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

/**
 * A list proxy for the something of an entity.
 */
public abstract class ValueTypeListProxyEntityBase<T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyBase<T, V> implements INBTProvider {

    private String world;
    private int entity;

    public ValueTypeListProxyEntityBase(ResourceLocation name, T valueType, World world, Entity entity) {
        super(name, valueType);
        this.world = (world == null ? DimensionType.OVERWORLD : world.getDimension().getType()).getRegistryName().toString();
        this.entity = entity == null ? -1 : entity.getEntityId();
    }

    protected Entity getEntity() {
        ServerWorld worldServer = ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.byName(new ResourceLocation(this.world)));
        if(worldServer != null) {
            return worldServer.getEntityByID(entity);
        }
        return null;
    }

    @Override
    public void writeGeneratedFieldsToNBT(CompoundNBT tag) {
        tag.putString("world", world);
        tag.putInt("entity", entity);
    }

    @Override
    public void readGeneratedFieldsFromNBT(CompoundNBT tag) {
        this.world = tag.getString("world");
        this.entity = tag.getInt("entity");
    }
}
