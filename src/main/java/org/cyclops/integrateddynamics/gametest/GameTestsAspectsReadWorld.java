package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.saveddata.WeatherData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.function.Supplier;

import org.cyclops.integrateddynamics.api.part.aspect.IAspectVariable;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.testReadAspect;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.testReadAspectPredicate;

public class GameTestsAspectsReadWorld {

    public static final String TEMPLATE_EMPTY = "integrateddynamics:empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    @GameTest(template = TEMPLATE_EMPTY, environment = Reference.MOD_ID + ":weather_clear")
    public void testAspectsReadWorldWeatherClearTrue(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_WEATHER_CLEAR, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY, environment = Reference.MOD_ID + ":weather_rain")
    public void testAspectsReadWorldWeatherClearFalse(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_WEATHER_CLEAR, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY, environment = Reference.MOD_ID + ":weather_rain")
    public void testAspectsReadWorldWeatherRainingTrue(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_WEATHER_RAINING, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY, environment = Reference.MOD_ID + ":weather_clear")
    public void testAspectsReadWorldWeatherRainingFalse(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_WEATHER_RAINING, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY, environment = Reference.MOD_ID + ":weather_thunder")
    public void testAspectsReadWorldWeatherThunderTrue(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_WEATHER_THUNDER, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY, environment = Reference.MOD_ID + ":weather_clear")
    public void testAspectsReadWorldWeatherThunderFalse(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_WEATHER_THUNDER, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadWorldIsDayAndIsNight(GameTestHelper helper) {
        // Day-phase cable at POS: check BOOLEAN_ISDAY=true and BOOLEAN_ISNIGHT=false at clock=1000 (day)
        Supplier<IAspectVariable> isDayTrueSupplier = GameTestHelpersIntegratedDynamics.testReadAspectSetup(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_ISDAY);
        PartPos partPosDay = PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST);
        PartHelpers.PartStateHolder<?, ?> dayStateHolder = PartHelpers.getPart(partPosDay);
        IPartTypeReader dayPartReader = (IPartTypeReader) dayStateHolder.getPart();
        IPartStateReader dayPartState = (IPartStateReader) dayStateHolder.getState();
        Supplier<IAspectVariable> isNightFalseSupplier = () -> dayPartReader.getVariable(PartTarget.fromCenter(partPosDay), dayPartState, Aspects.Read.World.BOOLEAN_ISNIGHT);

        // Night-phase cable at POS.offset(2,0,0): check BOOLEAN_ISDAY=false and BOOLEAN_ISNIGHT=true at clock=13000 (night)
        // A separate cable is used so that the night-phase variables are computed fresh (no cached values from the day phase)
        BlockPos posNight = POS.offset(2, 0, 0);
        Supplier<IAspectVariable> isDayFalseSupplier = GameTestHelpersIntegratedDynamics.testReadAspectSetup(posNight, helper, PartTypes.WORLD_READER, Aspects.Read.World.BOOLEAN_ISDAY);
        PartPos partPosNight = PartPos.of(helper.getLevel(), helper.absolutePos(posNight), Direction.WEST);
        PartHelpers.PartStateHolder<?, ?> nightStateHolder = PartHelpers.getPart(partPosNight);
        IPartTypeReader nightPartReader = (IPartTypeReader) nightStateHolder.getPart();
        IPartStateReader nightPartState = (IPartStateReader) nightStateHolder.getState();
        Supplier<IAspectVariable> isNightTrueSupplier = () -> nightPartReader.getVariable(PartTarget.fromCenter(partPosNight), nightPartState, Aspects.Read.World.BOOLEAN_ISNIGHT);

        // Set clock to day; after 1 tick skyDarken reflects the new value
        helper.getLevel().registryAccess().get(WorldClocks.OVERWORLD).ifPresent(clockHolder ->
                ((ServerLevel) helper.getLevel()).clockManager().setTotalTicks(clockHolder, 1000L));
        helper.runAfterDelay(1, () -> {
            GameTestHelpersIntegratedDynamics.assertValueEqual(helper, isDayTrueSupplier.get(), ValueTypeBoolean.ValueBoolean.of(true));
            GameTestHelpersIntegratedDynamics.assertValueEqual(helper, isNightFalseSupplier.get(), ValueTypeBoolean.ValueBoolean.of(false));

            // Set clock to night; after 1 tick skyDarken reflects the new value
            helper.getLevel().registryAccess().get(WorldClocks.OVERWORLD).ifPresent(clockHolder ->
                    ((ServerLevel) helper.getLevel()).clockManager().setTotalTicks(clockHolder, 13000L));
            helper.runAfterDelay(1, () -> {
                GameTestHelpersIntegratedDynamics.assertValueEqual(helper, isDayFalseSupplier.get(), ValueTypeBoolean.ValueBoolean.of(false));
                GameTestHelpersIntegratedDynamics.assertValueEqual(helper, isNightTrueSupplier.get(), ValueTypeBoolean.ValueBoolean.of(true));
                helper.succeed();
            });
        });
    }

    @GameTest(template = TEMPLATE_EMPTY, environment = Reference.MOD_ID + ":weather_clear2")
    public void testAspectsReadWorldRainCountdown(GameTestHelper helper) {
        WeatherData weatherData = helper.getLevel().getWeatherData();
        weatherData.setRaining(true);
        weatherData.setRainTime(123);
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.INTEGER_RAINCOUNTDOWN, ValueTypeInteger.ValueInteger.of(123));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadWorldTicktime(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.INTEGER_TICKTIME, ValueTypeInteger.ValueInteger.of((int) (Helpers.mean(ServerLifecycleHooks.getCurrentServer().getTickTime(helper.getLevel().dimension())) * 1.0E-6D)));
    }

    @GameTest(template = TEMPLATE_EMPTY, environment = Reference.MOD_ID + ":time_day_far")
    public void testAspectsReadWorldDaytime(GameTestHelper helper) {
        long targetTicks = 1000 + 10L * IModHelpers.get().getMinecraftHelpers().getDayLength();
        helper.getLevel().registryAccess().get(WorldClocks.OVERWORLD).ifPresent(clockHolder ->
                ((ServerLevel) helper.getLevel()).clockManager().setTotalTicks(clockHolder, targetTicks));
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

    @GameTest(template = TEMPLATE_EMPTY, environment = Reference.MOD_ID + ":time_day_far")
    public void testAspectsReadWorldTime(GameTestHelper helper) {
        long targetTicks = 1000 + 10L * IModHelpers.get().getMinecraftHelpers().getDayLength();
        helper.getLevel().registryAccess().get(WorldClocks.OVERWORLD).ifPresent(clockHolder ->
                ((ServerLevel) helper.getLevel()).clockManager().setTotalTicks(clockHolder, targetTicks));
        testReadAspect(POS, helper, PartTypes.WORLD_READER, Aspects.Read.World.LONG_TIME, ValueTypeLong.ValueLong.of(1000 + 10 * IModHelpers.get().getMinecraftHelpers().getDayLength()));
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
