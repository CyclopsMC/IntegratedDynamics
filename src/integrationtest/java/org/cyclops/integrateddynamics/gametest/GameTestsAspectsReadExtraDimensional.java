package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeDouble;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Objects;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.testReadAspect;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.testReadAspectPredicate;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsAspectsReadExtraDimensional {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadExtraDimensionalRandom(GameTestHelper helper) {
        testReadAspectPredicate(POS, helper, PartTypes.EXTRADIMENSIONAL_READER, Aspects.Read.ExtraDimensional.INTEGER_RANDOM, Objects::nonNull);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadExtraDimensionalPlayerCount(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.EXTRADIMENSIONAL_READER, Aspects.Read.ExtraDimensional.INTEGER_PLAYERCOUNT, ValueTypeInteger.ValueInteger.of(ServerLifecycleHooks.getCurrentServer().getPlayerCount()));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadExtraDimensionalTickTime(GameTestHelper helper) {
        testReadAspectPredicate(POS, helper, PartTypes.EXTRADIMENSIONAL_READER, Aspects.Read.ExtraDimensional.INTEGER_TICKTIME, Objects::nonNull);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadExtraDimensionalTps(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.EXTRADIMENSIONAL_READER, Aspects.Read.ExtraDimensional.DOUBLE_TPS, ValueTypeDouble.ValueDouble.of(20));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadExtraDimensionalPlayers(GameTestHelper helper) {
        testReadAspectPredicate(POS, helper, PartTypes.EXTRADIMENSIONAL_READER, Aspects.Read.ExtraDimensional.LIST_PLAYERS, Objects::nonNull);
    }

}
