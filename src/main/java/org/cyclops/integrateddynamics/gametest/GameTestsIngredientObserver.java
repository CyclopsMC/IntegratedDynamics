package org.cyclops.integrateddynamics.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integrateddynamics.core.network.IngredientObserver;

import java.util.concurrent.ExecutionException;

/**
 * Game tests for {@link IngredientObserver}.
 * @author rubensworks
 */
public class GameTestsIngredientObserver {

    public static final String TEMPLATE_EMPTY = "integrateddynamics:empty10";

    @GameTest(template = TEMPLATE_EMPTY)
    public void testWorkerThreadsAreDaemon(GameTestHelper helper) throws ExecutionException, InterruptedException {
        // Non-daemon worker threads would keep the client JVM alive after quitting the game.
        boolean daemon = IngredientObserver.getWorkerPool().submit(() -> Thread.currentThread().isDaemon()).get();
        helper.assertTrue(daemon, "Ingredient observer worker threads must be daemon threads");
        helper.succeed();
    }

}
