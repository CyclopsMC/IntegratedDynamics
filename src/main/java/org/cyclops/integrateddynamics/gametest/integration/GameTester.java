package org.cyclops.integrateddynamics.gametest.integration;

import com.google.common.collect.Lists;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;
import org.cyclops.integrateddynamics.command.CommandTest;
import org.cyclops.integrateddynamics.core.test.IntegrationTest;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author rubensworks
 */
public class GameTester {

    public static GameTestHelper GAME_TEST_HELPER;

    public static void registerCommonTests(String modId, BiConsumer<Identifier, GameTestInstance> registrar, Registry<TestEnvironmentDefinition<?>> testEnvironmentRegistry) {
        for (IntegrationMethodGameTestInstance testInstance : GameTester.integrationTests(modId, testEnvironmentRegistry)) {
            registrar.accept(testInstance.getId(), testInstance);
        }
    }

    public static Collection<IntegrationMethodGameTestInstance> integrationTests(String modId, Registry<TestEnvironmentDefinition<?>> testEnvironmentRegistry) {
        List<IntegrationMethodGameTestInstance> testsList = Lists.newArrayList();

        for(String className : CommandTest.CLASSES) {
            try {
                Class<?> clazz = Class.forName(className);

                // Collect test methods
                List<Method> tests = com.google.common.collect.Lists.newLinkedList();
                for(Method method : clazz.getDeclaredMethods()) {
                    if(method.isAnnotationPresent(IntegrationTest.class)) {
                        tests.add(method);
                    }
                }

                // Run tests
                for(Method method : tests) {
                    Holder.Reference<TestEnvironmentDefinition<?>> environment = testEnvironmentRegistry.getOrThrow(ResourceKey.create(
                            Registries.TEST_ENVIRONMENT,
                            Identifier.parse("default")
                    ));
                    testsList.add(new IntegrationMethodGameTestInstance(
                            new TestData<Holder<TestEnvironmentDefinition<?>>>(
                                    environment,
                                    Identifier.parse("integrateddynamics:test"),
                                    1,
                                    1,
                                    true,
                                    Rotation.NONE
                            ),
                            modId,
                            clazz.getName(),
                            method.getName()));
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        return testsList;
    }

}
