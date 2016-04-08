package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

/**
 * A list proxy for the something of an entity.
 */
public abstract class ValueTypeListProxyEntityBase<T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyBase<T, V> implements INBTProvider {

    @NBTPersist
    private int world;
    @NBTPersist
    private int entity;

    public ValueTypeListProxyEntityBase(String name, T valueType) {
        this(name, valueType, null, null);
    }

    public ValueTypeListProxyEntityBase(String name, T valueType, World world, Entity entity) {
        super(name, valueType);
        this.world = world == null ? -1 : world.provider.getDimension();
        this.entity = entity == null ? -1 : entity.getEntityId();
    }

    protected Entity getEntity() {
        WorldServer[] servers = FMLCommonHandler.instance().getMinecraftServerInstance().worldServers;
        if(world < servers.length) {
            Entity e = servers[world].getEntityByID(entity);
            return e;
        }
        return null;
    }

    @Override
    public void writeGeneratedFieldsToNBT(NBTTagCompound tag) {

    }

    @Override
    public void readGeneratedFieldsFromNBT(NBTTagCompound tag) {

    }
}
