package org.cyclops.integrateddynamics;

import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.DummyConfig;
import org.cyclops.cyclopscore.tracking.Analytics;
import org.cyclops.cyclopscore.tracking.Versions;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt;

/**
 * A config with general options for this mod.
 * @author rubensworks
 *
 */
public class GeneralConfig extends DummyConfig {

    /**
     * The current mod version, will be used to check if the player's config isn't out of date and
     * warn the player accordingly.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "Config version for " + Reference.MOD_NAME +".\nDO NOT EDIT MANUALLY!", showInGui = false)
    public static String version = Reference.MOD_VERSION;

    /**
     * If an anonymous mod startup analytics request may be sent to our analytics service.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If an anonymous mod startup analytics request may be sent to our analytics service.")
    public static boolean analytics = true;

    /**
     * If the version checker should be enabled.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If the version checker should be enabled.")
    public static boolean versionChecker = true;

    /**
     * The maximum allowed size of values in bytes to avoid network packet issues.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "The maximum allowed size of values in bytes to avoid network packet issues.", isCommandable = true)
    public static int maxValueByteSize = 20000;

    /**
     * The default update frequency in ticks to use for new parts.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The default update frequency in ticks to use for new parts.", minimalValue = 1)
    public static int defaultPartUpdateFreq = 1;

    /**
     * The energy usage multiplier for networks.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The energy usage multiplier for networks.", minimalValue = 0)
    public static int energyConsumptionMultiplier = 0;
    
    /**
     * The base energy usage for the audio reader.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the audio reader.", minimalValue = 0)
    public static int audioReaderBaseConsumption = 1;
    
    /**
     * The base energy usage for the audio writer.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the audio writer.", minimalValue = 0)
    public static int audioWriterBaseConsumption = 1;
    
    /**
     * The base energy usage for the block reader.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the block reader.", minimalValue = 0)
    public static int blockReaderBaseConsumption = 1;
    
    /**
     * The base energy usage for the mono-directional connector.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the mono-directional connector.", minimalValue = 0)
    public static int connectorMonoDirectionalBaseConsumption = 32;
    
    /**
     * The base energy usage for the omni-directional connector.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the omni-directional connector.", minimalValue = 0)
    public static int connectorOmniDirectionalBaseConsumption = 128;
    
    /**
     * The base energy usage for the effect writer.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the effect writer.", minimalValue = 0)
    public static int effectWriterBaseConsumption = 1;
    
    /**
     * The base energy usage for the entity reader.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the entity reader.", minimalValue = 0)
    public static int entityReaderBaseConsumption = 1;
    
    /**
     * The base energy usage for the entity writer.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the entity writer.", minimalValue = 0)
    public static int entityWriterBaseConsumption = 1;
    
    /**
     * The base energy usage for the extra-dimensional reader.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the extra-dimensional reader.", minimalValue = 0)
    public static int extraDimensionalReaderBaseConsumption = 1;
    
    /**
     * The base energy usage for the fluid reader.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the fluid reader.", minimalValue = 0)
    public static int fluidReaderBaseConsumption = 1;
    
    /**
     * The base energy usage for the inventory reader.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the inventory reader.", minimalValue = 0)
    public static int inventoryReaderBaseConsumption = 1;
    
    /**
     * The base energy usage for the inventory writer.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the inventory writer.", minimalValue = 0)
    public static int inventoryWriterBaseConsumption = 1;
    
    /**
     * The base energy usage for the machine reader.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the machine reader.", minimalValue = 0)
    public static int machineReaderBaseConsumption = 1;
    
    /**
     * The base energy usage for the machine writer.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the machine writer.", minimalValue = 0)
    public static int machineWriterBaseConsumption = 1;
    
    /**
     * The base energy usage for the materializer.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the materializer.", minimalValue = 0)
    public static int materializerBaseConsumption = 1;
    
    /**
     * The base energy usage for the network reader.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the network reader.", minimalValue = 0)
    public static int networkReaderBaseConsumption = 1;
    
    /**
     * The base energy usage for the display panel.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the display panel.", minimalValue = 0)
    public static int panelDisplayBaseConsumption = 2;
    
    /**
     * The base energy usage for the dynamic light panel.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the dynamic light panel.", minimalValue = 0)
    public static int panelLightDynamicBaseConsumption = 0;
    
    /**
     * The base energy usage for the static light panel.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the static light panel.", minimalValue = 0)
    public static int panelLightStaticBaseConsumption = 0;
    
    /**
     * The base energy usage for the proxy.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the proxy.", minimalValue = 0)
    public static int proxyBaseConsumption = 2;
    
    /**
     * The base energy usage for the redstone reader.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the redstone reader.", minimalValue = 0)
    public static int redstoneReaderBaseConsumption = 1;
    
    /**
     * The base energy usage for the redstone writer.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the redstone writer.", minimalValue = 0)
    public static int redstoneWriterBaseConsumption = 1;
    
    /**
     * The base energy usage for the variable store.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the variable store.", minimalValue = 0)
    public static int variablestoreBaseConsumption = 4;
    
    /**
     * The base energy usage for the world reader.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the world reader.", minimalValue = 0)
    public static int worldReaderBaseConsumption = 1;

    /**
     * The maximum render distance for part overlays to render. The higher, the more resource intensive.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The maximum render distance for part overlays to render. The higher, the more resource intensive.", isCommandable = true, minimalValue = 1)
    public static int partOverlayRenderdistance = 15;

    /**
     * The chance at which a Menril Tree will spawn in the wild, the higher this value, the lower the chance.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.WORLDGENERATION, comment = "The chance at which a Menril Tree will spawn in the wild, the higher this value, the lower the chance.", minimalValue = 0)
    public static int wildMenrilTreeChance = 350;

    /**
     * List of dimension IDs in which wild menril trees should not generate.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.WORLDGENERATION, comment = "List of dimension IDs in which wild menril trees should not generate.")
    public static int[] wildMenrilTreeDimensionBlacklist = new int[]{-1, 1};

    /**
     * If cable models should be cached for rendering optimization.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If cable models should be cached for rendering optimization.", isCommandable = true)
    public static boolean cacheCableModels = true;

    /**
     * The maximum network energy transfer rate.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "The maximum network energy transfer rate.", isCommandable = true, minimalValue = 0)
    public static int energyRateLimit = Integer.MAX_VALUE;

    /**
     * The maximum offset in blocks a directional connector can look for its target.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The maximum offset in blocks a directional connector can look for its target.", minimalValue = 1)
    public static int maxDirectionalConnectorOffset = 512;

    /**
     * The NBT tags that are not allowed to be read by displaying NBT tags or performing operations on them.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The NBT tags that are not allowed to be read by displaying NBT tags or performing operations on them.", changedCallback = ValueTypeNbt.BlacklistChangedCallback.class)
    public static String[] nbtTagBlacklist = {
            // Tag names that have to be ignored
    };

    /**
     * When enabled, networks will stop ticking and values will not be shown and evaluated again. This can be used to fix crashing networks by temporarily enabling this option.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "When enabled, networks will stop ticking and values will not be shown and evaluated again. This can be used to fix crashing networks by temporarily enabling this option.", isCommandable = true)
    public static boolean safeMode = false;

    /**
     * The fastest possible frequency in ticks at which ingredient network should be observed.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "The fastest possible frequency in ticks at which ingredient network should be observed.", minimalValue = 1, isCommandable = true)
    public static int ingredientNetworkObserverFrequencyMin = 5;

    /**
     * The slowest possible frequency in ticks at which ingredient network should be observed.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "The slowest possible frequency in ticks at which ingredient network should be observed.", minimalValue = 1, isCommandable = true)
    public static int ingredientNetworkObserverFrequencyMax = 40;

    /**
     * The ingredient network observation frequency slowdown rate in ticks.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "The ingredient network observation frequency slowdown rate in ticks.", minimalValue = 1, isCommandable = true)
    public static int ingredientNetworkObserverFrequencyIncreaseFactor = 1;

    /**
     * The ingredient network observation frequency speedup rate in ticks.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "The ingredient network observation frequency slowdown rate in ticks.", minimalValue = 1, isCommandable = true)
    public static int ingredientNetworkObserverFrequencyDecreaseFactor = 5;

    /**
     * The frequency in ticks at which ingredient network should be observed after a position's contents are changed.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "The frequency in ticks at which ingredient network should be observed after a position's contents are changed.", minimalValue = 0, isCommandable = true)
    public static int ingredientNetworkObserverFrequencyForced = 0;

    /**
     * The number of threads that the ingredient network observer can use.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "The number of threads that the ingredient network observer can use.", minimalValue = 1, requiresMcRestart = true)
    public static int ingredientNetworkObserverThreads = 4;

    /**
     * If the ingredient network observer can work on separate thread.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If the ingredient network observer can work on separate thread.", isCommandable = true)
    public static boolean ingredientNetworkObserverEnableMultithreading = true;

    /**
     * If network change events should be logged. Only enable this when debugging.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If network change events should be logged. Only enable this when debugging.", isCommandable = true)
    public static boolean logChangeEvents = false;

    /**
     * How deep the recursion stack on an operator can become. This is to avoid game crashes when building things like the omega operator.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "How deep the recursion stack on an operator can become. This is to avoid game crashes when building things like the omega operator.", isCommandable = true)
    public static int operatorRecursionLimit = 256;
    
    /**
     * Create a new instance.
     */
    public GeneralConfig() {
        super(IntegratedDynamics._instance, true, "general", null, GeneralConfig.class);
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
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}
