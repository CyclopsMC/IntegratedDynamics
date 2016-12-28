package org.cyclops.integrateddynamics;

import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.DummyConfig;
import org.cyclops.cyclopscore.tracking.Analytics;
import org.cyclops.cyclopscore.tracking.Versions;

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
     * The maximum render distance for part overlays to render. The higher, the more resource intensive.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The maximum render distance for part overlays to render. The higher, the more resource intensive.", isCommandable = true, minimalValue = 1)
    public static int partOverlayRenderdistance = 15;

    /**
     * The chance at which a Menril Tree will spawn in the wild, the higher, the lower the chance.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.WORLDGENERATION, comment = "The chance at which a Menril Tree will spawn in the wild, the higher, the lower the chance.", minimalValue = 0)
    public static int wildMenrilTreeChance = 350;

    /**
     * If cable models should be cached for rendering optimization.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If cable models should be cached for rendering optimization.", isCommandable = true)
    public static boolean cacheCableModels = true;
    
    /**
     * Create a new instance.
     */
    public GeneralConfig() {
        super(IntegratedDynamics._instance, true, "general", null, GeneralConfig.class);
    }
    
    @Override
    public void onRegistered() {
        // Check version of config file
        if(!version.equals(Reference.MOD_VERSION))
            System.err.println("The config file of " + Reference.MOD_NAME + " is out of date and might cause problems, please remove it so it can be regenerated.");

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
