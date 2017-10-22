package org.cyclops.integrateddynamics.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.cyclops.cyclopscore.command.CommandMod;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.core.test.IntegrationBefore;
import org.cyclops.integrateddynamics.core.test.IntegrationTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Command for initiating the integration tests.
 * @author rubensworks
 *
 */
public class CommandTest extends CommandMod {

    public static final String NAME = "test";

    private static final String P = "org.cyclops.integrateddynamics.core.evaluate.variable.integration.";
    public static final List<String> CLASSES = ImmutableList.of(
            P + "TestVariables",
            P + "TestBlockOperators",
            P + "TestItemStackOperators",
            P + "TestEntityOperators",
            P + "TestFluidStackOperators",
            P + "TestIngredientsOperators",
            P + "TestRecipeOperators"
    );

    public CommandTest(ModBase mod) {
        super(mod, NAME);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] parts, BlockPos blockPos) {
        return null;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] parts) {
        sender.sendMessage(new TextComponentString("Running tests..."));
        try {
            if(!test()) {
                sender.sendMessage(new TextComponentString("There were failing tests, see results in console."));
            } else {
                sender.sendMessage(new TextComponentString("All tests succeeded!"));
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    protected boolean test() throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
        int ok = 0;
        int total = 0;
        for(String className : CLASSES) {
            Class<?> clazz = Class.forName(className);
            Object testInstance = clazz.newInstance();

            // Collect test methods
            List<Method> befores = Lists.newLinkedList();
            Map<Method, Boolean> tests = Maps.newHashMap();
            for(Method method : clazz.getDeclaredMethods()) {
                if(method.isAnnotationPresent(IntegrationBefore.class)) {
                    befores.add(method);
                }
                if(method.isAnnotationPresent(IntegrationTest.class)) {
                    tests.put(method, false);
                }
            }

            // Run tests
            for(Method test : tests.keySet()) {
                String testName = className.replace(P, "") + "#" + test.getName();
                for(Method before : befores) {
                    before.invoke(testInstance);
                }
                boolean testOk;
                try {
                    test.invoke(testInstance);
                    testOk = true;
                } catch (InvocationTargetException e) {
                    Class<?> excepted = test.getAnnotation(IntegrationTest.class).expected();
                    if(!excepted.isInstance(e.getTargetException())) {
                        testOk = false;
                        if (e.getTargetException() instanceof IllegalStateException || e.getTargetException() instanceof AssertionError) {
                            System.err.println("Test " + testName + " failed!");
                            e.getTargetException().printStackTrace();
                        } else {
                            System.err.println(String.format("Expected at %s exception %s, but found:", testName, e));
                            e.getTargetException().printStackTrace();
                        }
                    } else {
                        testOk = true;
                    }
                }
                tests.put(test, testOk);
            }

            // Count results
            for(Boolean result : tests.values()) {
                if(result) {
                    ok++;
                }
            }
            total += tests.size();
        }
        System.err.println(String.format("Tests succeeded: %s/%s", ok, total));
        return ok == total;
    }

}
