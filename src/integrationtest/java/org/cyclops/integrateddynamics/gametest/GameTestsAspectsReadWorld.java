package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.testReadAspect;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.testReadAspectPredicate;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsAspectsReadWorld {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    // Weather tests must be run in different batches, as they conflict each other otherwise.

    @GameTest(template = TEMPLATE_EMPTY, batch = "weather1")
    public void testAspectsReadWorldWeatherClearTrue(GameTestHelper helper) {
        helper.getLevel().setWeatherParameters(0, 0, false, false);
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_WEATHER_CLEAR, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY, batch = "weather2")
    public void testAspectsReadWorldWeatherClearFalse(GameTestHelper helper) {
        helper.getLevel().setWeatherParameters(0, 0, true, false);
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_WEATHER_CLEAR, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY, batch = "weather3")
    public void testAspectsReadWorldWeatherRainingTrue(GameTestHelper helper) {
        helper.getLevel().setWeatherParameters(0, 0, true, false);
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_WEATHER_RAINING, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY, batch = "weather4")
    public void testAspectsReadWorldWeatherRainingFalse(GameTestHelper helper) {
        helper.getLevel().setWeatherParameters(0, 0, false, false);
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_WEATHER_RAINING, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY, batch = "weather5")
    public void testAspectsReadWorldWeatherThunderTrue(GameTestHelper helper) {
        helper.getLevel().setWeatherParameters(0, 0, true, true);
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_WEATHER_THUNDER, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY, batch = "weather6")
    public void testAspectsReadWorldWeatherThunderFalse(GameTestHelper helper) {
        helper.getLevel().setWeatherParameters(0, 0, false, false);
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_WEATHER_THUNDER, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY, batch = "daynight1")
    public void testAspectsReadWorldIsDayTrue(GameTestHelper helper) {
        helper.getLevel().setDayTime(1000);
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_ISDAY, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY, batch = "daynight2")
    public void testAspectsReadWorldIsDayFalse(GameTestHelper helper) {
        helper.getLevel().setDayTime(13000);
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_ISDAY, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY, batch = "daynight3")
    public void testAspectsReadWorldIsNightTrue(GameTestHelper helper) {
        helper.getLevel().setDayTime(13000);
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_ISNIGHT, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY, batch = "daynight4")
    public void testAspectsReadWorldIsNightFalse(GameTestHelper helper) {
        helper.getLevel().setDayTime(1000);
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_ISNIGHT, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY, batch = "weather7")
    public void testAspectsReadWorldRainCountdown(GameTestHelper helper) {
        helper.getLevel().setWeatherParameters(0, 123, true, false);
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.INTEGER_RAINCOUNTDOWN, ValueTypeInteger.ValueInteger.of(123));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadWorldTicktime(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.INTEGER_TICKTIME, ValueTypeInteger.ValueInteger.of((int) (Helpers.mean(ServerLifecycleHooks.getCurrentServer().getTickTime(helper.getLevel().dimension())) * 1.0E-6D)));
    }

    @GameTest(template = TEMPLATE_EMPTY, batch = "daynight5")
    public void testAspectsReadWorldDaytime(GameTestHelper helper) {
        helper.getLevel().setDayTime(1000 + 10 * MinecraftHelpers.MINECRAFT_DAY);
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.INTEGER_DAYTIME, ValueTypeInteger.ValueInteger.of(1000));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadWorldLightlevel(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.INTEGER_LIGHTLEVEL, ValueTypeInteger.ValueInteger.of(helper.getLevel().getMaxLocalRawBrightness(helper.absolutePos(POS.west()))));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadWorldTps(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.DOUBLE_TPS, ValueTypeDouble.ValueDouble.of(20));
    }

    @GameTest(template = TEMPLATE_EMPTY, batch = "daynight6")
    public void testAspectsReadWorldTime(GameTestHelper helper) {
        helper.getLevel().setDayTime(1000 + 10 * MinecraftHelpers.MINECRAFT_DAY);
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.LONG_TIME, ValueTypeLong.ValueLong.of(1000 + 10 * MinecraftHelpers.MINECRAFT_DAY));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadWorldTotalTime(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.LONG_TOTALTIME, ValueTypeLong.ValueLong.of(helper.getLevel().getGameTime()));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadWorldName(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.STRING_NAME, ValueTypeString.ValueString.of(((ServerLevelData) helper.getLevel().getLevelData()).getLevelName()));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadWorldPlayers(GameTestHelper helper) {
        testReadAspectPredicate(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.LIST_PLAYERS, players -> players.getRawValue() != null);
    }

}
