package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    public void serialize(ValueOutput valueOutput, P values) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        values.writeGeneratedFieldsToNBT(valueOutput);
    }

    @Override
    public P deserialize(ValueInput valueInput) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        try {
            P proxy = this.proxyClassConstructor.newInstance();
            proxy.readGeneratedFieldsFromNBT(valueInput);
            return proxy;
        } catch (InvocationTargetException | InstantiationException | ClassCastException | IllegalAccessException e) {
            e.printStackTrace();
            throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(e.getMessage());
        }
    }
}
