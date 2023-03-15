package org.cyclops.integrateddynamics;

import com.google.common.collect.Lists;
import net.minecraftforge.fml.config.ModConfig;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.DummyConfig;
import org.cyclops.cyclopscore.tracking.Analytics;
import org.cyclops.cyclopscore.tracking.Versions;

import java.util.List;

/**
 * A config with general options for this mod.
 * @author rubensworks
 *
 */
public class GeneralConfig extends DummyConfig {

    @ConfigurableProperty(category = "core", comment = "If an anonymous mod startup analytics request may be sent to our analytics service.")
    public static boolean analytics = true;

    @ConfigurableProperty(category = "core", comment = "If the version checker should be enabled.")
    public static boolean versionChecker = true;

    @ConfigurableProperty(category = "machine", comment = "The default update frequency in ticks to use for new parts.", minimalValue = 1, configLocation = ModConfig.Type.SERVER)
    public static int defaultPartUpdateFreq = 1;

    @ConfigurableProperty(category = "general", comment = "The energy usage multiplier for networks.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int energyConsumptionMultiplier = 0;

    @ConfigurableProperty(category = "general", comment = "The base energy usage for the audio reader.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int audioReaderBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the audio writer.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int audioWriterBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the block reader.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int blockReaderBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the mono-directional connector.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int connectorMonoDirectionalBaseConsumption = 32;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the omni-directional connector.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int connectorOmniDirectionalBaseConsumption = 128;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the effect writer.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int effectWriterBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the entity reader.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int entityReaderBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the entity writer.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int entityWriterBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the extra-dimensional reader.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int extraDimensionalReaderBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the fluid reader.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int fluidReaderBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the inventory reader.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int inventoryReaderBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the inventory writer.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int inventoryWriterBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the machine reader.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int machineReaderBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the machine writer.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int machineWriterBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the materializer.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int materializerBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the network reader.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int networkReaderBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the display panel when it has a variable.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int panelDisplayBaseConsumptionEnabled = 2;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the display panel when does not have a variable.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int panelDisplayBaseConsumptionDisabled = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the dynamic light panel.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int panelLightDynamicBaseConsumption = 0;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the static light panel.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int panelLightStaticBaseConsumption = 0;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the proxy.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int proxyBaseConsumption = 2;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the redstone reader.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int redstoneReaderBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the redstone writer.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int redstoneWriterBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the variable store.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int variablestoreBaseConsumption = 4;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the world reader.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int worldReaderBaseConsumption = 1;

    @ConfigurableProperty(category = "general", comment = "The maximum render distance for part overlays to render. The higher, the more resource intensive.", isCommandable = true, minimalValue = 1, configLocation = ModConfig.Type.CLIENT)
    public static int partOverlayRenderdistance = 15;

    @ConfigurableProperty(category = "core", comment = "If cable models should be cached for rendering optimization.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static boolean cacheCableModels = true;

    @ConfigurableProperty(category = "core", comment = "The maximum network energy transfer rate.", isCommandable = true, minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int energyRateLimit = Integer.MAX_VALUE;

    @ConfigurableProperty(category = "machine", comment = "The maximum offset in blocks a directional connector can look for its target.", minimalValue = 1, configLocation = ModConfig.Type.SERVER)
    public static int maxDirectionalConnectorOffset = 512;

    @ConfigurableProperty(category = "machine", comment = "The NBT tags that are not allowed to be read by displaying NBT tags or performing operations on them.", configLocation = ModConfig.Type.SERVER)
    public static List<String> nbtTagBlacklist = Lists.newArrayList(); // Tag names that have to be ignored

    @ConfigurableProperty(category = "core", comment = "When enabled, networks will stop ticking and values will not be shown and evaluated again. This can be used to fix crashing networks by temporarily enabling this option.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static boolean safeMode = false;

    @ConfigurableProperty(category = "core", comment = "The fastest possible frequency in ticks at which ingredient network should be observed.", minimalValue = 1, isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static int ingredientNetworkObserverFrequencyMin = 5;

    @ConfigurableProperty(category = "core", comment = "The slowest possible frequency in ticks at which ingredient network should be observed.", minimalValue = 1, isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static int ingredientNetworkObserverFrequencyMax = 40;

    @ConfigurableProperty(category = "core", comment = "The ingredient network observation frequency slowdown rate in ticks.", minimalValue = 1, isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static int ingredientNetworkObserverFrequencyIncreaseFactor = 1;

    @ConfigurableProperty(category = "core", comment = "The ingredient network observation frequency slowdown rate in ticks.", minimalValue = 1, isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static int ingredientNetworkObserverFrequencyDecreaseFactor = 5;

    @ConfigurableProperty(category = "core", comment = "The frequency in ticks at which ingredient network should be observed after a position's contents are changed.", minimalValue = 0, isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static int ingredientNetworkObserverFrequencyForced = 0;

    @ConfigurableProperty(category = "core", comment = "The number of threads that the ingredient network observer can use.", minimalValue = 1, requiresMcRestart = true, configLocation = ModConfig.Type.SERVER)
    public static int ingredientNetworkObserverThreads = 4;

    @ConfigurableProperty(category = "core", comment = "If the ingredient network observer can work on separate thread.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static boolean ingredientNetworkObserverEnableMultithreading = true;

    @ConfigurableProperty(category = "core", comment = "If network change events should be logged. Only enable this when debugging.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static boolean logChangeEvents = false;

    @ConfigurableProperty(category = "core", comment = "How deep the recursion stack on an operator can become. This is to avoid game crashes when building things like the omega operator.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static int operatorRecursionLimit = 256;

    @ConfigurableProperty(category = "machine", comment = "Priority list of mod id's when determining tag-based recipe outputs.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static List<String> recipeTagOutputModPriorities = Lists.newArrayList();

    @ConfigurableProperty(category = "core", comment = "If corrupted networks should automatically be restored on first tick.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static boolean recreateCorruptedNetworks = true;

    @ConfigurableProperty(category = "core", comment = "The maximum frequency at which speach messages can be played in milliseconds.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static int speachMaxFrequency = 1000;

    @ConfigurableProperty(category = "core" , comment = "When true, use the LONG number format style. Otherwise, use the SHORT style.")
    public static boolean numberCompactUseLongStyle = false;

    @ConfigurableProperty(category = "core" , comment = "The maximum number of fractional digits to include in the result of the compact operator")
    public static int numberCompactMaximumFractionDigits = 2;

    @ConfigurableProperty(category = "core" , comment = "The minimum number of fractional digits to include in the result of the compact operator")
    public static int numberCompactMinimumFractionDigits = 0;

    @ConfigurableProperty(category = "core" , comment = "The maximum number of integer digits to include in the result of the compact operator")
    public static int numberCompactMaximumIntegerDigits = 3;

    @ConfigurableProperty(category = "core" , comment = "The minimum number of integer digits to include in the result of the compact operator")
    public static int numberCompactMinimumIntegerDigits = 1;

    @ConfigurableProperty(category = "general", comment = "The default port for running the network diagnostics HTTP server.", configLocation = ModConfig.Type.CLIENT)
    public static int diagnosticsWebServerPort = 3030;

    @ConfigurableProperty(category = "machine", comment = "The maximum offset in blocks a part can target.", minimalValue = 1, configLocation = ModConfig.Type.SERVER)
    public static int maxPartOffset = 32;

    @ConfigurableProperty(category = "machine", comment = "The distance from which part offsets should be shown.", minimalValue = 1, configLocation = ModConfig.Type.SERVER)
    public static int partOffsetRenderDistance = 64;

    public GeneralConfig() {
        super(IntegratedDynamics._instance, "general");
    }

    @Override
    public void onRegistered() {
        if(analytics) {
            Analytics.registerMod(getMod(), Reference.GA_TRACKING_ID);
        }
        if(versionChecker) {
            Versions.registerMod(getMod(), IntegratedDynamics._instance, Reference.VERSION_URL);
        }
    }

}
