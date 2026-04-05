package org.cyclops.integrateddynamics.gametest.integration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.cyclops.integrateddynamics.core.test.IntegrationBefore;
import org.cyclops.integrateddynamics.core.test.IntegrationTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

/**
 * @author rubensworks
 */
public class IntegrationMethodGameTestInstance extends GameTestInstance {

    public static final MapCodec<? extends GameTestInstance> CODEC = RecordCodecBuilder.<IntegrationMethodGameTestInstance>mapCodec(instance -> instance.group(
            TestData.CODEC.forGetter(IntegrationMethodGameTestInstance::info),
            Codec.STRING.fieldOf("modId").forGetter(IntegrationMethodGameTestInstance::getModId),
            Codec.STRING.fieldOf("class").forGetter(IntegrationMethodGameTestInstance::getClassName),
            Codec.STRING.fieldOf("method").forGetter(IntegrationMethodGameTestInstance::getMethodName)
    ).apply(instance, IntegrationMethodGameTestInstance::new));

    private final String modId;
    private final String className;
    private final String methodName;

    public IntegrationMethodGameTestInstance(TestData<Holder<TestEnvironmentDefinition<?>>> info, String modId, String className, String methodName) {
        super(info);
        this.modId = modId;
        this.className = className;
        this.methodName = methodName;
    }

    public String getModId() {
        return modId;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public void run(GameTestHelper gameTestHelper) {
        try {
            Class<?> clazz = Class.forName(this.className);
            Object instance = clazz.newInstance();

            // Call befores
            List<Method> befores = com.google.common.collect.Lists.newLinkedList();
            for(Method method : clazz.getDeclaredMethods()) {
                if(method.isAnnotationPresent(IntegrationBefore.class)) {
                    befores.add(method);
                }
            }

            Method method = clazz.getMethod(this.methodName);

            gameTestHelper.succeedIf(() -> {
                GameTester.GAME_TEST_HELPER = gameTestHelper;
                try {
                    for(Method before : befores) {
                        before.invoke(instance);
                    }
                    method.invoke(instance);
                } catch (InvocationTargetException e) {
                    Class<?> excepted = method.getAnnotation(IntegrationTest.class).expected();
                    if(!excepted.isInstance(e.getTargetException())) {
                        if (e.getTargetException() instanceof IllegalStateException || e.getTargetException() instanceof AssertionError) {
                            e.getTargetException().printStackTrace();
                            throw new GameTestAssertException(Component.literal("Test " + getId().toString() + " failed!"), (int) gameTestHelper.getTick());
                        } else {
                            e.getTargetException().printStackTrace();
                            throw new GameTestAssertException(Component.literal(String.format("Expected at %s exception %s, but found:", getId().toString(), e)), (int) gameTestHelper.getTick());
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new GameTestAssertException(Component.literal(e.getMessage()), (int) gameTestHelper.getTick());
                }
            });
        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new GameTestAssertException(Component.literal(e.getMessage()), (int) gameTestHelper.getTick());
        }
    }

    @Override
    public MapCodec<? extends GameTestInstance> codec() {
        return CODEC;
    }

    @Override
    protected MutableComponent typeDescription() {
        return Component.literal("Method-based test instance for " + getClassName() + "." + getMethodName());
    }

    public Identifier getId() {
        return Identifier.fromNamespaceAndPath(this.modId, (this.className.replaceAll("org.cyclops\\.[^.]*\\.[^.]*\\.", "") + "." + this.methodName).toLowerCase(Locale.ROOT).replace('.', '_'));
    }
}
