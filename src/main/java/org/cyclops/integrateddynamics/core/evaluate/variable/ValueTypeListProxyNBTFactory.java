package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Factory for list proxies that implement {@link org.cyclops.cyclopscore.persist.nbt.INBTProvider}.
 * @author rubensworks
 */
public class ValueTypeListProxyNBTFactory<T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V> & INBTProvider> implements IValueTypeListProxyFactoryTypeRegistry.IProxyFactory<T, V, P> {

    private final ResourceLocation name;
    private final Class<P> proxyClass;
    private final Constructor<P> proxyClassConstructor;

    public ValueTypeListProxyNBTFactory(ResourceLocation name, Class<P> proxyClass) {
        this.name = name;
        this.proxyClass = proxyClass;

        try {
            this.proxyClassConstructor = this.proxyClass.getConstructor();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format(
                    "Could not find a default constructor for %s, while this is required for list proxies. This is a developer error.", proxyClass.getName()));
        }
    }

    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    protected Class<P> getProxyClass() {
        return this.proxyClass;
    }

    @Override
    public INBT serialize(P values) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        CompoundNBT tag = new CompoundNBT();
        values.writeGeneratedFieldsToNBT(tag);
        return tag;
    }

    @Override
    public P deserialize(INBT value) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        try {
            P proxy = this.proxyClassConstructor.newInstance();
            proxy.readGeneratedFieldsFromNBT((CompoundNBT) value);
            return proxy;
        } catch (InvocationTargetException | InstantiationException | ClassCastException | IllegalAccessException e) {
            e.printStackTrace();
            throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(e.getMessage());
        }
    }
}
