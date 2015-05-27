package org.cyclops.integrateddynamics;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.client.gui.GuiHandler;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.init.ItemCreativeTab;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.persist.world.GlobalCounters;
import org.cyclops.cyclopscore.proxy.ICommonProxy;
import org.cyclops.integrateddynamics.core.TickHandler;
import org.cyclops.integrateddynamics.core.client.gui.ExtendedGuiHandler;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRegistry;
import org.cyclops.integrateddynamics.core.persist.world.NetworkWorldStorage;

/**
 * The main mod class of IntegratedDynamics.
 * @author rubensworks
 *
 */
@Mod(modid = Reference.MOD_ID,
     name = Reference.MOD_NAME,
     useMetadata = true,
     version = Reference.MOD_VERSION,
     dependencies = Reference.MOD_DEPENDENCIES
)
public class IntegratedDynamics extends ModBase {

    /**
     * The proxy of this mod, depending on 'side' a different proxy will be inside this field.
     * @see net.minecraftforge.fml.common.SidedProxy
     */
    @SidedProxy(clientSide = "org.cyclops.integrateddynamics.proxy.ClientProxy", serverSide = "org.cyclops.integrateddynamics.proxy.CommonProxy")
    public static ICommonProxy proxy;

    /**
     * The unique instance of this mod.
     */
    @Mod.Instance(value = Reference.MOD_ID)
    public static IntegratedDynamics _instance;

    public static GlobalCounters globalCounters = null;

    public IntegratedDynamics() {
        super(Reference.MOD_ID, Reference.MOD_NAME);

        // Register world storages
        registerWorldStorage(NetworkWorldStorage.getInstance(this));
        registerWorldStorage(globalCounters = new GlobalCounters(this));
    }

    @Override
    protected GuiHandler constructGuiHandler() {
        return new ExtendedGuiHandler(this);
    }

    @Mod.EventHandler
    @Override
    public final void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        getRegistryManager().addRegistry(IAspectRegistry.class, AspectRegistry.getInstance());

        FMLCommonHandler.instance().bus().register(TickHandler.getInstance());
    }

    @Mod.EventHandler
    @Override
    public final void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Mod.EventHandler
    @Override
    public final void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Mod.EventHandler
    @Override
    public void onServerStarted(FMLServerStartedEvent event) {
        super.onServerStarted(event);
    }

    @Mod.EventHandler
    @Override
    public void onServerStopping(FMLServerStoppingEvent event) {
        super.onServerStopping(event);
    }

    @Override
    public CreativeTabs constructDefaultCreativeTab() {
        return new ItemCreativeTab(this, Items.bed); // TODO: temp
    }

    @Override
    public void onGeneralConfigsRegister(ConfigHandler configHandler) {
        configHandler.add(new GeneralConfig());
    }

    @Override
    public void onMainConfigsRegister(ConfigHandler configHandler) {
        Configs.registerBlocks(configHandler);
    }

    @Override
    public ICommonProxy getProxy() {
        return proxy;
    }

    /**
     * Log a new info message for this mod.
     * @param message The message to show.
     */
    public static void clog(String message) {
        IntegratedDynamics._instance.log(Level.INFO, message);
    }

    /**
     * Log a new message of the given level for this mod.
     * @param level The level in which the message must be shown.
     * @param message The message to show.
     */
    public static void clog(Level level, String message) {
        IntegratedDynamics._instance.log(level, message);
    }
}