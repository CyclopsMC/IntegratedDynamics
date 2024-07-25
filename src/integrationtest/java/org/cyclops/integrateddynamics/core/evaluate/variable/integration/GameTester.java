package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.apache.commons.compress.utils.Lists;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.command.CommandTest;
import org.cyclops.integrateddynamics.core.test.IntegrationBefore;
import org.cyclops.integrateddynamics.core.test.IntegrationTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
public class GameTester {

    @GameTestGenerator
    public Collection<TestFunction> integrationTests() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        List<TestFunction> testsList = Lists.newArrayList();

        for(String className : CommandTest.CLASSES) {
            Class<?> clazz = Class.forName(className);
            Object testInstance = clazz.newInstance();

            // Collect test methods
            List<Method> befores = com.google.common.collect.Lists.newLinkedList();
            List<Method> tests = com.google.common.collect.Lists.newLinkedList();
            for(Method method : clazz.getDeclaredMethods()) {
                if(method.isAnnotationPresent(IntegrationBefore.class)) {
                    befores.add(method);
                }
                if(method.isAnnotationPresent(IntegrationTest.class)) {
                    tests.add(method);
                }
            }

            // Run tests
            for(Method test : tests) {
                String testName = className.replace(CommandTest.P, "") + "#" + test.getName();

                testsList.add(new TestFunction(
                        "defaultBatch",
                        testName,
                        "integrateddynamics:test",
                        1,
                        1,
                        true,
                        (gameTestHelpers) -> {
                            gameTestHelpers.succeedIf(() -> {
                                try {
                                    for(Method before : befores) {
                                        before.invoke(testInstance);
                                    }
                                    test.invoke(testInstance);
                                } catch (InvocationTargetException e) {
                                    Class<?> excepted = test.getAnnotation(IntegrationTest.class).expected();
                                    if(!excepted.isInstance(e.getTargetException())) {
                                        if (e.getTargetException() instanceof IllegalStateException || e.getTargetException() instanceof AssertionError) {
                                            e.getTargetException().printStackTrace();
                                            throw new GameTestAssertException("Test " + testName + " failed!");
                                        } else {
                                            e.getTargetException().printStackTrace();
                                            throw new GameTestAssertException(String.format("Expected at %s exception %s, but found:", testName, e));
                                        }
                                    }
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                    throw new GameTestAssertException(e.getMessage());
                                }
                            });
                        }
                ));
            }
        }

        return testsList;
    }

}
