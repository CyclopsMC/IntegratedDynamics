package org.cyclops.integrateddynamics;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.event.config.ModConfigEvent;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.ModConfigLocation;
import org.cyclops.cyclopscore.config.extendedconfig.DummyConfigCommon;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.part.PartTypes;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A config with general options for this mod.
 * @author rubensworks
 *
 */
public class GeneralConfig extends DummyConfigCommon<IntegratedDynamics> {

    @ConfigurablePropertyCommon(category = "core", comment = "If an anonymous mod startup analytics request may be sent to our analytics service.")
    public static boolean analytics = true;

    @ConfigurablePropertyCommon(category = "core", comment = "If the version checker should be enabled.")
    public static boolean versionChecker = true;

    @ConfigurablePropertyCommon(category = "machine", comment = "The default update frequency in ticks to use for new parts.", minimalValue = 1, configLocation = ModConfigLocation.SERVER)
    public static int defaultPartUpdateFreq = 1;

    @ConfigurablePropertyCommon(category = "general", comment = "The energy usage multiplier for networks.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int energyConsumptionMultiplier = 0;

    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the audio reader.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int audioReaderBaseConsumption = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the audio writer.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int audioWriterBaseConsumption = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the block reader.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int blockReaderBaseConsumption = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the mono-directional connector.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int connectorMonoDirectionalBaseConsumption = 32;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the omni-directional connector.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int connectorOmniDirectionalBaseConsumption = 128;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the effect writer.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int effectWriterBaseConsumption = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the entity reader.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int entityReaderBaseConsumption = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the entity writer.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int entityWriterBaseConsumption = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the extra-dimensional reader.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int extraDimensionalReaderBaseConsumption = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the fluid reader.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int fluidReaderBaseConsumption = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the inventory reader.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int inventoryReaderBaseConsumption = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the inventory writer.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int inventoryWriterBaseConsumption = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the machine reader.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int machineReaderBaseConsumption = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the machine writer.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int machineWriterBaseConsumption = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the materializer.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int materializerBaseConsumption = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the network reader.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int networkReaderBaseConsumption = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the display panel when it has a variable.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int panelDisplayBaseConsumptionEnabled = 2;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the display panel when does not have a variable.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int panelDisplayBaseConsumptionDisabled = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the dynamic light panel.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int panelLightDynamicBaseConsumption = 0;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the static light panel.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int panelLightStaticBaseConsumption = 0;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the delayer.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int delayerBaseConsumption = 2;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the proxy.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int proxyBaseConsumption = 2;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the redstone reader.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int redstoneReaderBaseConsumption = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the redstone writer.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int redstoneWriterBaseConsumption = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the variable store.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int variablestoreBaseConsumption = 4;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the world reader.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int worldReaderBaseConsumption = 1;

    @ConfigurablePropertyCommon(category = "general", comment = "The maximum render distance for part overlays to render. The higher, the more resource intensive.", isCommandable = true, minimalValue = 1, configLocation = ModConfigLocation.CLIENT)
    public static int partOverlayRenderdistance = 15;

    @ConfigurablePropertyCommon(category = "core", comment = "If cable models should be cached for rendering optimization.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static boolean cacheCableModels = true;

    @ConfigurablePropertyCommon(category = "core", comment = "The maximum network energy transfer rate.", isCommandable = true, minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int energyRateLimit = Integer.MAX_VALUE;

    @ConfigurablePropertyCommon(category = "machine", comment = "The maximum offset in blocks a directional connector can look for its target.", minimalValue = 1, configLocation = ModConfigLocation.SERVER)
    public static int maxDirectionalConnectorOffset = 512;

    @ConfigurablePropertyCommon(category = "machine", comment = "The NBT tags that are not allowed to be read by displaying NBT tags or performing operations on them.", configLocation = ModConfigLocation.SERVER)
    public static List<String> nbtTagBlacklist = Lists.newArrayList(); // Tag names that have to be ignored

    @ConfigurablePropertyCommon(category = "core", comment = "When enabled, networks will stop ticking and values will not be shown and evaluated again. This can be used to fix crashing networks by temporarily enabling this option.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static boolean safeMode = false;

    @ConfigurablePropertyCommon(category = "core", comment = "The fastest possible frequency in ticks at which ingredient network should be observed.", minimalValue = 1, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int ingredientNetworkObserverFrequencyMin = 5;

    @ConfigurablePropertyCommon(category = "core", comment = "The slowest possible frequency in ticks at which ingredient network should be observed.", minimalValue = 1, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int ingredientNetworkObserverFrequencyMax = 40;

    @ConfigurablePropertyCommon(category = "core", comment = "The ingredient network observation frequency slowdown rate in ticks.", minimalValue = 1, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int ingredientNetworkObserverFrequencyIncreaseFactor = 1;

    @ConfigurablePropertyCommon(category = "core", comment = "The ingredient network observation frequency slowdown rate in ticks.", minimalValue = 1, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int ingredientNetworkObserverFrequencyDecreaseFactor = 5;

    @ConfigurablePropertyCommon(category = "core", comment = "The frequency in ticks at which ingredient network should be observed after a position's contents are changed.", minimalValue = 0, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int ingredientNetworkObserverFrequencyForced = 0;

    @ConfigurablePropertyCommon(category = "core", comment = "The number of threads that the ingredient network observer can use.", minimalValue = 1, requiresMcRestart = true, configLocation = ModConfigLocation.SERVER)
    public static int ingredientNetworkObserverThreads = 4;

    @ConfigurablePropertyCommon(category = "core", comment = "If the ingredient network observer can work on separate thread.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static boolean ingredientNetworkObserverEnableMultithreading = true;

    @ConfigurablePropertyCommon(category = "core", comment = "If network change events should be logged. Only enable this when debugging.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static boolean logChangeEvents = false;

    @ConfigurablePropertyCommon(category = "core", comment = "If variable card IDs should be logged during evaluation. This is useful for debugging crashes caused by card evaluation.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static boolean logCardEvaluation = false;

    @ConfigurablePropertyCommon(category = "core", comment = "How deep the recursion stack on an operator can become. This is to avoid game crashes when building things like the omega operator.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int operatorRecursionLimit = 256;

    @ConfigurablePropertyCommon(category = "machine", comment = "Priority list of mod id's when determining tag-based recipe outputs.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static List<String> recipeTagOutputModPriorities = Lists.newArrayList();

    @ConfigurablePropertyCommon(category = "core", comment = "If corrupted networks should automatically be restored on first tick.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static boolean recreateCorruptedNetworks = true;

    @ConfigurablePropertyCommon(category = "core", comment = "The maximum frequency at which speach messages can be played in milliseconds.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static int speachMaxFrequency = 1000;

    @ConfigurablePropertyCommon(category = "core" , comment = "When true, use the LONG number format style. Otherwise, use the SHORT style.")
    public static boolean numberCompactUseLongStyle = false;

    @ConfigurablePropertyCommon(category = "core" , comment = "The maximum number of fractional digits to include in the result of the compact operator")
    public static int numberCompactMaximumFractionDigits = 2;

    @ConfigurablePropertyCommon(category = "core" , comment = "The minimum number of fractional digits to include in the result of the compact operator")
    public static int numberCompactMinimumFractionDigits = 0;

    @ConfigurablePropertyCommon(category = "core" , comment = "The maximum number of integer digits to include in the result of the compact operator")
    public static int numberCompactMaximumIntegerDigits = 3;

    @ConfigurablePropertyCommon(category = "core" , comment = "The minimum number of integer digits to include in the result of the compact operator")
    public static int numberCompactMinimumIntegerDigits = 1;

    @ConfigurablePropertyCommon(category = "general", comment = "The default port for running the network diagnostics HTTP server.", configLocation = ModConfigLocation.CLIENT)
    public static int diagnosticsWebServerPort = 3030;

    @ConfigurablePropertyCommon(category = "machine", comment = "The maximum offset in blocks a part can target.", minimalValue = 1, configLocation = ModConfigLocation.SERVER)
    public static int maxPartOffset = 32;

    @ConfigurablePropertyCommon(category = "machine", comment = "The distance from which part offsets should be shown.", minimalValue = 1, configLocation = ModConfigLocation.SERVER)
    public static int partOffsetRenderDistance = 16;

    @ConfigurablePropertyCommon(category = "machine", comment = "The maximum values that Part Offset items will have when dropped from a broken part.", minimalValue = 1, configLocation = ModConfigLocation.SERVER)
    public static int enchancementOffsetPartDropValue = 4;

    @ConfigurablePropertyCommon(category = "machine" , comment = "When true, disable the collision for cable.", configLocation = ModConfigLocation.SERVER)
    public static boolean disableCableCollision = false;

    @ConfigurablePropertyCommon(category = "general", comment = "The minimum update interval to enforce for all parts, in number of ticks.", configLocation = ModConfigLocation.SERVER)
    public static int partsMinimumUpdateInterval = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The minimum update intervals to enforce for specific parts. You can add entries in the form of 'integrateddynamics:machine_reader:10', where '10' refers to the number of ticks.", configLocation = ModConfigLocation.SERVER)
    public static List<String> partMinimumUpdateIntervals = Lists.newArrayList();
    public static Map<IPartType<?, ?>, Integer> partMinimumUpdateIntervalsMap = Maps.newIdentityHashMap();

    @ConfigurablePropertyCommon(category = "core" , comment = "When true, network elements will tick, irrespective of whether their chunk is loaded or not.", configLocation = ModConfigLocation.SERVER, isCommandable = true)
    public static boolean tickUnloadedNetworkElements = false;

    public GeneralConfig() {
        super(IntegratedDynamics._instance, "general");
        IntegratedDynamics._instance.getModEventBus().addListener(this::onConfigLoad);
        IntegratedDynamics._instance.getModEventBus().addListener(this::onConfigReload);
    }

    public void onConfigLoad(ModConfigEvent.Loading event) {
        if (Objects.equals(event.getConfig().getModId(), Reference.MOD_ID)) {
            recreatePartMinimumUpdateIntervals();
        }
    }

    public void onConfigReload(ModConfigEvent.Loading event) {
        if (Objects.equals(event.getConfig().getModId(), Reference.MOD_ID)) {
            recreatePartMinimumUpdateIntervals();
        }
    }

    private void recreatePartMinimumUpdateIntervals() {
        partMinimumUpdateIntervalsMap = Maps.newIdentityHashMap();
        for (String entry : partMinimumUpdateIntervals) {
            int lastColon = entry.lastIndexOf(":");
            if (lastColon == -1) {
                IntegratedDynamics.clog(Level.WARN, "Error while reloading config entry partMinimumUpdateIntervals: Invalid entry " + entry);
                continue;
            }
            String partType = entry.substring(0, lastColon);
            int interval;
            try {
                interval = Integer.parseInt(entry.substring(lastColon + 1));
            } catch (NumberFormatException e) {
                IntegratedDynamics.clog(Level.WARN, "Error while reloading config entry partMinimumUpdateIntervals: Invalid interval integer in " + entry);
                continue;
            }
            try {
                IPartType<?, ?> part = PartTypes.REGISTRY.getPartType(Identifier.tryParse(partType));
                if (part != null) {
                    partMinimumUpdateIntervalsMap.put(part, interval);
                } else {
                    IntegratedDynamics.clog(Level.WARN, "Error while reloading config entry partMinimumUpdateIntervals: Could not find a part with id " + partType);
                }
            } catch (IdentifierException e) {
                IntegratedDynamics.clog(Level.WARN, "Error while reloading config entry partMinimumUpdateIntervals: " + e.getMessage());
            }
        }
    }

}
